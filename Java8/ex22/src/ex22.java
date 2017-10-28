import utils.BigFraction;
import utils.RunTimer;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.math.BigInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.ExceptionUtils.rethrowFunction;
import static utils.ForkJoinUtils.*;

/**
 * This example shows how to reduce and/or multiply big fractions
 * using the Java fork-join pool framework.
 */
public class ex22 {
    /**
     * Number of big fractions to process via a fork-join pool.
     */
    private static int sMAX_FRACTIONS = 10;

    /**
     * A big reduced fraction constant.
     */
    private static BigFraction sBigReducedFraction = 
        BigFraction.valueOf(new BigInteger("846122553600669882"),
                            new BigInteger("188027234133482196"),
                            true);

    /**
     * Stores a completed future with a BigFraction value of
     * sBigReducedFraction.
     */
    private static CompletableFuture<BigFraction> mBigReducedFractionFuture =
        CompletableFuture.completedFuture(sBigReducedFraction);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) throws IOException {
        List<BigFraction> fractionList = Stream
            // Generate sMAX_FRACTIONS random unreduced BigFractions.
            .generate(() -> makeBigFraction(new Random(), false))
            .limit(sMAX_FRACTIONS)
            .collect(toList());

        Function<BigFraction, BigFraction> op =
            (bigFraction) -> BigFraction
                .reduce(bigFraction)
                .multiply(sBigReducedFraction);

        // Warm up the thread pool so the results are more accurate.
        warmUpThreadPool(fractionList, op);

        System.gc();
        RunTimer.timeRun(() -> testFractionOperations1(fractionList, op),
                         "testFractionOperations1()");
        System.gc();
        RunTimer.timeRun(() -> testFractionOperations2(fractionList, op),
                         "testFractionOperations2()");
        System.gc();
        RunTimer.timeRun(() -> testFractionOperations3(fractionList, op),
                         "testFractionOperations3()");
        System.gc();
        RunTimer.timeRun(() -> testFractionOperations4(fractionList, op),
                         "testFractionOperations4()");


        display(RunTimer.getTimingResults());
    }

    /**
     * Warm up the thread pool so the results are more accurate.
     */
    private static void warmUpThreadPool(List<BigFraction> fractionList,
                                         Function<BigFraction, BigFraction> op) {
        System.out.println("Warming up the thread pool");
        // Test big fraction multiplication using ...
        List<BigFraction> results = applyAllSplitIndex(fractionList, op);
    }

    /**
     * Test the applyAllIter() utility method.
     */
    private static void testFractionOperations1(List<BigFraction> fractionList,
                                                    Function<BigFraction, BigFraction> op) {
        // Test big fraction operations using applyAppIter().
        List<BigFraction> results = applyAllIter(fractionList, op);
    }

    /**
     * Test the invokeAll() utility method.
     */
    private static void testFractionOperations2(List<BigFraction> fractionList,
                                                    Function<BigFraction, BigFraction> op) {    
        // Test big fraction operations using invokeAll()
        List<Future<BigFraction>> results = invokeAll(fractionList, op);
    }

    /**
     * Test the applyAllSplit() utility method.
     */
    private static void testFractionOperations3(List<BigFraction> fractionList,
                                                    Function<BigFraction, BigFraction> op) {    
        // Test big fraction operations using applyAllSplit().
        List<BigFraction> results = applyAllSplit(fractionList, op);

    }

    /**
     * Test the applyAllSplitIndex() utility method.
     */
    private static void testFractionOperations4(List<BigFraction> fractionList,
                                                    Function<BigFraction, BigFraction> op) {    
        // Test big fraction operations using applyAllSplitIndex().
        List<BigFraction> results = applyAllSplitIndex(fractionList, op);
    }

    /**
     * A factory method that returns a large random BigFraction whose
     * creation is performed synchronously.
     *
     * @param random A random number generator
     * @param reduced A flag indicating whether to reduce the fraction or not
     * @return A large random BigFraction
     */
    private static BigFraction makeBigFraction(Random random,
                                               boolean reduced) {
        // Create a large random big integer.
        BigInteger numerator =
            new BigInteger(150000, random);

        // Create a denominator that's between 1 to 10 times smaller
        // than the numerator.
        BigInteger denominator =
            numerator.divide(BigInteger.valueOf(random.nextInt(10) + 1));

        // Return a big fraction.
        return BigFraction.valueOf(numerator,
                                   denominator,
                                   reduced);
    }

    /**
     * Display the {@code string} after prepending the thread id.
     */
    private static void display(String string) {
        System.out.println("["
                           + Thread.currentThread().getId()
                           + "] "
                           + string);
    }
}
