DO NOT EDIT - See: https://www.eclipse.org/jetty/documentation/current/startup-modules.html

[description]
Enables a Unix Domain Socket Connector that can receive
requests from a local proxy and/or SSL offloader (eg haproxy) in either
HTTP or TCP mode.  Unix Domain Sockets are more efficient than 
localhost TCP/IP connections  as they reduce data copies, avoid 
needless fragmentation and have better dispatch behaviours. 
When enabled with corresponding support modules, the connector can 
accept HTTP, HTTPS or HTTP2C traffic.

[tags]
connector

[depend]
server

[xml]
etc/jetty-unixsocket.xml

[files]
maven://com.github.jnr/jnr-unixsocket/@jnr-unixsocket.version@|lib/jnr/jnr-unixsocket-@jnr-unixsocket.version@.jar
maven://com.github.jnr/jnr-ffi/2.1.9|lib/jnr/jnr-ffi-2.1.9.jar
maven://com.github.jnr/jffi/1.2.17|lib/jnr/jffi-1.2.17.jar
maven://com.github.jnr/jffi/1.2.16/jar/native|lib/jnr/jffi-1.2.16-native.jar
maven://org.ow2.asm/asm/@asm.version@|lib/jnr/asm-@asm.version@.jar
maven://org.ow2.asm/asm-commons/@asm.version@|lib/jnr/asm-commons-@asm.version@.jar
maven://org.ow2.asm/asm-analysis/@asm.version@|lib/jnr/asm-analysis-@asm.version@.jar
maven://org.ow2.asm/asm-tree/@asm.version@|lib/jnr/asm-tree-@asm.version@.jar
maven://org.ow2.asm/asm-util/@asm.version@|lib/jnr/asm-util-@asm.version@.jar
maven://com.github.jnr/jnr-x86asm/1.0.2|lib/jnr/jnr-x86asm-1.0.2.jar
maven://com.github.jnr/jnr-constants/0.9.11|lib/jnr/jnr-constants-0.9.11.jar
maven://com.github.jnr/jnr-enxio/0.18|lib/jnr/jnr-enxio-0.18.jar
maven://com.github.jnr/jnr-posix/3.0.46|lib/jnr/jnr-posix-3.0.46.jar

[lib]
lib/jetty-unixsocket-${jetty.version}.jar
lib/jnr/*.jar

[license]
Jetty UnixSockets is implemented using the Java Native Runtime, which is an
open source project hosted on Github and released under the Apache 2.0 license.
https://github.com/jnr/jnr-unixsocket
http://www.apache.org/licenses/LICENSE-2.0.html

[ini-template]
### Unix SocketHTTP Connector Configuration

## Connector host/address to bind to
# jetty.unixsocket=/tmp/jetty.sock

## Connector idle timeout in milliseconds
# jetty.unixsocket.idleTimeout=30000

## Number of selectors (-1 picks default 1)
# jetty.unixsocket.selectors=-1

## ServerSocketChannel backlog (0 picks platform default)
# jetty.unixsocket.acceptorQueueSize=0
