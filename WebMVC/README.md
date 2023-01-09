This directory contains examples where clients use Spring WebMVC
features to perform remote method invocations from a client to a
server over HTTP.

Here's an overview of what's currently included in these examples:

. ex1 - This example shows how WebMVC can be used to send and receive
        HTTP GET requests via the Java parallel streams framework, the
        completable futures framework, and the Java structured
        concurrency frameworks.  These requests are mapped to methods
        that determine the primality of large Integer objects.

. ex2 - 

. ex3 - This example demonstrate Java's structured concurrency
        frameworks by (1) checking the primality of a List of random
        Integer objects and (2) computing the greatest common divisor
        (GCD) of pairs of these Integer objects using Spring WebMVC
        microservices.

. ex4 - This example demonstrates the ability to use Spring WebMVC
        features so a client can interact with the various
        microservices via an API Gateway.

. ex5 - This example shows the use of Spring WebMVC to apply Java
        sequential and parallel streams to process entries in a
        recursively-structured directory folder sequentially and/or
        concurrently in a client/server environment.  This example
        also shows how to encode/decode complex objects that use
        inheritance relationships and transmits them between
        processes.
