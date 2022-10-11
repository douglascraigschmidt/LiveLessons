package tests;

import utils.BigFraction;

import java.util.concurrent.*;

import static utils.BigFractionUtils.*;

/**
 * Tests that showcase how the Java completable futures framework
 * works with Java threads.
 */
public final class ThreadTests {
    /**
     * Test the use of a BigFraction constant using basic features of
     * a CompletableFuture and an explicit Java Thread.
     */
    public static CompletableFuture<Void> testFractionConstantThread() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionConstantThread()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Create and start a thread whose runnable lambda 
        // sets the future to a constant.
        new Thread (() -> {
                // Set future to a constant.
                future.complete(mBigReducedFractionFuture.join());
        }).start();

        // Do something interesting here...

        // Print the result, blocking until it's ready.
        sb.append("     Thread result = "
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using basic features of a
     * CompletableFuture and an explicit Java Thread.
     */
    public static CompletableFuture<Void> testFractionMultiplicationThread() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationThread()\n");

        // Create an empty completable future.
        CompletableFuture<BigFraction> future =
            new CompletableFuture<>();

        // Create and start a thread whose runnable lambda multiplies
        // two large fractions.
        new Thread (() -> {
                BigFraction bf1 =
                    new BigFraction(sF1);
                BigFraction bf2 =
                    new BigFraction(sF2);
                    
                // Complete the future once the computation is
                // finished.
                future.complete(bf1.multiply(bf2));
        }).start();

        // Do something interesting here...

        // Print the result, blocking until it's ready.
        sb.append("     Thread result = "
                  + future.join().toMixedString());
        display(sb.toString());

        return sCompleted;
    }

    /**
     * Test BigFraction multiplication using a Callable, Future, and
     * the common fork-join pool.
     */
    public static CompletableFuture<Void> testFractionMultiplicationCallable() {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplicationCallable()\n");

        try {
            // Create a callable that multiplies two large Big fractions.
            Callable<BigFraction> call = () -> {
                BigFraction bf1 = new BigFraction(sF1);
                BigFraction bf2 = new BigFraction(sF2);

                // Return the result of multiplying the fractions.
                return bf1.multiply(bf2);
            };

            // Submit the call to the common fork-join pool and store
            // the future it returns.
            Future<BigFraction> future =
                ForkJoinPool.commonPool().submit(call);

            // Do something interesting here...

            // Block until the result is available.
            BigFraction result = future.get();

            sb.append("     Callable.call() = "
                      + result.toMixedString());
            display(sb.toString());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return sCompleted;
    }
}
