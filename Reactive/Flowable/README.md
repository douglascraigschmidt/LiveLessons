The folders in this directory demonstrate various RxJava Observable
(and Single) features, as follows:

. ex2 - This example shows how to reduce and/or multiply big fractions
        asynchronously and concurrently using RxJava Flowable
        operators, including fromArray() and parallel(), and
        ParallelFlowable operators, including runOn(), flatMap(),
        sequential(), and reduce(), as well as the
        Schedulers.computation() thread pool.
        
. ex3 - This example shows how to apply RxJava features to download
        and store images from remote web servers.  In particular, it
        showcases a range of Flowable operators (such as
        fromIterator(), parallel(), and collect()), ParallelFlowable
        operators (such as runOn(), map(), and sequential()), and
        Single operators (such as doOnSuccess() and ignoreElement()),
        as well as the Schedulers.io() thread pool.

. ex4 - This example shows how to apply timeouts with the asynchronous
        Single and ParallelFlowable classes in the RxJava framework.

