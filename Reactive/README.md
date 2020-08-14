This directory contains all the Project Reactor source code examples
from my LiveTraining webinars and [courses at
Vanderbilt](http://www.dre.vanderbilt.edu/~schmidt/DigitalLearning).
Here's an overview of what's included:

. Mono - These examples show how to reduce and/or multiply big
         fractions asynchronously and concurrently using many Mono
         features in the Project Reactor framework, including
         fromCallable(), just(), doOnSuccess(), zipWith(), first(),
         when(), subscribeOn(), then(), and various thread pools.

. Flux - These examples show how to reduce and/or multiply big
         fractions asynchronously and concurrently using many Flux
         features in the Project Reactor framework, including
         fromIterable(), just(), create(), doOnNext(), map(),
         flatMap(),take(), interval(), subscribe(), subscribeOn(),
         collectList(), and various thread pools.  It also
         demonstrates how to combine the Java streams framework with
         the Project Reactor framework.

. ImageCounter - This example shows how to asynchronously and
         concurrently count the number of images in a recursively-
         defined folder structure using a range of Project Reactor
         features, including Mono features (e.g., just(), block(),
         doOnSuccess(), map(), transformDeferred(), subscribeOn(),
         flatMap(), zipWith(), defaultIfEmpty()s) and Flux features
         (e.g., fromIterable(), flatMap(), reduce()).  The root folder
         can either reside locally (filesystem-based) or remotely
         (web-based).
