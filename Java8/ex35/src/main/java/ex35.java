import utils.Options;
import utils.RunTimer;

import java.util.List;
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

        // Runs the flatMap() and reduce()/Stream.concat() tests.
        runTest(ex35::testFlatMap, "testFlatMap()");
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

        // Let the system garbage collect.
        System.gc();

        // Record how long the test takes to run.
        RunTimer.timeRun(test,
                         testName);

    }

    /**
     * Demonstrate how the {@code testFlatMap()}
     * intermediate operation doesn't scale in a Java parallel stream.
     */
    private static void testFlatMap() {
        var result =
            generateOuterStream(Options.instance().iterations())

            // Apply the flatMap() intermediate operation, which
            // doesn't work scalably for parallel streams.
            .flatMap(ex35::innerStream)

            // Collect the results into a List of Integer objects.
            .collect(Collectors.toList());

        // Print the results.
        System.out.println("testFlatMap() results = " + result);
    }

    /**
     * Demonstrate how combining {@code reduce()} and {@code Stream.concat()}
     * scales much better than {@code flatMap()} in a Java parallel stream.
     */
    private static void testReduceConcat() {
        var result =
            generateOuterStream(Options.instance().iterations())

            // Apply the map() intermediate operation, which works
            // scalably for parallel streams.
            .map(ex35::innerStream)

            // Reduce the stream of streams into a single stream of
            // Integer objects.
            .reduce(Stream::concat).orElse(Stream.empty())

            // Collect the results into a List of Integer objects.
            .collect(Collectors.toList());

        // Print the results.
        System.out.println("testReduceConcat() results = " + result);
    }

    /**
     * @return A parallel stream that emits {@code iteration } {@link Integer} objects
     */
    private static Stream<Integer> generateOuterStream(int iterations) {
        return IntStream
            // Create a stream of 4 'iterations" from 1 to 4.
            .rangeClosed(1, iterations)

            // Convert int to Integer.
            .boxed()

            // Convert the stream to a parallel stream.
            .parallel()

            // Print the outer thread id.
            .peek(iteration -> System.out.println("outer thread id for "
                                                   + iteration
                                                   + " = "
                                                   + Thread.currentThread().getId()));
    }

    /**
     * Compute and print how many threads are used to process an inner
     * parallel stream.
     *
     * @param max       The max number of iterations
     * @param counter   Keep track of whether the max number of
     *                  iterations have been met
     * @param threadMap A {@link Map} that records which threads are
     *                  used by the inner parallel stream
     * @param threadId  The outer thread id.
     */
    private static void checkInnerStreamThreadIds(int max,
                                                  AtomicInteger counter,
                                                  Map<Long, AtomicInteger> threadMap,
                                                  long threadId) {
        // Try to find the current thread id in the map.  If this is
        // the first time in then give it an initial value of 1.
        AtomicInteger value = threadMap
            .putIfAbsent(Thread.currentThread().getId(),
                         new AtomicInteger(1));

        // If this isn't the first time in then simply increment by 1.
        if (value != null)
            value.incrementAndGet();

        // When the final iteration has been encountered print the
        // contents of the map.
        if (counter.incrementAndGet() == max)
            System.out.println("inner thread ids for outer thread "
                               + threadId 
                               + " = " 
                               + threadMap);
    }

    /**
     * Return an inner {@link Stream} that emits {@link Integer} objects from 1
     * to {@code iterations}.  This method also check to see whether the inner
     * {@link Stream} ran in parallel or not.
     *
     * @param iterations The number of iterations to emit
     * @return An inner {@link Stream} that emits {@link Integer} objects from
     *         1 to {@code iterations}.
     */
    private static Stream<Integer> innerStream(Integer iterations) {
        // Store the outer thread id.
        long threadId = Thread.currentThread().getId();

        // Store the # of iterations as a 'max' value.
        final int max = iterations;

        // Create an AtomicInteger to pass by reference.
        AtomicInteger counter = new AtomicInteger();

        // Create a ConcurrentHashMap to store results.
        Map<Long, AtomicInteger> threadMap = new ConcurrentHashMap<>();

        return IntStream
            // Generate ints from 1 .. iterations.
            .rangeClosed(1, iterations)

            // Convert each int to Integer.
            .boxed()

            // Run the computations in parallel.
            .parallel()

            // Check whether the computations ran in parallel.
            .peek(m ->
                  checkInnerStreamThreadIds(max,
                                            counter,
                                            threadMap,
                                            threadId));
    }
}
