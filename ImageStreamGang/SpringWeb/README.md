The following are the steps required to build and run this example.

1. Change directory to the ImageStreamGang/SpringWeb folder and run
   the following command from the shell:

   % mvn spring-boot:run

   This starts the Tomcat webserver by default on port 8080 and also
   starts the WebCrawlerApplication.

   This application can also be run from within the IDE by selecting
   run on WebCrawlerApplication.  Please see
   
   https://www.javadevjournal.com/spring-boot/spring-boot-application-intellij/

   for information on how to integrate Spring Boot into Intellij.

2. Below are the end points to access the application

   . http://localhost:8080/run (Post Query. Use curl or postman to invoke or use any Http/Java client)
   . http://localhost:8080/timingresults (Get query to fetch timing results)
