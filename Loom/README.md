This director contains examples of Java's structured concurrency
features.  They require recent versions of the JDK, such as JDK 19 or
later, as well as gradle 7.6 or later.

Here's an overview of what's current included in these examples:

. ex1 - This example demonstrates how to create, start, and use
         virtual and platform Thread objects in Java 19, which
         contains an implementation of lightweight user-mode threads
         (virtual threads).

. ex2 - This example demonstrates Java 19 structured concurrency
        features, which enable a main task to split into several
        concurrent sub-tasks that run concurrently to completion
        before the main task can complete.  Java 19 supports
        structured concurrency by enhancing ExecutorService to support
        AutoCloseable and updating Executors to define new static
        factory methods that support usage in a structured manner.

. ex3 - This example demonstrates Java 19 preview structured
        concurrency features, which enables a main task to split into
        several concurrent sub-tasks that run concurrently to
        completion before the main task can complete.  The Java 19
        preview supports structured concurrency via the
        StructuredTaskScope class, which supports AutoCloseable and
        defines several nested classes (such as
        StructuredTaskScope.ShutdownOnFailure) that supports
        structured concurrency.

. ex4 - This example compares and contrasts the programming models and
        performance results of Java parallel streams, completable
        futures, Project Reactor, RxJava, and Java structured
        concurrency when applied to download, transform, and store
        many images from a remote web server.

