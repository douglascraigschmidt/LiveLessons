# Getting Started

This example shows how to use the Spring WebMVC framework together
with the Java streams framework to process entries in a large
recursive directory folder concurrently as a web service.  The
following are the steps required to build and run this WebMVC project.

1. Run this from within the Intellij by selecting run on the
   FolderApplication.

2. Below are the end points to access the application via curl,
   postman, or any other Http/Java client (including web browers):

   . Search count for a key at a given root directory using HTTP GET via

     http://localhost:8081/folders/works/searchFolder?word=CompletableFuture

     You can pass any root director and Search word using HTTP GET via

     localhost:8081/folders/{rootDir}/searchFolder?word=SomeWord&concurrent=true

   . Count entries recursively at a given root directory using HTTP
     GET via

     http://localhost:8081/folders/works/countDocuments
     http://localhost:8081/folders/{rootDir}/countDocuments

   . Get recursive folder entries at a given root directory using HTTP GET via

     http://localhost:8081/folders/works/createFolder?memoize=false&concurrent=true
     http://localhost:8081/folders/{rootDir}/createFolder?memoize=false&concurrent=true

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

