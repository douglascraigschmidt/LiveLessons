The folders in this directory demonstrate various RxJava Observable
(and Single) features, as follows:

. ex1 - This example shows how to apply RxJava features synchronously
        to perform basic Observable operations, including just(),
        map(), and blockingSubscribe().

. ex2 - This example shows how to apply RxJava features asynchronously
        to perform various Observable operations, including create(),
        interval(), map(), filter(), doOnNext(), doOnComplete(),
        take(), subscribe(), range(), subscribeOn(), observeOn(),
        count(), and various thread pools.

. ex3 - This example shows how to reduce and/or multiply big fractions
        asynchronously and concurrently using many RxJava Observable
        operations, including fromArray(), map(), generate(), take(),
        flatMap(), fromCallable(), filter(), reduce(), collectInto(),
        subscribeOn(), onErrorReturn(), onErrorResumeNext(), and
        Schedulers.computation().  It also shows RxJava Single and
        Maybe operations, including fromCallable(), ambArray(),
        flatMapCompletable(), subscribeOn(), ignoreElement(), and
        doOnSuccess().

. ex4- This example shows how to apply RxJava features asynchronously
       to perform a range of Observable operations, including
       fromArray(), fromCallable(), doOnNext(), map(), flatMap(),
       subscribeOn(), toFlowable(), subscribe(), and a parallel thread
       pool.  It also shows the Flowable subscribe() operation.  In
       addition it shows various Single operations, such as
       zipArray(), ambArray(), subscribeOn(), flatMapObservable(),
       flatMapCompletable(), ignoreElement(), flatMap(), and a
       parallel thread pool.  It also shows how to combine the Java
       Streams framework with the RxJava framework.
