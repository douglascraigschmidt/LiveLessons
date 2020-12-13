# Getting Started

This example shows how to use the Spring WebFlux framework together
with Project Reactor to implement an airline reservation app
synchronously and asynchronously communicates with various
microservices that find the best price for flight legs and convert
from US dollars into other currencies.  The following are the steps
required to build and run this WebFlux project.

1. Change directory to the Reactor/ex5 folder and run the following
   command from the shell:

   % mvn spring-boot:run

   This starts the Tomcat webserver by default on port 8080 and also
   starts the FindBestPriceApplication and ExchangeRateApplication.

   Conversely, this application can be run from within the Intellij by
   selecting run on the FindBestPriceApplication and
   ExchangeRateApplication.

2. Below are the end points to access the application via curl,
   postman, or any other Http/Java client (including web browers):

### Reference Documentation
For further reference, please read the following documents

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.2.7.RELEASE/gradle-plugin/reference/html/)
* [Rest Repositories](https://docs.spring.io/spring-boot/docs/2.2.7.RELEASE/reference/htmlsingle/#howto-use-exposing-spring-data-repositories-rest-endpoint)

### Guides
The following guides illustrate how to use some features concretely:

* [Accessing JPA Data with REST](https://spring.io/guides/gs/accessing-data-rest/)
* [Accessing Neo4j Data with REST](https://spring.io/guides/gs/accessing-neo4j-data-rest/)
* [Accessing MongoDB Data with REST](https://spring.io/guides/gs/accessing-mongodb-data-rest/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

