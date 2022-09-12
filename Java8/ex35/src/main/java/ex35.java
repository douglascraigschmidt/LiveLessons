import utils.Options;
import utils.RunTimer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * This example demonstrates how the {@code flatMap()} intermediate
 * operation doesn't scale in a Java parallel stream since it forces
 * sequential processing.  It then shows how a combination of {@code
 * map()}, {@code reduce()}, and {@code Stream.concat()} fixes this
 * problem.
 */
public class ex35 {
    /**
     * These constants are used to differentiate different results
     * from the checkInnerStreamThreadIds() method, which determines
     * if an inner stream operation runs in parallel or sequentially.
     */ 
    private static final int sIntermediateResult = 0;
    private static final int sSequentialResult = 1;
    private static final int sParallelResult = 2;

    /**
     * Main entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Entering the program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Run the flatMap() test, which demonstrates the limitations
        // of flatMap() with Java parallel streams.
        runTest(ex35::testFlatMap, "testFlatMap()");

        // Run the reduce()/Stream.concat() test, which shows how to
        // overcome the limitations with flatMap().
        runTest(ex35::testReduceConcat, "testReduceConcat()");

        // Print the timing results.
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
     * Demonstrate how the {@code flatMap()} intermediate operation
     * doesn't scale in a Java parallel stream.
     */
    private static void testFlatMap() {
        System.out.println("Entering testFlatMap()");

        var wasSequential =
            // Generate an outer Stream that emits the designated
            // number of Integer objects in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply flatMap(), which doesn't scale for parallel
            // streams since it only runs sequentially!
            .flatMap(ex35::innerStream)

            // Return true if all results were sequential, else false.
            .allMatch(value -> value == sSequentialResult);

        // Print the results.
        System.out.println("Leaving testFlatMap(), which ran "
                           + (wasSequential ? "sequentially" : "in parallel"));
    }

    /**
     * Demonstrate how combining {@code reduce()} and {@code
     * Stream.concat()} scales much better than {@code flatMap()} in a
     * Java parallel stream.
     */
    private static void testReduceConcat() {
        System.out.println("Entering testReduceConcat()");

        var wasSequential =
            // Generate an outer Stream that emits the designated
            // number of Integer objects in parallel.
            generateOuterStream(Options.instance().iterations())

            // Apply map(), which works scalably for parallel streams
            // since it runs tasks in parallel.
            .map(ex35::innerStream)

            // Reduce the stream of streams of Integer objects into a
            // stream of Integer objects.
            .reduce(Stream::concat).orElse(Stream.empty())

            // Return true if all results were sequential, else false.
            .allMatch(value -> value == sSequentialResult);

        // Print the results.
        System.out.println("Leaving testReduceConcat(), which ran "
                           + (wasSequential ? "sequentially" : "in parallel"));
    }

    /**
     * Return an outer {@link Stream} that emits {@link Integer}
     * objects from 1 to {@code outerCount}.
     *
     * @param outerCount The number of iterations to perform
     * @return An outer {@link Stream} that generates {@code
     *         outerCount} {@link Integer} objects in parallel
     */
    private static Stream<Integer> generateOuterStream(int outerCount) {
        return IntStream
            // Create a Stream of ints from 1 to outerCount.
            .rangeClosed(1, outerCount)

            // Convert each int to an Integer.
            .boxed()

            // Convert the stream to a parallel stream.
            .parallel()

            // Print the outer thread id to aid result analysis.
            .peek(iteration -> Options
                  .display("outer thread id for iteration "
                           + iteration
                           + " = "
                           + Thread.currentThread().getId()));
    }

    /**
     * Return an inner {@link Stream} that emits {@link Integer}
     * objects indicating whether the inner {@link Stream} ran
     * sequentially or in parallel.  
     *
     * @param innerCount The number of iterations to perform
     * @return An inner {@link Stream} that emits {@link Integer}
     *         objects {@code sParallelResult} or {@code
     *         sSequentialResult}, depending on the context in which
     *         {@code innerStream()} is called
     */
    private static Stream<Integer> innerStream(Integer innerCount) {
        // Store the outer thread id.
        long outerThreadId = Thread.currentThread().getId();

        // Create an AtomicInteger that counts the number of integers
        // processed by each thread.
        AtomicInteger innerThreadIdCounter = new AtomicInteger();

        // Create a ConcurrentHashMap that associates thread ids with
        // a count of the number of integers processed by each thread.
        Map<Long, AtomicInteger> threadMap = new ConcurrentHashMap<>();

        return IntStream
            // Generate ints from 1 to innerCount iterations.
            .rangeClosed(1, innerCount)

            // Convert each int to an Integer.
            .boxed()

            // Attempt to run each computation in parallel.
            .parallel()

            // Determine if a computation ran in parallel.
            .map(__ -> 
                 checkInnerStreamThreadIds(innerCount,
                                           innerThreadIdCounter,
                                           threadMap,
                                           outerThreadId))

            // Remove all sIntermediaResult values, leaving just
            // sSequentialResult and sParallelResult values.
            .filter(value -> value > sIntermediateResult);
    }

    /**
     * Determine and print how many threads are used to process an
     * inner stream.  If the inner stream runs sequentially its thread
     * id will match the {@code outerThreadId}.  If the inner stream
     * runs in parallel then its thread id may not match the {@code
     * outerThreadId}.
     *
     * @param maxIterations The max number of iterations
     * @param innerThreadIdCounter Keep track of whether the max
     *                             number of iterations have been met
     * @param threadMap A {@link Map} that records which threads are
     *                  used by the inner parallel stream
     * @param outerThreadId The outer stream's thread id
     * @return An {@code int} that's either {@code
     *         sIntermediateResult}, {@code sSequentialResult}, or
     *         {@code sParallelResult}, depending on the observed
     *         behavior
     */
    private static int checkInnerStreamThreadIds
        (int maxIterations,
         AtomicInteger innerThreadIdCounter,
         Map<Long, AtomicInteger> threadMap,
         long outerThreadId) {
        // Try to find the AtomicInteger associated with current
        // thread id in the map.
        var value = threadMap
            // If it's the first time in make an initial value of 1.
            .putIfAbsent(Thread.currentThread().getId(),
                         new AtomicInteger(1));

        // If it's not the first time in increment the AtomicInteger
        // by 1 to indicate this inner thread processed it.
        if (value != null)
            value.incrementAndGet();

        // Conduct analysis when the final iteration is reached.
        if (innerThreadIdCounter.incrementAndGet() == maxIterations) {
            // See if any computations ran in non-outer threads!
            boolean wasSequential = threadMap.size() == 1
                && threadMap.containsKey(outerThreadId);

            Options.display("inner thread ids for outer thread "
                            + outerThreadId
                            + " = "
                            + threadMap
                            + " suggests "
                            + (wasSequential 
                               ? "sequential execution" 
                               : "parallel execution"));

            // Indicate if the inner stream ran sequentially or in
            // parallel.
            return wasSequential ? sSequentialResult : sParallelResult;
        } else
            // Intermediate results are ignored by the caller.
            return sIntermediateResult;
    }
}
