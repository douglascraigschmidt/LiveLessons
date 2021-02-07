This program applies reactive streams features to implement a
multi-tier Airline Booking App (ABA) as an Intellij project.  The ABA
project will be used as a motivating example throughout our upcoming
Coursera MOOC Specialization on Developing Secure and Scalable Restful
APIs for Reactive Microservices.  This six-part MOOC Specialization is
expected to launch at some point in 2021.

The ABA project showcases a wide range of Java concurrency and
parallelism frameworks that are used to synchronously and
asynchronously communicate with various microservices to find prices
for flights and convert these prices from US dollars to other
currencies.  These price are displayed after the microservices
complete their computations.

The current version of ABA applies the Project Reactor and RxJava
reactive streams implementations together with the Spring WebFlux
reactive web application framework.  Other implementations will
showcase Java frameworks that provide concurrent object-oriented
programming and functional parallel programming capabilities (such as
the Java executor, parallel streams, and completable futures
frameworks), conventional RESTful microservices (such as Spring Boot),
and object-oriented and reactive database programming models (such as
the JPA and R2DBC).

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
