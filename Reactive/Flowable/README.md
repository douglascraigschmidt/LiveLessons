The folders in this directory demonstrate various RxJava Observable
(and Single) features, as follows:

. ex1 - This program applies RxJava Flowable features to demonstrate
        various types of backpressure strategies (e.g., MISSING,
        BUFFER, ERROR, LATEST, and DROP) between a Subscriber and a
        non-backpressure-aware Publisher that run in the context of
        different Scheduler2 objects.

. ex2 - This program applies RxJava Flowable features to demonstrate
        how a Subscriber running in one Scheduler context can exert
        flow-control on a backpressure-aware Publisher that runs in a
        different Scheduler context.

. ex3 - This example shows how to multiply and add big fractions
        asynchronously and concurrently using RxJava Flowable
        operators, including fromArray() and parallel(), and
        ParallelFlowable operators, including runOn(), flatMap(),
        sequential(), and reduce(), as well as the
        Schedulers.computation() thread pool.
        
. ex4 - This example shows how to apply RxJava features to download
        and store images from remote web servers by showcasing a range
        of Flowable operators (such as fromIterator(), parallel(), and
        collect()), ParallelFlowable operators (such as runOn(),
        map(), and sequential()), and Single operators (such as
        doOnSuccess() and ignoreElement()), as well as the
        Schedulers.io() thread pool.

. ex5 - This example shows how to apply timeouts with the asynchronous
        Single and ParallelFlowable classes in the RxJava framework.

