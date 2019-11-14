import utils.BigFraction;
import utils.RunTimer;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.ForkJoinUtils.*;

/**
 * This example shows how to reduce and multiply big fractions using
 * the Java fork-join pool framework.
 */
public class ex22 {
    /**
     * Number of big fractions to process via a fork-join pool.
     */
    private static int sMAX_FRACTIONS = 30;

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
        display("Starting ForkJoinTest");

        // A list of random unreduced BigFractions.
        List<BigFraction> fractionList = Stream
            // Generate a list of random unreduced BigFractions
            .generate(() -> makeBigFraction(new Random(), false))

            // Limit the size of the list.
            .limit(sMAX_FRACTIONS)

            // Collect all the BigFractions into an ArrayList.
            .collect(toList());

        // Define a BigFraction operation to run.
        Function<BigFraction, BigFraction> op = bigFraction -> BigFraction
            // Reduce the big fraction.
            .reduce(bigFraction)

            // Multiply it by the constant.
            .multiply(sBigReducedFraction);

        ForkJoinPool fjp1 = new ForkJoinPool();

        // Run all the tests.
        RunTimer.timeRun(() -> testApplyAllIter(fractionList, op, fjp1),
                         "testApplyAllIter()");
        System.out.println("applyAllIter() steal count = " 
                           + fjp1.getStealCount());

        System.gc();
        ForkJoinPool fjp2 = new ForkJoinPool();
        RunTimer.timeRun(() -> testApplyAllSplitIndex(fractionList, op, fjp2),
                         "testApplyAllSplitIndex()");
        System.out.println("applyAllSplitIndex() steal count = " 
                           + fjp2.getStealCount());

        System.gc();

        ForkJoinPool fjp3 = new ForkJoinPool();
        RunTimer.timeRun(() -> testApplyAllSplit(fractionList, op, fjp3),
                         "testApplyAllSplit()");
        System.out.println("applyAllSplit() steal count = "
                           + fjp3.getStealCount());

        System.gc();
        ForkJoinPool fjp4 = new ForkJoinPool();
        RunTimer.timeRun(() -> testInvokeAll(fractionList, op, fjp4),
                         "testInvokeAll()");
        System.out.println("invokeAll() steal count = "
                           + fjp4.getStealCount());

        System.gc();
        ForkJoinPool fjp5 = new ForkJoinPool();
        RunTimer.timeRun(() -> testApplyAllSplitIndexEx(fractionList, op, fjp5),
                "testApplyAllSplitIndexEx()");
        System.out.println("applyAllSplitIndexEx() steal count = "
                + fjp5.getStealCount());

        System.gc();
        RunTimer.timeRun(() -> testParallelStream(fractionList, op),
                "testParallelStream()");
        System.out.println("applyParallelStream() steal count = "
                + ForkJoinPool.commonPool().getStealCount());

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
    private static void testApplyAllIter(List<BigFraction> fractionList,
                                         Function<BigFraction, BigFraction> op,
                                         ForkJoinPool fjp) {
        // Test big fraction operations using applyAllIter().
        applyAllIter(fractionList, 
                     op,
                     fjp);
    }

    /**
     * Test the applyAllSplitIndex() utility method.
     */
    private static void testApplyAllSplitIndex(List<BigFraction> fractionList,
                                               Function<BigFraction, BigFraction> op,
                                               ForkJoinPool fjp) {
        // Test big fraction operations using applyAllSplitIndex().
        applyAllSplitIndex(fractionList,
                           op,
                           fjp);
    }

    /**
     * Test the applyAllSplitIndex() utility method.
     */
    private static void testApplyAllSplitIndexEx(List<BigFraction> fractionList,
                                                 Function<BigFraction, BigFraction> op,
                                                 ForkJoinPool fjp) {
        BigFraction[] results = new BigFraction[fractionList.size()];
        // Test big fraction operations using applyAllSplitIndex().
        applyAllSplitIndexEx(fractionList,
                op,
                fjp,
                results);
    }

    /**
     * Test the applyAllSplit() utility method.
     */
    private static void testApplyAllSplit(List<BigFraction> fractionList,
                                          Function<BigFraction, BigFraction> op,
                                          ForkJoinPool fjp) {
        // Test big fraction operations using applyAllSplit().
        applyAllSplit(fractionList,
                      op,
                      fjp);
    }

    /**
     * Test the invokeAll() utility method.
     */
    private static void testInvokeAll(List<BigFraction> fractionList,
                                      Function<BigFraction, BigFraction> op,
                                      ForkJoinPool fjp) {
        // Test big fraction operations using invokeAll().
        invokeAll(fractionList,
                  op,
                  fjp);
    }

    /**
     * Test the parallel stream implementation.
     */
    private static void testParallelStream(List<BigFraction> fractionList,
                                           Function<BigFraction, BigFraction> op) {
        // Test big fraction operations using a parallel stream.
        applyParallelStream(fractionList,
                            op);
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
