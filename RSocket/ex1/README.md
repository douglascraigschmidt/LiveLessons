This program applies reactive programming capabilities to implement a
client/server application that demonstrates all
[RSocket](https://rsocket.io) interaction models.  RSocket is a binary
point-to-point application-level protocol designed for use in
client/server programs that communicate using the
[Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html)
and
[Mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html)
classes defined in the [Project Reactor](https://projectreactor.io/)
reactive streams framework.  RSocket provides a (potentially) more
efficient alternative to other popular application-level protocols,
such as HTTP using non-binary encodings like XML and JSon.  Tutorial
on RSocket are available [here](https://www.baeldung.com/rsocket) and
[here](https://spring.io/blog/2020/03/02/getting-started-with-rsocket-spring-boot-server).

The program implemented in this Intellij project is developed and run
using [RSocket](https://rsocket.io), [Spring
Boot](https://spring.io/projects/spring-boot), [Project
Reactor](https://projectreactor.io), and [Java functional
programming](http://www.dre.vanderbilt.edu/~schmidt/cs253/) features.
It demonstrates all [four RSocket interaction
models](https://docs.spring.io/spring-framework/docs/current/reference/pdf/rsocket.pdf)
that communicate between a client test program and a Spring-based
microservice, including

* **Request/Response**, where each two-way async request receives a single
  async response from the server.

* **Fire-and-Forget**, where each one-way message receives no response
  from the server.

* **Request/Stream**, where each async request receives a stream of
  responses from the server.

* **Channel**, where a stream of async messages can be sent in both
  directions between client and server.

Spring enables the integration of RSockets into a controller via the
@Controller annotation, which enables the autodetection of
implementation classes via classpath scanning, and the @MessageMapping
annotation, which maps a message onto a message-handling method by
matching the declared patterns to a destination extracted from the
message.

Combining the @Controller annotation with the @MessageMapping
annotation enables this class to declare service endpoints, which in
this case map to RSocket endpoints that each take one Mono or Flux
parameter and can return a Mono or Flux result.  The use of Mono and
Flux types enables client and server code to run reactively across a
communication channel.

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
microservice locally on the local computer. They then run all the
SpringBoot [@Test](https://www.baeldung.com/spring-boot-testing)
annotations in the
[ZippyMicroserviceTest.java](src/test/java/zippyisms/application/ZippyMicroserviceTest.java)
file to demonstrate common RSocket functionality for both client and
server programs.

