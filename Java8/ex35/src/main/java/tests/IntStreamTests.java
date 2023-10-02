package tests;

import utils.Options;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import static utils.ParallelCheckerUtils.*;

/**
 * This class contains tests that demonstrate the performance
 * differences between {@code flatMap()}, {@code map()}, and {@code
 * mapMulti()} intermediate operations in Java parallel streams when
 * applied to {@link IntStream}.
 */
public final class IntStreamTests {
    /**
     * Demonstrate how the {@code flatMap()} intermediate operation
     * doesn't scale in a Java parallel stream.
     */
    public static void testFlatMap() {
        System.out.println("Entering IntStreamTests::testFlatMap()");

        var wasSequential =
            // Generate an outer Stream that emits the designated
            // number of int values in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply flatMap(), which doesn't scale for parallel
            // streams since it runs sequentially!
            .flatMap(IntStreamTests::innerStream)

            // Return true if all results were sequential, else false.
            .allMatch(value -> value == sSequentialResult);

        // Print the results.
        System.out.println("Leaving IntStreamTests::testFlatMap(), which ran "
                           + (wasSequential ? "sequentially" : "in parallel"));
    }

    /**
     * Demonstrate how {@code map()} scales much better than {@code flatMap()}
     * in a Java parallel stream when applied to {@link IntStream}.
     */
    public static void testMap() {
        System.out.println("Entering IntStreamTest::testMap()");

        var wasSequential =
            // Generate an outer Stream that emits the designated
            // number of Integer objects in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply map(), which works scalably for parallel streams
            // since it runs tasks in parallel.
            .map(integer -> IntStreamTests
                 // Create an IntStream that indicates if it ran
                 // in parallel or sequentially.
                 .innerStream(integer)

                 // Sum the results.
                 .sum())

            // Return true if all results were sequential, else false.
            .allMatch(value -> value == sSequentialResult);

        // Print the results.
        System.out.println("Leaving IntStreamTest::testMap(), which ran "
                           + (wasSequential ? "sequentially" : "in parallel"));
    }

    /**
     * Demonstrate how the {@code mapMulti()} intermediate operation
     * scales better than {@code flatMap()} in a Java parallel stream
     * when applied to {@link IntStream}.
     */
    public static void testMapMulti() {
        System.out.println("Entering IntStreamTests::testMapMulti()");

        var wasSequential =
            // Generate an outer Stream that emits the designated
            // number of Integer objects in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply mapMulti(), which works scalably for parallel streams
            // since it runs tasks in parallel.
            .mapMulti(IntStreamTests::innerStreamEx)

            // Return true if all results were sequential, else false.
            .allMatch(value -> value == sSequentialResult);

        // Print the results.
        System.out.println("Leaving IntStreamTests::testMultiMap(), which ran "
                           + (wasSequential ? "sequentially" : "in parallel"));
    }

    /**
     * Return an outer {@link IntStream} that emits {@link Integer}
     * objects from 1 to {@code outerCount}.
     *
     * @param outerCount The number of iterations to perform
     * @return An outer {@link IntStream} that generates {@code
     *         outerCount} {@code int} values in parallel
     */
    private static IntStream generateOuterStream(int outerCount) {
        return IntStream
            // Create a Stream of ints from 1 to outerCount.
            .rangeClosed(1, outerCount)

            // Convert the stream to a parallel stream.
            .parallel()

            // Print the outer thread id to aid result analysis.
            .peek(iteration -> Options
                  .display("outer thread id for iteration "
                           + iteration
                           + " = "
                           + Thread.currentThread().threadId()));
    }

    /**
     * Return an inner {@link IntStream} that emits {@code int}
     * values indicating whether the inner {@link IntStream} ran
     * sequentially or in parallel.
     *
     * @param innerCount The number of iterations to perform
     * @return An inner {@link IntStream} that emits {@code int}
     *         values {@code sParallelResult} and/or {@code
     *         sSequentialResult}, depending on the context in which
     *         {@code innerStream()} is called
     */
    private static IntStream innerStream(Integer innerCount) {
        // Store the outer thread id.
        var outerThreadId = Thread.currentThread().threadId();

        // Create an AtomicInteger that counts the number of integers
        // processed by each thread.
        var innerThreadIdCounter = new AtomicInteger();

        // Create a ConcurrentHashMap that associates thread ids with
        // a count of the number of integers processed by each thread.
        Map<Long, AtomicInteger> threadMap = new ConcurrentHashMap<>();

        return IntStream
            // Generate ints from 1 to innerCount iterations.
            .rangeClosed(1, innerCount)

            // Attempt to run each computation in parallel.
            .parallel()

            // Determine if each computation ran in parallel.
            .map(__ ->
                 checkInnerStreamThreadIds(innerCount,
                                           innerThreadIdCounter,
                                           threadMap,
                                           outerThreadId))

            // Remove all sIntermediaResult values, leaving just a
            // sSequentialResult and/or sParallelResult value.
            .filter(value -> value > sIntermediateResult);
    }

    /**
     * Demonstrate how {@code mapMulti()} can run in parallel.
     *
     * @param innerCount The number of iterations to perform
     * @param consumer The {@link IntConsumer} that will receive the
     *                 result
     */
    private static void innerStreamEx(Integer innerCount,
                                      IntConsumer consumer) {
        // Store the outer thread id.
        var outerThreadId = Thread.currentThread().threadId();

        // Create an AtomicInteger that counts the number of integers
        // processed by each thread.
        var innerThreadIdCounter = new AtomicInteger();

        // Create a ConcurrentHashMap that associates thread ids with
        // a count of the number of integers processed by each thread.
        Map<Long, AtomicInteger> threadMap =
            new ConcurrentHashMap<>();

        int result = IntStream
            // Generate ints from 1 to innerCount iterations.
            .rangeClosed(1, innerCount)

            // Attempt to run each computation in parallel.
            .parallel()

            // Determine if each computation ran in parallel.
            .mapMulti((int i, IntConsumer c) -> {
                    var value = checkInnerStreamThreadIds(innerCount,
                                                          innerThreadIdCounter,
                                                          threadMap,
                                                          outerThreadId);

                // Remove all sIntermediaResult values, leaving just a
                // sSequentialResult or sParallelResult value.
                 if (value > sIntermediateResult)
                     c.accept(value);
                 })

            // Sum the values.
            .sum();

        // Indicate if the inner stream ran sequentially or in
        // parallel.
        consumer.accept(result);
    }
}
