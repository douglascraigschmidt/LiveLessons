import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.math.BigInteger;

/**
 * This example shows the use of a CompletableFuture to concurrently
 * compute the greatest common divisor (GCD) of two BigIntegers.
 */
public class ex8 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        testCF();
    }

    /**
     * Test a GCD computation using a CompletableFuture.
     */
    private static void testGCD() {
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
}
