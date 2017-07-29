import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.math.BigInteger;

/**
 * This example shows the use of the Java 8 completable future
 * framework to concurrently compute the greatest common divisor (GCD)
 * of two BigIntegers using several techniques.
 */
public class ex8 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        // Test a GCD computation using a CompletableFuture and an
        // explicit Java Thread.
        testGCDThread();

        // Test a GCD computation using a CompletableFuture and its
        // various methods, such as supplyAsync() and thenAccept().
        testGCDFancy();
    }

    /**
     * Test a GCD computation using a CompletableFuture and an
     * explicit Java Thread.
     */
    private static void testGCDThread() {
        CompletableFuture<BigInteger> future =
            new CompletableFuture<>();

        // Create and start a thread whose runnable lambda computes
        // the GCD of two big integers.
        new Thread (() -> {
                BigInteger bi1 =
                    new BigInteger("188027234133482196");
                BigInteger bi2 =
                    new BigInteger("2434101");
                    
                // Complete the future once the computation is
                // finished.
                future.complete(bi1.gcd(bi2));
        }).start();

        // Print the result, blocking until it is ready.
        System.out.println("GCD = " + future.join());  
    }

    /**
     * Test a GCD computation using a CompletableFuture and its
     * various methods, such as supplyAsync() and thenAccept().
     */
    private static void testGCDSupplyAsync() {
        CompletableFuture
            // Initiate an async task whose runnable lambda computes
            // the GCD of two big integers.
            .supplyAsync(() -> {
                BigInteger bi1 =
                    new BigInteger("188027234133482196");

                BigInteger bi2 =
                    new BigInteger("2434101");
                    
                // Return the GCD result, which is converted into a
                // CompletableFuture<BigInteger>.
                return bi1.gcd(bi2);
                })

            // This completion stage method is dispatched after the
            // GCD of the big integer completes.
            .thenAccept(bigInteger
                       // Print the result when it's computed.
                       -> System.out.println("GCD = " + bigInteger))

            // Wait until the computation is done.
            .join();
    }
}
