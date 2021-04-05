This program applies reactive programming capabilities to implement a
client/server application that demonstrates several RSocket
interaction models.  RSocket is a binary point-to-point
application-level protocol designed for use in client/server programs
that communicate using the Flux and Mono classes defined in the
Project Reactor reactive streams framework.  RSocket is designed to
provide a (potentially) more efficient alternative to other popular
application-level protocols, such as HTTP using non-binary encodings
like XML and JSon.  More information on RSocket is available
[here](https://rsocket.io) and
[here](https://www.baeldung.com/rsocket).

The program implemented in this Intellij project is developed and run
using RSocket, Spring, Project Reactor, and Java functional
programming features.  It demonstrates all four RSocket interaction
models that communicate between a test program and a Spring-based
microservice, including

. Request/Response, where each two-way async request receives a single
  async response from the server.

. Fire-and-Forget, where each one-way message receives no response
  from the server.

. Request/Stream, where each async request receives a stream of
  responses from the server.

. Channel, where a stream of async messages can be sent in both
  directions between client and server.

The functionality of the server is implemented via the
ZippyApplication, which is a reactive microservice that provides zany
Zippy th' Pinhead quotes to clients using all four RSocket interaction
models.

To run this program all you need to do is open the Intellij project
and then select and run the tests in the ex1>src>main>tests
folder. These tests automatically launch the ZippyApplication reactive
microservice locally on the local computer and then runs all the unit
tests in the ZippyMicroserviceTest.java file to demonstrate the
RSocket client and server functionality.

