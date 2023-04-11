This directory contains all the WebFlux source code examples Here's an
overview of what's included:

. ex1 - This program tests the features of the LockApplication
        microservice, which uses Spring WebFlux to provide a
        distributed lock manager for Spring applications using an
        asynchronous Spring controller method that returns a Mono
        reactive type.  It also shows how to use the HTTP interface
        features in Spring framework 6, which enables the definition
        of declarative HTTP services using Java interfaces.

. ex1 - This example shows how Spring WebFlux can be used to send and
        receive HTTP GET requests via the Project Reactor Flux and
        ParallelFlux reactive types.  These requests are mapped to
        methods that determine the primality of large Integer objects.

. ex3 - This example demonstrates the ability to use Spring WebFlux
        features so a client uses the Eureka discover service in
        conjunction with an API gateway to interact with two
        microservices that provide quotes from Zippy th' Pinhead and
        Jack Handey.

. ex4 - This program applies WebFlux and Project Reactor features to
        implement various types of backpressure strategies (e.g.,
        ignore, buffer, error, latest, drop, and push/pull) between a
        publisher that runs as a micro-service in one process and
        produces a flux stream of random integers and a subscriber
        that runs in one or more threads in a different process and
        consumes this stream of integers.  This program also measures
        the performance of checking these random numbers for primality
        with and without various types of memoizers (e.g., untimed and
        timed) based on Java ConcurrentHashMap.  In addition, it
        demonstrates the use of slicing with the Flux takeWhile() and
        skipWhile() operations.

