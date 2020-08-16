The folders in this directory demonstrate various RxJava Observable
(and Single) features, as follows:

. ex1 - This example shows how to apply RxJava features synchronously
        to perform basic Observable operations, including just(),
        map(), and blockingSubscribe().

. ex2 - This example shows how to apply RxJava features asynchronously
        to perform various Observable operations, including create(),
        interval(), map(), filter(), doOnNext(), doOnComplete(),
        take(), subscribe(), ignoreElement(), range(), subscribeOn(),
        observeOn(), count(), and various thread pools.

. ex3 - This example shows how to reduce and/or multiply big fractions
        asynchronously and concurrently using many advanced RxJava
        Observable operations, including fromIterable(), map(),
        create(), flatMap(), filter(), collectList(), subscribeOn(),
        take(), and various types of thread pools.  It also shows
        advanced RxJava Single operations, such as first(), when(),
        flatMap(), subscribeOn(), and the parallel thread pool.

