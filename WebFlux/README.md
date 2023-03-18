This directory contains all the WebFlux source code examples Here's an
overview of what's included:

. ex1 - This program applies WebFlux and Project Reactor features to
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

. ex2 - ...

