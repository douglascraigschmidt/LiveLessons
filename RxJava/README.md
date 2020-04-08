This directory contains all the RxJava source code examples from my
webinars on [Modern Java by
Example](http://www.dre.vanderbilt.edu/~schmidt/MJBE).  Here's an
overview of what's included:

. ex1 - This example is currently just a placeholder for random things
        I'm playing around with for RxJava.
  
. ex2 - This example downloads multiple images from a remote web
        server via several different mechanisms, including Java
        parallel streams and RxJava.  It also compares the performance
        of Java parallel streams and RxJava with and without the
        ForkJoinPool.ManagedBlocker interface and the Java common
        fork-join pool.

. ex3 - This example demonstrates various RxJava mechanisms for
        determining if a flow of random big integers are prime numbers
        or not.  It shows a sequential flow and two different
        concurrent flows.  It also illustrates the use of a memoizer
        based on Java's ConcurrentHashMap.
