This program applies reactive programming capabilities to implement a
client/server application that demonstrates several
[RSocket](https://rsocket.io) interaction models.  RSocket is a binary
point-to-point application-level protocol designed for use in
client/server programs that communicate using the
[Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html)
and
[Mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html)
classes defined in the [Project Reactor](https://projectreactor.io/)
reactive streams framework.  RSocket is designed to provide a
(potentially) more efficient alternative to other popular
application-level protocols, such as HTTP using non-binary encodings
like XML and JSon.  A tutorial on RSocket is available
[here](https://www.baeldung.com/rsocket).

The program implemented in this Intellij project is developed and run
using RSocket, Spring, Project Reactor, and Java functional
programming features.  It demonstrates all four RSocket interaction
models that communicate between a test program and a Spring-based
microservice, including

* Request/Response, where each two-way async request receives a single
  async response from the server.

* Fire-and-Forget, where each one-way message receives no response
  from the server.

* Request/Stream, where each async request receives a stream of
  responses from the server.

* Channel, where a stream of async messages can be sent in both
  directions between client and server.

The server runs in the context of the
[ZippyApplication](src/main/java/zippyisms/application/ZippyApplication.java)
reactive microservice, which uses the
[ZippyController](src/main/java/zippyisms/controller/ZippyController.java)
and [ZippyService](src/main/java/zippyisms/service/ZippyService.java)
to provide zany [Zippy th'
Pinhead](https://en.wikipedia.org/wiki/Zippy_the_Pinhead) quotes to
clients using all four RSocket interaction models.

To run this program you simply need to open the provided Intellij
project and then select and run the tests in the `src>main>tests`
folder. These tests automatically launch the ZippyApplication reactive
microservice locally on the local computer and then run all the
SpringBoot [@Test](https://www.baeldung.com/spring-boot-testing)
annotations in the
[ZippyMicroserviceTest.java](src/test/java/zippyisms/application/ZippyMicroserviceTest.java)
file to demonstrate common RSocket functionality for both client and
server programs.

