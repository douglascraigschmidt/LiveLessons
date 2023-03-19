This directory contains examples where clients use Spring WebMVC
features to perform remote method invocations from a client to a
server over HTTP.

Here's an overview of what's currently included in these examples:

. ex1 - This example shows how Spring WebMVC can be used to send and
        receive HTTP GET requests via the Java parallel streams
        framework, the completable futures framework, and the Java
        structured concurrency frameworks.  These requests are mapped
        to methods that determine the primality of large Integer
        objects.

. ex2 - This example shows how Spring WebMVC can be used to obtain
        quotes from Yogi Berra in various ways.

. ex3 - This example demonstrate Java's structured concurrency
        frameworks using two Spring WebMVC microservices that (1)
        check the primality of a List of random Integer objects and
        (2) compute the greatest common divisor (GCD) of pairs of
        these Integer objects.

. ex4 - This example demonstrates the ability to use Spring WebMVC
        features so a client uses an API gateway to interact with two
        microservices that provide quotes from Zippy th' Pinhead and
        Jack Handey.

. ex5 - This program tests the features of the LockApplication
        microservice, which uses Spring WebMVC to provide a
        distributed lock manager for Spring applications using
        quasi-asynchronous Spring DeferredResult controller methods
        with a synchronous client.  It also shows how to use the HTTP
        interface features in Spring framework 6, which enables the
        definition of declarative HTTP services using Java interfaces.

. ex6 - This example shows the use of Spring WebMVC to apply Java
        sequential and parallel streams to process entries in a
        recursively-structured directory folder sequentially and/or
        concurrently in a client/server environment.  This example
        also shows how to encode/decode complex objects that use
        inheritance relationships and transmits them between
        processes.


