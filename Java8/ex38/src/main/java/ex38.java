import utils.*;
import gcd.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.*;

import static java.util.stream.Collectors.*;

/**
 * This program creates various {@link Map} objects that compute the
 * Greatest Common Divisor (GCD) for a various sized lists of randomly
 * generated integers.  It also shows the difference in overhead
 * between collecting results in a parallel stream vs. sequential
 * stream using concurrent and non-concurrent collectors for various
 * types of Java {@link Map} implementations, including {@link
 * HashMap} and {@link TreeMap}.  In addition, it shows how to use the
 * Java {@code record} type.
 */
public class ex38 {
    /**
     * This interface applies four params to perform a computation.
     */
    @FunctionalInterface
    interface QuadFunction<P1, P2, P3, P4> {
        void apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }
    
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 10;

    /**
     * Main entry point into the tests program.
     */
    static public void main(String[] argv) {
        System.out.println("Entering the test program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Warm up the threads in the fork/join pool so the timing
        // results will be more accurate.
        warmUpForkJoinPool();

        // Run tests that demonstrate the performance differences
        // between concurrent and non-concurrent collectors when
        // collecting results in Java sequential and parallel streams
        // that use HashMaps, which are unordered.
        runMapCollectorTests("HashMap",
                             GCDResult::integers,
                             GCDResult::gcd,
                             HashMap::new,
                             ConcurrentHashMap::new,
                             ex38::timeStreamCollect);

        // Run tests that demonstrate the performance differences
        // between concurrent and non-concurrent collectors when
        // collecting results in Java sequential and parallel streams
        // that use TreeMaps, which are ordered.
        runMapCollectorTests("TreeMap",
                             GCDResult::integers,
                             GCDResult::gcd,
                             TreeMap::new,
                             TreeMap::new,
                             ex38::timeStreamCollect);

        // Print the results.
        printResults();

        System.out.println("Exiting the test program");
    }

    /**
     * Run tests that demonstrate the performance differences between
     * concurrent and non-concurrent collectors when collecting
     * results in Java sequential and parallel streams for various
     * types of Java {@link Map} types.
     * 
     * @param testType The type of test, i.e., HashMap or TreeMap
     * @param mapSupplier A {@link Supplier} that creates the given
     *                    non-concurrent {@link Map}
     * @param concurrentMapSupplier A {@link Supplier} that creates
     *                              the given concurrent {@link Map}
     * @param streamCollect A {@link Function} that performs the test
     *                      using either a non-concurrent or
     *                      concurrent {@link Collector
     */
    private static void runMapCollectorTests
        (String testType,
         Function<GCDResult, GCDParam> keyMapper,
         Function<GCDResult, Integer> valueMapper,
         Supplier<Map<GCDParam, Integer>> mapSupplier,
         Supplier<Map<GCDParam, Integer>> concurrentMapSupplier,
         QuadFunction<String,
                      Boolean,
                      List<GCDParam>,
                      Collector<GCDResult, ?, Map<GCDParam, Integer>>>
                 streamCollect) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000)

            // Run the tests for various input data sizes.
            .forEach (count -> {
                    // Get a List of 'count' random GCDParam objects.
                    List<GCDParam> randomParams = GCDUtils
                        .getRandomData(count);

                    // Print a message when the test starts.
                    System.out.println("Starting "
                                       + testType
                                       + " test for "
                                       + count
                                       + " random GCD params..");

                    // Collect results into a sequential stream via a
                    // non-concurrent collector.
                    streamCollect
                        .apply("non-concurrent " + testType,
                               false,
                               randomParams,
                               Collectors
                               .toMap(keyMapper,
                                      valueMapper,
                                      mergeDuplicateKeyValues(),
                                      mapSupplier));

                    // Collect results into a parallel stream via a
                    // non-concurrent collector.
                    streamCollect
                        .apply("non-concurrent " + testType,
                               true,
                               randomParams,
                               Collectors
                               .toMap(keyMapper,
                                      valueMapper,
                                      mergeDuplicateKeyValues(),
                                      mapSupplier));

                    // Collect results into a sequential stream via a
                    // concurrent collector.
                    streamCollect
                        .apply("concurrent " + testType,
                               false,
                               randomParams,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      mergeDuplicateKeyValues(),
                                      concurrentMapSupplier));

                    // Collect results into a parallel stream via a
                    // concurrent collector.
                    streamCollect
                        .apply("concurrent " + testType,
                               true,
                               randomParams,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      mergeDuplicateKeyValues(),
                                      concurrentMapSupplier));

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Merge duplicate keys by simply choosing one of the key's values.
     *
     * @return One of the values
     */
    private static BinaryOperator<Integer> mergeDuplicateKeyValues() {
        return (o1, o2) -> o1;
    }

    /**
     * Determines how long it takes to lowercase a {@link List} of
     * {@code words} and collect the results using the given {@link
     * Collector}.
     *
     * @param testType The type of test, i.e., HashMap or TreeMap
     * @param parallel If true then a parallel stream is used, else a
     *                 sequential stream is used
     * @param randomNumbers A {@link List} of {@link GCDParam} objects
     * @param collector The {@link Collector} used to combine the
     *                  results
     */
    private static void timeStreamCollect
        (String testType,
         boolean parallel,
         List<GCDParam> randomNumbers,
         Collector<GCDResult, ?, Map<GCDParam, Integer>> collector) {
        // Run the garbage collector before each test.
        System.gc();

        String testName =
            (parallel ? " parallel" : " sequential")
            + " "
            + testType;

        RunTimer
            // Time how long it takes to run the test.
            .timeRun(() -> {
                    IntStream
                        // Iterate for sMAX_ITERATIONS.
                        .range(0, sMAX_ITERATIONS)

                        // Run the test.
                        .forEach(i ->
                                 // Get the results.
                                 getResults(parallel,
                                            randomNumbers,
                                            collector));

                },
                testName);
    }

    /**
     * Perform computations that create a {@link Map} of {@link
     * GCDParam} and {@link Integer} objects.
     * 
     * @param parallel If true then a parallel stream is used, else a
     *                 sequential stream is used
     * @param randomNumbers A {@link List} of {@link GCDParam} objects
     * @param collector The {@link Collector} used to combine the
     *                  results
     * @return A {@link Map} containing {@link GCDResult} objects
     */
    private static Map<GCDParam, Integer> getResults
        (boolean parallel,
         List<GCDParam> randomNumbers,
         Collector<GCDResult, ?, Map<GCDParam, Integer>> collector) {
        // Conditionally convert into a parallel or sequential stream.
        return (parallel ? randomNumbers.parallelStream()
                  : randomNumbers.stream())

            // Compute the GCD of the params.
            .map(GCDUtils::computeGCD)

            // Trigger intermediate processing and
            // collect GCDResults into the given
            // collector.
            .collect(collector);
    }

    /**
     * Print the results.
     */
    private static void printResults() {
        var treeMapResults = getResults(true,
                GCDUtils.getRandomData(10),
                Collectors
                        .toMap(GCDResult::integers,
                                GCDResult::gcd  ,
                                mergeDuplicateKeyValues(),
                                TreeMap::new));

        var hashMapResults = getResults(true,
                GCDUtils.getRandomData(10),
                Collectors
                        .toMap(GCDResult::integers,
                                GCDResult::gcd  ,
                                mergeDuplicateKeyValues(),
                                HashMap::new));

        Function<Map<GCDParam, Integer>, String> display = results ->
            results
            .entrySet()
            .stream()
            .map(entry ->
                 "GCD of "
                 + entry.getKey()
                 + " is "
                 + entry.getValue())
            .collect(joining("|"));

        // Print the results.
        System.out.println("Results of hashMapResults was:\n"
                           + display.apply(hashMapResults));
        System.out.println("Results of treeMapResults was:\n"
                + display.apply(treeMapResults));
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            var results = GCDUtils
                .getRandomData(100000)
                .parallelStream()

                // Compute the GCD of the params.
                .map(GCDUtils::computeGCD)

                // Trigger intermediate processing and
                // collect GCDResults into the given
                // collector.
                .collect(toMap(GCDResult::integers,
                               GCDResult::gcd));
        }
    }
}
