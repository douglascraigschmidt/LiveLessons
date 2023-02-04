import tests.ExceptionTests;
import tests.MultiTriggerTests;
import tests.SingleTriggerTests;
import tests.ThreadTests;
import utils.AsyncTester;
import utils.BigFraction;

import java.util.concurrent.CompletableFuture;

/**
 * This example shows how to reduce and/or multiply {@link
 * BigFraction} objects using a wide range of features in the Java
 * completable futures framework, including various factory methods,
 * completion stage methods, arbitrary-arity methods, and exception
 * handling methods.  This example also shows how to combine the Java
 * completable futures framework with the Java (sequential) streams
 * framework.
 */
public class ex8 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Tests that showcase how the completable futures framework
        // works with Java threads.
        runThreadTests();

        // Tests that showcase CompletableFuture completion stage
        // methods that trigger after a single future completes.
        runSingleTriggerTests();

        // Tests that showcase CompletableFuture completion stage
        // methods that trigger after multiple futures complete.
        runMultiTriggerTests();

        // Tests that showcase CompletableFuture completion stage
        // methods that handle exceptions.
        runExceptionTests();
    }

    /**
     * Tests that showcase how the Java completable futures framework
     * works with Java threads.
     */
    private static void runThreadTests() {
        System.out.println("entering runThreadTests()");

        var asyncTester = new AsyncTester();

        // Test the use of a BigFraction constant using basic features
        // of a CompletableFuture and an explicit Java Thread.
        asyncTester.register(ThreadTests::testFractionConstantThread);

        // Test BigFraction multiplication using basic features of
        // CompletableFuture and an explicit Java Thread.
        asyncTester.register(ThreadTests::testFractionMultiplicationThread);

        // Test BigFraction multiplication using a Callable, Future,
        // and the common fork-join pool.
        asyncTester.register(ThreadTests::testFractionMultiplicationCallable);

        asyncTester
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();

        System.out.println("leaving runThreadTests()");
    }

    /**
     * Tests that showcase {@link CompletableFuture} completion stage
     * methods that trigger after a single future completes.
     */
    private static void runSingleTriggerTests() {
        System.out.println("entering runSingleTriggerTests()");

        var asyncTester = new AsyncTester();

        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage "normal" (i.e., non-*Async())
        // methods.
        asyncTester.register(SingleTriggerTests::testFractionReduction);

        // Test BigFraction reduction using a CompletableFuture and a
        // chain of completion stage *Async() methods.
        asyncTester.register(SingleTriggerTests::testFractionReductionAsync);

        // Test BigFraction multiplication using a CompletableFuture and
        // its runAsync() and join() methods.
        asyncTester.register(SingleTriggerTests::testFractionMultiplicationRunAsync);

        // Test BigFraction multiplication using a CompletableFuture and
        // its supplyAsync() factory method and join() method.
        asyncTester.register(SingleTriggerTests::testFractionMultiplicationSupplyAsync);

        // Test BigFraction multiplication using a CompletableFuture and
        // its completeAsync() factory method and join() method.
        asyncTester.register(SingleTriggerTests::testFractionMultiplicationCompleteAsync);

        asyncTester
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();

        System.out.println("leaving runSingleTriggerTests()");
    }

    /**
     * Tests that showcase {@link CompletableFuture} completion stage
     * methods that trigger after multiple futures complete.
     */
    private static void runMultiTriggerTests() {
        System.out.println("entering runMultiTriggerTests()");

        var asyncTester = new AsyncTester();

        // Test big fraction multiplication and addition using a
        // supplyAsync() and thenCombine().
        asyncTester.register(MultiTriggerTests::testFractionCombine);

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenCompose(), and acceptEither().
        asyncTester.register(MultiTriggerTests::testFractionMultiplications1);

        // Test big fraction multiplication using a stream of
        // CompletableFutures and a chain of completion stage methods
        // involving supplyAsync(), thenComposeAsync(), and
        // acceptEither().
        asyncTester.register(MultiTriggerTests::testFractionMultiplications2);

        asyncTester
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();

        System.out.println("leaving runMultiTriggerTests()");
    }

    /**
     * Tests that showcase {@link CompletableFuture} completion stage
     * methods that handle exceptions.
     */
    private static void runExceptionTests() {
        System.out.println("entering runExceptionTests()");

        var asyncTester = new AsyncTester();

        // Test BigFraction exception handling using
        // CompletableFutures and the handle() method.
        asyncTester.register(ExceptionTests::testFractionExceptions1);
        
        // Test BigFraction exception handling using
        // CompletableFutures and the exceptionally() method.
        asyncTester.register(ExceptionTests::testFractionExceptions2);

        // Test BigFraction exception handling using
        // CompletableFutures and the whenComplete() method.
        asyncTester.register(ExceptionTests::testFractionExceptions3);

        asyncTester
            // Run all the asynchronous tests.
            .runTests()

            // Block until all the tests are done to allow future
            // computations to complete running asynchronously.
            .join();

        System.out.println("leaving runExceptionTests()");
    }
}
