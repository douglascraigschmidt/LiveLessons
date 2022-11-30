import utils.BigFraction;
import utils.RunTimer;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static tests.ForkJoinTests.*;

/**
 * This example shows how to reduce and multiply big fractions using
 * different programming models provided by the Java fork-join
 * framework.
 */
public class ex22 {
    /**
     * Number of big fractions to process via a fork-join pool.
     */
    private static final int sMAX_FRACTIONS = 100;

    /**
     * A big reduced fraction constant.
     */
    private static final BigFraction sBigReducedFraction =
        BigFraction.valueOf(new BigInteger("846122553600669882"),
                            new BigInteger("188027234133482196"),
                            true);

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) {
        display("Starting ForkJoinTest");

        // A list of random unreduced BigFractions.
        List<BigFraction> fractionList = Stream
            // Generate a list of random unreduced BigFractions
            .generate(() -> makeBigFraction(new Random(), false))

            // Limit the size of the list.
            .limit(sMAX_FRACTIONS)

            // Collect all the BigFractions into an ArrayList.
            .toList();

        // Define a BigFraction operation to run.
        Function<BigFraction, BigFraction> op = bigFraction -> BigFraction
            // Reduce the big fraction.
            .reduce(bigFraction)

            // Multiply it by the constant.
            .multiply(sBigReducedFraction);

        // Create a list of test methods.
        List<BiFunction<List<BigFraction>,
                        Function<BigFraction, BigFraction>,
                        Void>>
            testMethods = List.of(ex22::testInvokeAll,
                                  ex22::testApplyAllIter,
                                  ex22::testApplyAllSplitIndex,
                                  ex22::testApplyAllSplit,
                                  ex22::testApplyAllSplitIndexEx,
                                  ex22::testParallelStream);

        ex22.testApplyAllSplitIndexEx(fractionList, op);

        // Run each test method.
        testMethods.forEach(method -> method.apply(fractionList, op));

        // Print the results of the tests.
        display(RunTimer.getTimingResults());

        display("Finishing ForkJoinTest");
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
     * Test the applyAllIter() utility method.
     */
    private static Void testApplyAllIter(List<BigFraction> fractionList,
                                         Function<BigFraction, BigFraction> op) {
        // Run garbage collector to start in a pristine state.
        System.gc();

        // Use a try-with-resources block to enable autoclose.
        try (ForkJoinPool fjp = new ForkJoinPool()) {
            RunTimer
                // Time the testing of big fraction operations using
                // applyAllIter().
                .timeRun(() -> applyAllIter(fractionList, op, fjp),
                         "testApplyAllIter()");

            System.out.println("applyAllIter() steal count = "
                               + fjp.getStealCount());
            return null;
        }
    }

    /**
     * Test the applyAllSplitIndex() utility method.
     */
    private static Void testApplyAllSplitIndex(List<BigFraction> fractionList,
                                               Function<BigFraction, BigFraction> op) {
        // Run garbage collector to start in a pristine state.
        System.gc();

        // Use a try-with-resources block to enable autoclose.
        try (ForkJoinPool fjp = new ForkJoinPool()) {
            RunTimer
                // Test big fraction operations using applyAllSplitIndex().
                .timeRun(() -> applyAllSplitIndex(fractionList, op, fjp),
                         "testApplyAllSplitIndex()");

            System.out.println("applyAllSplitIndex() steal count = "
                               + fjp.getStealCount());
            return null;
        }
    }

    /**
     * Test the applyAllSplitIndexEx() utility method.
     */
    private static Void testApplyAllSplitIndexEx(List<BigFraction> fractionList,
                                                 Function<BigFraction, BigFraction> op) {
        // Run garbage collector to start in a pristine state.
        System.gc();

        // Use a try-with-resources block to enable autoclose.
        try (ForkJoinPool fjp = new ForkJoinPool()) {
            BigFraction[] results = new BigFraction[fractionList.size()];

            RunTimer
                // Time testing of big fraction operations using
                // applyAllSplitIndex().
                .timeRun(() -> applyAllSplitIndexEx(fractionList, op, fjp, results),
                         "testApplyAllSplitIndexEx()");

            System.out.println("applyAllSplitIndexEx() steal count = "
                               + fjp.getStealCount());
            return null;
        }
    }

    /**
     * Test the applyAllSplit() utility method.
     */
    private static Void testApplyAllSplit(List<BigFraction> fractionList,
                                          Function<BigFraction, BigFraction> op) {
        // Run garbage collector to start in a pristine state.
        System.gc();

        // Use a try-with-resources block to enable autoclose.
        try (ForkJoinPool fjp = new ForkJoinPool()) {
            RunTimer
                // Time testing of big fraction operations using
                // applyAllSplit().
                .timeRun(() -> applyAllSplit(fractionList, op, fjp),
                         "testApplyAllSplit()");

            System.out.println("applyAllSplit() steal count = "
                               + fjp.getStealCount());
            return null;
        }
    }

    /**
     * Test the invokeAll() utility method.
     */
    private static Void testInvokeAll(List<BigFraction> fractionList,
                                      Function<BigFraction, BigFraction> op) {
        // Run garbage collector to start in a pristine state.
        System.gc();

        // Use a try-with-resources block to enable autoclose.
        try (ForkJoinPool fjp1 = new ForkJoinPool()) {
            RunTimer
                // Time the testing of big fraction operations using invokeAll().
                .timeRun(() -> invokeAll(fractionList, op, fjp1),
                         "testInvokeAll()");

            System.out.println("invokeAll() steal count = "
                               + fjp1.getStealCount());
            return null;
        }
    }

    /**
     * Test the parallel stream implementation.
     */
    private static Void testParallelStream(List<BigFraction> fractionList,
                                           Function<BigFraction, BigFraction> op) {
        // Run garbage collector to start in a pristine state.
        System.gc();

        RunTimer
            // Time testing of big fraction operations using a
            // parallel stream.
            .timeRun(() -> applyParallelStream(fractionList, op),
                     "testParallelStream()");

        System.out.println("applyParallelStream() steal count = "
                           + ForkJoinPool.commonPool().getStealCount());

        return null;
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
