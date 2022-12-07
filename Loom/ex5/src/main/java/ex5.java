import common.CompletableFutureEx;
import common.Options;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.RunTimer;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

import static common.ListOfFuturesCollector.toFuture;
import static utils.BigFractionUtils.sBigReducedFraction;
import static utils.BigFractionUtils.sortAndPrintList;

/**
 * This example shows how to reduce and/or multiply BigFraction
 * objects via the Java completable futures framework.  It also shows
 * how to customize the Java completable futures framework to use
 * arbitrary {@link Executor} objects, including the new {@link
 * Executors#newVirtualThreadPerTaskExecutor} provided in Java 19.
 * You'll need to install JDK 19 (or beyond) with gradle version 7.6
 * configured to run this example.
 */
public class ex5 {
    /**
     * The main entry point into this program.
     */
    static public void main(String[] argv) throws Exception {
        System.out.println("Entering test");

        // Parse the command-line arguments.
        Options.instance().parseArgs(argv);

        // Run the tests.
        runTests();

        System.out.println(RunTimer.getTimingResults());
        System.out.println("Leaving test");
    }

    /**
     * Run all the tests.
     */
    private static void runTests() {
        // Generate a List of random unreduced BigFraction objects.
        var list = BigFractionUtils
                .generateRandomBigFractions(Options.instance().getCount(),
                        false);

        // Run a test using the default virtual threads Executor.
        RunTimer.timeRun(() ->
                        testFractionMultiplications(list).join(),
                "CompletableFutureEx with virtual threads");

        // Run a test using the common fork-join pool executor.
        CompletableFutureEx.setExecutor(ForkJoinPool.commonPool());

        RunTimer.timeRun(() ->
                        testFractionMultiplications(list).join(),
                "CompletableFutureEx with common fork-join pool");
    }

    /**
     * Test BigFraction reductions and multiplications using a stream
     * of CompletableFutures and a chain of completion stage methods
     * involving supplyAsync(), thenComposeAsync(), and
     * acceptEither().
     */
    public static CompletableFuture<Void> testFractionMultiplications
        (List<BigFraction> bigFractionList) {
        StringBuilder sb =
            new StringBuilder(">> Calling testFractionMultiplications()\n");

        // Function asynchronously reduces/multiplies a BigFraction.
        Function<BigFraction,
                 CompletableFuture<BigFraction>> reduceAndMultiplyFraction =
            sUnreducedFraction -> CompletableFutureEx
            // Perform BigFraction reduction asynchronously.
            .supplyAsync(() -> BigFraction.reduce(sUnreducedFraction))

            // Return a CompletableFuture to a BigFraction that's
            // being multiplied asynchronously, which may run for a
            // while.
            .thenApplyAsync(reducedFraction -> reducedFraction
                            .multiply(sBigReducedFraction));

        sb.append("     Printing sorted results:");

        // Return a CompletableFuture to a List of sorted random
        // BigFraction objects that were reduced and multiplied
        // concurrently.
        return bigFractionList
            // Convert the List into a Stream.
            .stream()

            // Reduce and multiply each BigFraction asynchronously.
            .map(reduceAndMultiplyFraction)

            // Trigger intermediate operation processing and return a
            // future to a List of BigFraction objects that are being
            // reduced and multiplied asynchronously.
            .collect(toFuture())

            // After all the BigFraction reductions have completed
            // asynchronously sort and print the results.
            .thenCompose(list ->
                         sortAndPrintList(list,
                                          sb));
    }
}

