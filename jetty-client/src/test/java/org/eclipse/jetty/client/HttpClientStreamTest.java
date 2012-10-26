//
//  ========================================================================
//  Copyright (c) 1995-2012 Mort Bay Consulting Pty. Ltd.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package org.eclipse.jetty.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.toolchain.test.MavenTestingUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Assert;
import org.junit.Test;

import static java.nio.file.StandardOpenOption.CREATE;
import static org.junit.Assert.fail;

public class HttpClientStreamTest extends AbstractHttpClientServerTest
{
    public HttpClientStreamTest(SslContextFactory sslContextFactory)
    {
        super(sslContextFactory);
    }

    @Test
    public void testFileUpload() throws Exception
    {
        // Prepare a big file to upload
        Path targetTestsDir = MavenTestingUtils.getTargetTestingDir().toPath();
        Files.createDirectories(targetTestsDir);
        Path upload = Paths.get(targetTestsDir.toString(), "http_client_upload.big");
        try (OutputStream output = Files.newOutputStream(upload, CREATE))
        {
            byte[] kb = new byte[1024];
            for (int i = 0; i < 10 * 1024; ++i)
                output.write(kb);
        }

        start(new EmptyServerHandler());

        final AtomicLong requestTime = new AtomicLong();
        ContentResponse response = client.newRequest("localhost", connector.getLocalPort())
                .scheme(scheme)
                .file(upload)
                .listener(new Request.Listener.Empty()
                {
                    @Override
                    public void onSuccess(Request request)
                    {
                        requestTime.set(System.nanoTime());
                    }
                })
                .send()
                .get(10, TimeUnit.SECONDS);
        long responseTime = System.nanoTime();

        Assert.assertEquals(200, response.getStatus());
        Assert.assertTrue(requestTime.get() <= responseTime);

        // Give some time to the server to consume the request content
        // This is just to avoid exception traces in the test output
        Thread.sleep(1000);
    }

    @Test
    public void testDownload() throws Exception
    {
        final byte[] data = new byte[128 * 1024];
        byte value = 1;
        Arrays.fill(data, value);
        start(new AbstractHandler()
        {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
            {
                baseRequest.setHandled(true);
                response.getOutputStream().write(data);
            }
        });

        InputStreamResponseListener listener = new InputStreamResponseListener();
        client.newRequest("localhost", connector.getLocalPort())
                .scheme(scheme)
                .send(listener);
        Response response = listener.get(5, TimeUnit.SECONDS);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        InputStream input = listener.getInputStream();
        Assert.assertNotNull(input);

        int length = 0;
        while (input.read() == value)
        {
            if (length % 100 == 0)
                Thread.sleep(1);
            ++length;
        }

        Assert.assertEquals(data.length, length);

        Result result = listener.await(5, TimeUnit.SECONDS);
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isFailed());
        Assert.assertSame(response, result.getResponse());
    }

    @Test
    public void testDownloadWithFailure() throws Exception
    {
        final byte[] data = new byte[64 * 1024];
        byte value = 1;
        Arrays.fill(data, value);
        start(new AbstractHandler()
        {
            @Override
            public void handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
            {
                baseRequest.setHandled(true);
                // Say we want to send this much...
                response.setContentLength(2 * data.length);
                // ...but write only half...
                response.getOutputStream().write(data);
                // ...then shutdown output
                baseRequest.getHttpChannel().getEndPoint().shutdownOutput();
            }
        });

        InputStreamResponseListener listener = new InputStreamResponseListener();
        client.newRequest("localhost", connector.getLocalPort())
                .scheme(scheme)
                .send(listener);
        Response response = listener.get(5, TimeUnit.SECONDS);
        Assert.assertNotNull(response);
        Assert.assertEquals(200, response.getStatus());

        InputStream input = listener.getInputStream();
        Assert.assertNotNull(input);

        int length = 0;
        try
        {
            length = 0;
            while (input.read() == value)
            {
                if (length % 100 == 0)
                    Thread.sleep(1);
                ++length;
            }
            fail();
        }
        catch (IOException expected)
        {
        }

        Assert.assertEquals(data.length, length);

        Result result = listener.await(5, TimeUnit.SECONDS);
        Assert.assertNotNull(result);
        Assert.assertTrue(result.isFailed());
    }
}
