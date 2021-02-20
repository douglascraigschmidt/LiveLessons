This program applies object-oriented, functional, and reactive
programming capabilities to implement a multi-tier Airline Booking App
(ABA) as an Intellij project.  The ABA project is used as a motivating
example throughout our upcoming Coursera MOOC Specialization on
Scalable Microservices for Developers.  This six-part MOOC
Specialization is expected to launch at some point in 2021.

The ABA project showcases a wide range of Java concurrency and
parallelism frameworks that are used to synchronously and
asynchronously communicate with various microservices to find prices
for flights and convert these prices to/from various currencies.
These price are displayed after the microservices complete their
computations.

The current version of ABA demonstrates a broad range of programming
paradigms, including

. Concurrent object-oriented programming via Java threads and the Java
  executor framework

. Functional parallel and asynchronous programming via the Java
  parallel streams and completable futures frameworks.

. Reactive programming via the Project Reactor and RxJava reactive
  streams frameworks

The paradigms are applied in the context of the Spring WebFlux
reactive webapp framework, as well as conventional RESTful
microservices via Spring Boot, object-oriented and reactive database
programming models (such as the JPA and R2DBC), Android, and popular
mocking frameworks (Such as MockK).

This ABA client provides a simple console app developed and run using
Intellij.  The following are the steps required to run this Intellij
project:

1. Open the Intellij project and then select and run the
   "Microservices" compound application, which launches a number of
   microservices that run locally to simplify testing.

2. After all the microservices are up and running then select and run
   the "Client" application, which then communicates with the
   microservices to demonstrate the ABA functionality.

The forthcoming MOOC specialization will also include an Android
client that provides a more interesting GUI app developed and run
using Android Studio.
