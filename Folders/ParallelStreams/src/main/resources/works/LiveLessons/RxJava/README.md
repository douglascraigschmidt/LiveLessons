This directory contains all the RxJava source code examples from my
webinars on [Modern Java by
Example](http://www.dre.vanderbilt.edu/~schmidt/MJBE).  Here's an
overview of what's included:

. ex1 - This example shows how to apply timeouts with the RxJava
        framework.
  
. ex2 - This example downloads multiple images from a remote web
        server via several different Java concurrency/parallelism
        frameworks, including the parallel streams, RxJava, and
        Project Reactor.  It also compares the performance of the Java
        parallel streams framework with and without the {@code
        ForkJoinPool.ManagedBlocker} interface and the Java common
        fork-join pool.

. ex3 - This example demonstrates various RxJava mechanisms for
        determining if a flow of random big integers are prime numbers
        or not.  It shows a sequential flow and two different
        concurrent flows.  It also illustrates the use of a memoizer
        based on Java's ConcurrentHashMap.

. ex4 - This example shows how to concurrently count the number of
        images in a recursively-defined folder structure using a range
        of RxJava features.  The root folder can either reside locally
        (filesystem-based) or remotely (web-based).
