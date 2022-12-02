This director contains examples of Java's structured concurrency
features.  They require recent versions of the JDK, such as JDK 19 or
later.

Here's an overview of what's current included in these examples:

. ex1 - This example demonstrates how to create, start, and use
        virtual and platform Thread objects.

. ex2 - This example demonstrates Java 19 structured concurrency
        features, which enables a main task to split into several
        concurrent sub-tasks that run concurrently to competion before
        the main task can complete.

. ex3 - This example demonstrates Java 20 structured concurrency
        features, which enables a main task to split into several
        concurrent sub-tasks that run concurrently to competion before
        the main task can complete.

. ex4 - This example compares and contrasts the programming models and
        performance results of Java parallel streams, completable
        futures, Project Reactor, and Java structured concurrency
        programming models when applied to download many images from a
        remote web server.

