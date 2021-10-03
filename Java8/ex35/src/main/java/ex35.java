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

        // Runs the tests.
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
        var result = List
            // Create a stream of 4 'iterations" from 1 to 4.
            .of(1, 2, 3, 4)

            // Convert the stream to a parallel stream.
            .parallelStream() 

            // Print the outer thread id.
            .peek(iterations -> System.out.println("outer thread id for "
                                                  + iterations
                                                  + " = "
                                                  + Thread.currentThread().getId()))

            // Apply the flatMap() intermediate operation, which
            // doesn't work scalably for parallel streams.
            .flatMap(iterations -> {
                    // Store the outer thread id.
                    long threadId = Thread.currentThread().getId();

                    // Store the # of iterations as a 'max' value.
                    final int max = iterations;

                    // Create an AtomicInteger to pass by reference.
                    AtomicInteger counter = new AtomicInteger();

                    // Create a ConcurrentHashMap to store results.
                    Map<Long, Integer> threadMap = new ConcurrentHashMap<>();

                    return IntStream
                    // Generate ints from 1 .. iterations.
                    .rangeClosed(1, iterations)
                    
                    // Convert each int to Integer.
                    .boxed()

                    // (Attempt to) run the computations in parallel.
                    .parallel()

                    // Check whether the computations ran in parallel.
                    .peek(m -> 
                          checkInnerThreadIds(max,
                                              counter,
                                              threadMap,
                                              threadId));
                })

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
        var result = List
            // Create a stream of 4 'iterations" from 1 to 4.
            .of(1, 2, 3, 4)

            // Convert the stream to a parallel stream.
            .parallelStream()

            // Print the outer thread id.
            .peek(iterations -> System.out.println("outer thread id for "
                                                   + iterations
                                                   + " = "
                                                   + Thread.currentThread().getId()))

            // Apply the map() intermediate operation, which works
            // scalably for parallel streams.
            .map(iterations -> {
                    // Store the outer thread id.
                    long threadId = Thread.currentThread().getId();

                    // Store the # of iterations as a 'max' value.
                    final int max = iterations;

                    // Create an AtomicInteger to pass by reference.
                    AtomicInteger counter = new AtomicInteger();

                    // Create a ConcurrentHashMap to store results.
                    Map<Long, Integer> threadMap = new ConcurrentHashMap<>();

                    return IntStream
                    // Generate ints from 1 .. iterations.
                    .rangeClosed(1, iterations)

                    // Convert each int to Integer.
                    .boxed()

                    // Run the computations in parallel.
                    .parallel()

                    // Check whether the computations ran in parallel.
                    .peek(m -> 
                          checkInnerThreadIds(max,
                                              counter,
                                              threadMap,
                                              threadId));
                })

            // Reduce the stream of streams into a single stream of
            // Integer objects.
            .reduce(Stream::concat).orElse(Stream.empty())

            // Collect the results into a List of Integer objects.
            .collect(Collectors.toList());

        // Print the results.
        System.out.println("testReduceConcat() results = " + result);
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
    private static void checkInnerThreadIds(int max,
                                            AtomicInteger counter,
                                            Map<Long, Integer> threadMap,
                                            long threadId) {
        // Try to store 0 into the map at the current thread id.
        var existing = threadMap
            .put(Thread.currentThread().getId(),
                 0);

        // A null indicates this is the first time in, so we
        // initialize the count for that thread id to 1.
        if (existing == null) {
            threadMap.put(Thread.currentThread().getId(),
                          1);

        // A non-null indicates this is not the first time in, so we
        // increment the count for that thread id by 1.
        } else {
            threadMap.put(Thread.currentThread().getId(),
                          existing + 1);
        }

        // When the final iteration has been encountered print the
        // contents of the map.
        if (counter.incrementAndGet() == max)
            System.out.println("inner thread ids for outer thread "
                               + threadId 
                               + " = " 
                               + threadMap);
    }
}
