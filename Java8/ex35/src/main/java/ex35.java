import utils.Options;
import utils.RunTimer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This example first demonstrates how the {@code testFlatMap()}
 * intermediate operation doesn't scale in a Java parallel stream and
 * then shows how a combination of {@code reduce()} and {@code
 * Stream.concat()} fixes this problem.
 */
public class ex35 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex35.class.getName();

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Entering the program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Run the flatMap() test, which demonstrates the limitations of
        // flatMap() with Java parallel streams.
        runTest(ex35::testFlatMap, "testFlatMap()");

        // Run the reduce()/Stream.concat() test, which fixes the limitations
        // with flatMap().
        runTest(ex35::testReduceConcat, "testReduceConcat()");

        // Print the results.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Leaving the program");
    }

    /**
     * Run the test named {@code testName} by applying the {@code
     * test} {@link Runnable} and timing its performance.
     *
     * @param test The test to run
     * @param testName Name of the test
     */
    private static void runTest(Runnable test,
                                String testName) {

        // Let the system garbage collect to start on an
        // even playing field.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(test,
                         testName);

    }

    /**
     * Demonstrate how the {@code flatMap()} intermediate
     * operation doesn't scale in a Java parallel stream.
     */
    private static void testFlatMap() {
        System.out.println("Entering testFlatMap()");

        var result =
            // Generate an outer Stream that emits the designated
            // number of Integer objects in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply the flatMap() intermediate operation, which
            // doesn't work scalably for parallel streams.
            .flatMap(ex35::innerStream)

            // Collect the results into a List of Integer objects.
            .collect(Collectors.toList());

        // Print the results.
        System.out.println("Leaving testFlatMap() with results = " + result);
    }

    /**
     * Demonstrate how combining {@code reduce()} and {@code Stream.concat()}
     * scales much better than {@code flatMap()} in a Java parallel stream.
     */
    private static void testReduceConcat() {
        System.out.println("Entering testReduceConcat()");

        var result =
            // Generate an outer Stream that emits the designated
            // number of Integer objects in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply the map() intermediate operation, which works
            // scalably for parallel streams.
            .map(ex35::innerStream)

            // Reduce the stream of streams into a stream of Integer objects.
            .reduce(Stream::concat).orElse(Stream.empty())

            // Collect the results into a List of Integer objects.
            .collect(Collectors.toList());

        // Print the results.
        System.out.println("Leaving testReduceConcat() with results = " + result);
    }

    /**
     * Return an outer {@link Stream} that emits {@link Integer} objects from 1
     * to {@code outerCount}.
     * @param outerCount The number of iterations to perform
     * @return An outer {@link Stream} that emits {@code outerCount} {@link Integer}
     *         objects in parallel
     */
    private static Stream<Integer> generateOuterStream(int outerCount) {
        return IntStream
            // Create a Stream of ints from 1 to outerCount.
            .rangeClosed(1, outerCount)

            // Convert each int to an Integer.
            .boxed()

            // Convert the stream to a parallel stream.
            .parallel()

            // Print the outer thread id to aid debugging.
            .peek(iteration ->
                  System.out.println("outer thread id for "
                                     + iteration
                                     + " = "
                                     + Thread.currentThread().getId()));
    }

    /**
     * Return an inner {@link Stream} that emits {@link Integer} objects from 1
     * to {@code innerCount}.  This method also check to see whether the inner
     * {@link Stream} ran in parallel or not.
     *
     * @param innerCount The number of iterations to perform
     * @return An inner {@link Stream} that emits {@link Integer} objects from
     *         1 to {@code innerCount}, possibly in parallel (depending on the
     *         context in which {@code innerStream()} is called)
     */
    private static Stream<Integer> innerStream(Integer innerCount) {
        // Store the outer thread id.
        long threadId = Thread.currentThread().getId();

        // Create an AtomicInteger to pass by reference.
        AtomicInteger counter = new AtomicInteger();

        // Create a ConcurrentHashMap to store results.
        Map<Long, AtomicInteger> threadMap = new ConcurrentHashMap<>();

        return IntStream
            // Generate ints from 1 to iterations.
            .rangeClosed(1, innerCount)

            // Convert each int to an Integer.
            .boxed()

            // Run the computations in parallel.
            .parallel()

            // Check whether the computations ran in parallel.
            .peek(m -> checkInnerStreamThreadIds(innerCount, counter,
                                                 threadMap, threadId));
    }

    /**
     * Determine and print how many threads are used to process an inner
     * parallel stream.
     *
     * @param maxIterations The max number of iterations
     * @param iteration     Keep track of whether the max number of
     *                      iterations have been met
     * @param threadMap     A {@link Map} that records which threads are
     *                      used by the inner parallel stream
     * @param outerThreadId The outer stream's thread id
     */
    private static void checkInnerStreamThreadIds(int maxIterations,
                                                  AtomicInteger iteration,
                                                  Map<Long, AtomicInteger> threadMap,
                                                  long outerThreadId) {
        // Try to find the AtomicInteger associated with current thread id in
        // the map.  If this is the first time in give it an initial value of 1.
        AtomicInteger value = threadMap
            .putIfAbsent(Thread.currentThread().getId(),
                         new AtomicInteger(1));

        // If this isn't the first time in simply increment by 1.
        if (value != null)
            value.incrementAndGet();

        // When the final iteration has been reached print the
        // contents of the map.
        if (iteration.incrementAndGet() == maxIterations)
            System.out.println("inner thread ids for outer thread "
                                   + outerThreadId
                                   + " = "
                                   + threadMap);
    }
}
