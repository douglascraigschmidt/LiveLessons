This program applies reactive programming capabilities to implement a
client/server application that demonstrates several RSocket
interaction models.  RSocket is a binary point-to-point communication
protocol designed for use in networked applications that communicate
via Project Reactor Flux and Mono reactive objects.  It provides a
(potentially) more efficient alternative to other popular protocols,
such as HTTP that use non-binary encodings (such as XML and JSon).

This program provides a simple console app developed and run using
RSocket, Spring, Project Reactor, and Intellij.  It demonstrates the
following RSocket interaction models

. Request/Response, where each two-way request receives a single
  response from the server.

. Fire-and-Forget, where each one-way request receives no response
  from the server.

. Request/Stream, where each request receives a stream of responses
  from the server.

The following are the steps required to run this Intellij project:

1. Open the Intellij project and then select and run the
   "ZippyMicroservice" application, which launches a microservice that
   run locally to simplify testing.

2. After ZippyMicroservice is up and running then select and run the
   "ZippyMicroserviceTest" application, which then communicates with
   the ZippyMicroservice to demonstrate the program functionality.

