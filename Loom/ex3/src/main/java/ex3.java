import jdk.incubator.concurrent.StructuredTaskScope;
import utils.BigFraction;
import utils.BigFractionUtils;
import utils.Options;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static utils.BigFractionUtils.makeBigFraction;
import static utils.BigFractionUtils.sBigReducedFraction;
import static utils.RandomUtils.generateRandomBigFractions;

/**
 * This example demonstrates Java 19 Java structured concurrency
 * features, which enables a main task to split into several
 * concurrent sub-tasks that run concurrently to completion before the
 * main task can complete.  Java 19 supports structured concurrency
 * via the {@link StructuredTaskScope} class, which supports
 * AutoCloseable and defines several nested classes (such as {@link
 * StructuredTaskScope.ShutdownOnFailure} and {@link
 * StructuredTaskScope.ShutdownOnSuccess}) that supports structured
 * concurrency.  You'll need to install JDK 19 (or beyond) with gradle
 * version 7.6 (or beyond) configured to run this example.
 */
public class ex3 {
    /**
     * Main entry point into the test program.
     */
    public static void main(String[] argv)
        throws ExecutionException, InterruptedException {
        System.out.println("Entering test");

        // Initialize any command-line options.
        Options.instance().parseArgs(argv);

        // Demonstrate Java structured concurrency.
        demoStructuredConcurrency(Options.instance().numberOfElements());

        System.out.println("Leaving test");
    }

    /**
     * Demonstrates Java structured concurrency features by generating
     * {@code count} random and unreduced {@link BigFraction} objects
     * and then concurrently reducing and multiplying them with a
     * large constant {@link BigFraction}.
     *
     * @param count The number of random and unreduced {@link
     *              BigFraction} objects to generate
     */
    public static void demoStructuredConcurrency(int count) {
        // Create a new scope to execute virtual threads.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Create a List of Future<BigFraction> to hold the
            // results.
            var results = new ArrayList<Future<BigFraction>>();

            // Iterate through all the random BigFraction objects.
            for (var bigFraction : generateRandomBigFractions(count))
                results
                    // Add the Future<BigFraction> to the ist.
                    .add(scope
                         // Fork a new virtual thread to reduce and
                         // multiply the BigFraction concurrently.
                         .fork(() ->
                               reduceAndMultiply(bigFraction,
                                                 sBigReducedFraction)));

            // This barrier synchronizer waits for all threads to
            // finish or throw any exception that occurred.
            scope.join().throwIfFailed();

            // Sort and print the results.
            BigFractionUtils.sortAndPrintList(results);

            // Don't exit the try-with-resources scope until all
            // concurrently executing virtual threads complete.
        } catch (Exception exception) {
            System.out.println("Exception: " 
                               + exception.getMessage());
        }
    }

    /**
     * Return the result of reducing {@link BigFraction} {@code bf1}
     * and multiplying it with {@link BigFraction} {@code bf2}.
     *
     * @param bf1 The {@link BigFraction} to reduce and then multiply
     *            with {@code bf2}
     * @param bf2 The {@link BigFraction} to multiply with {@code bf1}
     * @return The results of reducing {@code bf1} and then
     *         multiplying it with {@code bf2}
     */
    private static BigFraction reduceAndMultiply(BigFraction bf1,
                                                 BigFraction bf2) {
        return BigFraction
            // Reduce bf1.
            .reduce(bf1)
            
            // Multiple bf1 and bf2.
            .multiply(bf2);
    }
}
