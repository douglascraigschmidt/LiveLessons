This directory contains all the Project Reactor source code examples
from my webinars on [Modern Java by
Example](http://www.dre.vanderbilt.edu/~schmidt/MJBE).  Here's an
overview of what's included:

. ex1 - This example shows how to apply timeouts with the Project
         Reactor framework.
  
. ex2 - This example demonstrates various reactive algorithms for
        finding all the minimum values in an unordered list, which is
        surprisingly not well documented in the literature.  These
        three algorithms use Project Reactor features to return
        Flux/Mono reactive types that emit the cheapest flight(s) from
        a Flux of available flights.  Another three algorithms use
        Java Streams features to perform the same computations.

. ex3 - This program applies Project Reactor features to implement
        various types of backpressure strategies (e.g., ignore,
        buffer, error, latest, drop, and push/pull) between a
        publisher and a subscriber that (conditionally) run in
        different threads/schedulers.  This program also measures the
        performance of checking random numbers for primality with and
        without various types of memoizers (e.g., untimed and timed)
        based on Java ConcurrentHashMap.  In addition, it demonstrates
        the use of slicing with the Flux takeWhile() and skipWhile()
        operations.

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

. ex5 - This program applies object-oriented, functional, and reactive
        programming capabilities to implement a multi-tier Airline
        Booking App (ABA) as an Intellij project.  The ABA project
        showcases a wide range of Java concurrency and parallelism
        frameworks that are used to synchronously and asynchronously
        communicate with various microservices to find prices for
        flights and convert these prices to/from various currencies.
        These price are displayed after the microservices complete
        their computations.

