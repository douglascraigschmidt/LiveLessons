This director contains examples of Project Loom features.  They
require experimental versions of the JDK, such as JDK 18, which you
can download from https://jdk.java.net/loom.

Here's an overview of what's current included in these examples:

. ex1 - This example demonstrates how to create, start, and use
        virtual and platform Thread objects in Project Loom.

. ex2 - This example demonstrates Project Loom structured concurrency
        features, which enables a main task to split into several
        concurrent sub-tasks that run concurrently to competion before
        the main task can complete.

. ex3 - This example compares and contrasts the programming models and
        performance results of Java parallel streams, completable
        futures, and Project Loom structured concurrency when applied
        to download many images from a remote web server.

