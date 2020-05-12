This directory contains all the Project Reactor source code examples
from my webinars on [Modern Java by
Example](http://www.dre.vanderbilt.edu/~schmidt/MJBE).  Here's an
overview of what's included:

. ex1 - This example shows how to reduce and/or multiply big fractions
        asynchronously using a wide range of features in the Reactor
        framework, including flatMap(), collectList(), zipWith(),
        first(), when(), and onErrorResume().
  
. ex2 - This example is currently just a placeholder for random things
        I'm playing around with for Project Reactor.  It's likely to
        change radically from time to time.

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

. ex4- This program applies WebFlux and Project Reactor features to
       implement various types of backpressure strategies (e.g.,
       ignore, buffer, error, latest, drop, and push/pull) between a
       publisher that runs as a micro-service in one process and
       produces a flux stream of random integers and a subscriber that
       runs in one or more threads in a different process and consumes
       this stream of integers.  This program also measures the
       performance of checking these random numbers for primality with
       and without various types of memoizers (e.g., untimed and
       timed) based on Java ConcurrentHashMap.  In addition, it
       demonstrates the use of slicing with the Flux takeWhile() and
       skipWhile() operations.
