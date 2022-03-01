import utils.ConcurrentMapCollector;
import utils.GCDResult;
import utils.GCDParam;
import utils.RunTimer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.*;

import static java.lang.Math.abs;
import static java.util.stream.Collectors.*;

/**
 * This program creates various {@link Map} objects that compute the
 * Greatest Common Divisor (GCD) for a various sized lists of randomly
 * generated integers.  It also shows the difference in overhead
 * between collecting results in a parallel stream vs. sequential
 * stream using concurrent and non-concurrent collectors for various
 * types of Java {@link Map} implementations, including {@link
 * HashMap} and {@link TreeMap}.
 */
@SuppressWarnings("ALL")
public class ex38 {
    /**
     * This interface converts four params to a result type.
     */
    @FunctionalInterface
    interface QuadFunction<P1, P2, P3, P4, R> {
        R apply(P1 p1, P2 p2, P3 p3, P4 p4);
    }
    
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 10;

    /**
     * The random number generator.
     */
    private static Random sRANDOM = new Random();

    /**
     * Create a {@link List} of random {@link GCDParam} objects.
     */
    static List<GCDParam> sRANDOM_PARAMS = IntStream
        // Iterate from 1 to 1_000_000.
        .rangeClosed(1, 1_000_000)

        // Create a stream of GCDParam objects initialized to random values.
        .mapToObj(___ -> 
                  new GCDParam(abs(sRANDOM.nextInt()), 
                               abs(sRANDOM.nextInt())))

        // Trigger intermediate operations and collect into list.
        .collect(toList());

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

        System.out.println("Exiting the test program");
    }

    /**
     * Run tests that demonstrate the performance differences between
     * concurrent and non-concurrent collectors when collecting
     * results in Java sequential and parallel streams for various
     * types of Java {@link Map} types.
     * 
     * @param testType The type of test, i.e., HashMap or TreeMap
     * @param setSupplier A {@link Supplier} that creates the given
     *                    non-concurrent {@link Map}
     * @param concurrentMapSupplier A {@link Supplier} that creates
     *                              the given concurrent {@link Map}
     * @param collect A {@link Function} that performs the test using
     *                either a non-concurrent or concurrent {@link
     *                Collector
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
                      Collector<GCDResult, ?, Map<GCDParam, Integer>>,
                      Void> collect) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000, 500_000)

            // Run the tests for various input data sizes.
            .forEach (count -> {
                    // Get a List of 'count' random GCDParam objects.
                    List<GCDParam> randomParams = getRandomData(count);

                    // Print a message when the test starts.
                    System.out.println("Starting "
                                       + testType
                                       + " test for "
                                       + count
                                       + " random GCD params..");

                    // Collect results into a sequential stream via a
                    // non-concurrent collector.
                    collect
                        .apply("non-concurrent " + testType,
                               false,
                               randomParams,
                               Collectors
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1,
                                      mapSupplier));

                    // Collect results into a parallel stream via a
                    // non-concurrent collector.
                    collect
                        .apply("non-concurrent " + testType,
                               true,
                               randomParams,
                               Collectors
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1,
                                      mapSupplier));

                    // Collect results into a sequential stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               false,
                               randomParams,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1,
                                      concurrentMapSupplier));

                    // Collect results into a parallel stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               true,
                               randomParams,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1,
                                      concurrentMapSupplier));

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
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
    private static Void timeStreamCollect
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
                    for (int i = 0; i < sMAX_ITERATIONS; i++) {
                        getResults(parallel, randomNumbers, collector);
                    }},
            testName);
        return null;
    }

    /**
     * Perform computations that create a Map of {@ink GCDResult} objects
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
        // Convert into a sequential stream.
        Stream<GCDParam> intStream = randomNumbers
            .stream();

        // Conditionally convert stream to parallel
        // stream.
        if (parallel)
            intStream.parallel();

        return intStream
            // Compute the GCD of the params.
            .map(params -> computeGCD(params))

            // Trigger intermediate processing and
            // collect GCDResults into the given
            // collector.
            .collect(collector);
    }
    /**
     * Print the {@code result} of the {@code testName}.
     *
     * @param result The results of applying the test
     * @param testName The name of the test.
     */
    private static void printResults(Map<Integer[], Integer> results,
                                     String testName) {
        // Convert the first 10 elements of the Map contents into a
        // String.
        var output = results
            .entrySet()
            .stream()
            .limit(10)
            .map(entry ->
                 "["
                 + entry.getKey()[0]
                 + ","
                 + entry.getKey()[1]
                 + "]="
                 + entry.getValue())
            .collect(joining("|"));

        // Print the results.
        System.out.println("Results for "
                           + testName
                           + " of size "
                           + results.size()
                           + " was:\n"
                           + output);
    }

    /**
     * Generate random data for use by the various hashmaps.
     *
     * @return A {@link List} of random {@link Integer} objects
     */
    private static List<GCDParam> getRandomData(int count) {
        return sRANDOM_PARAMS
            // Convert the List into a Stream.
            .stream()

            // Limit the size of the stream by 'count'.
            .limit(count)

            // Collect the results into a List.
            .collect(toList());
    }

    /**
     * Compute the GCD of the two-element array {@code integers}.
     *
     * @param integers A two-element array containing the numbers to
     *                 compute the GCD
     * @return A {@link GCDResult}
     */
    private static GCDResult computeGCD(GCDParam integers) {
        // Create a record to hold the GCD results.
        return new GCDResult(integers,
                             gcd(integers));
    }

    /**
     * Provides an iterative implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD) of {@code number1}
     * and {@code number2}.
     */
    private static int gcd(GCDParam integers) {
        int number1 = integers.first();
        int number2 = integers.second();
        for (;;) {
            int remainder = number1 % number2;
            if (remainder == 0){
                return number2;
            } else{
                number1 = number2;
                number2 = remainder;
            }
        }
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            Stream<GCDParam> intStream = getRandomData(100000)
                .parallelStream();

            Map<GCDParam, Integer> resultMap = intStream
                // Compute the GCD of the params.
                .map(params -> computeGCD(params))

                // Trigger intermediate processing and
                // collect GCDResults into the given
                // collector.
                .collect(toMap(GCDResult::integers,
                               GCDResult::gcd));
        }
    }
}
