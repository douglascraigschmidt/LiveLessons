The folders in this directory demonstrate various Project Reactor Flux
features, as follows:

. ex1 - This example shows how to apply Project Reactor features
        synchronously to perform basic Flux operations, including
        just(), map(), and subscribe().

. ex2 - This example shows how to reduce and/or multiply big fractions
        asynchronously using Flux features in the Reactor framework,
        including create(), interval(), map(), doOnNext(), take(), and
        subscribe().

. ex3 - This example shows how to reduce and/or multiply big fractions
        asynchronously and concurrently using many advanced Flux
        features in the Project Reactor framework, including just(),
        create(), map(), flatMap(), collectList(), subscribeOn(),
        take(), and various types of thread pools.  It also shows
        advanced Mono operations, such as first() and when().  It also
        demonstrates how to combine the Java streams framework with
        the Project Reactor framework.
