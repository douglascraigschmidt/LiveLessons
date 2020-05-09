# Getting Started

The following are the steps required to build and run this example.

1. Change directory to the Folders/ReactiveSpring folder and run
   the following command from the shell:

   % mvn spring-boot:run

   This starts the Tomcat webserver by default on port 8080 and also
   starts the FolderApplication.

   This application can also be run from within the IDE by selecting
   run on FolderApplication.  Please see
   
   https://www.javadevjournal.com/spring-boot/spring-boot-application-intellij/

   for information on how to integrate Spring Boot into Intellij.

2. Below are the end points to access the application via curl,
   postman, or any other Http/Java client (including web browers):

   . Search count for a key at a given root directory using HTTP GET via

     http://localhost:8080/folders/works/_search?key=CompletableFuture

     You can pass any root director and Search key using HTTP GET via

     localhost:8080/folders/{rootDir}/_search?key="SEARCH_KEY"

   . Count entries recursively at a given root directory using HTTP GET via

     http://localhost:8080/folders/works/_count
     http://localhost:8080/folders/{rootDir}/_count

   . Getting recursive Dirent at a given root director using HTTP GET via

     http://localhost:8080/folders/works/
     http://localhost:8080/folders/{rootDir}

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

