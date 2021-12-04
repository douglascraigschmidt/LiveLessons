import utils.ConcurrentMapCollector;
import utils.GCDResult;
import utils.ListSpliterator;
import utils.RunTimer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

/**
 * This example shows the difference in overhead between collecting
 * results in a parallel stream vs. sequential stream using concurrent
 * and non-concurrent collectors for various types of Java {@link Map}
 * implementations, including {@link HashMap} and {@link TreeMap}.
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
     * Create a {@link List} of random {@link Integer} objects.
     */
    static List<Integer> sRandomNumbers = new Random()
        // Generate "count" random large ints
        .ints(1000000,
              1_000_000,
              1_000_000_000)

        // Convert each primitive int to Integer.
        .boxed()

        // Trigger intermediate operations and collect into list.
        .collect(toList());

    /**
     * Create a {@link Comparator} that's used by the
     * {@link TreeMap} to order the two-element arrays
     * containing the parameters to the GCD method.
     */
    static class CompareIntegers implements Comparator {
        /**
         * @return 0 if the arrays are equal, < 0 if the
         * first array is less than the second, and > 0 if
         * the first array is greater than the second
         */
        public int compare(Object obj1, Object obj2) {
            // Cast the params to the appropriate type.
            Integer[] t1 = (Integer[]) obj1;
            Integer[] t2 = (Integer[]) obj2;

            // Subtract the second element from the
            // first element.
            int r1 = t1[0] - t2[0];

            // If the first comparison != 0 then
            // return the result.
            if (r1 != 0)
                return r1;
            else
                // Continue on to compare the
                // second elements.
                return t1[1] - t2[1];
        }
    }

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
                             () -> new TreeMap(new CompareIntegers()),
                             () -> new TreeMap(new CompareIntegers()),
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
         Function<GCDResult, Integer[]> keyMapper,
         Function<GCDResult, Integer> valueMapper,
         Supplier<Map<Integer[], Integer>> mapSupplier,
         Supplier<Map<Integer[], Integer>> concurrentMapSupplier,
         QuadFunction<String,
                      Boolean,
                      List<Integer>,
                      Collector<GCDResult, ?, Map<Integer[], Integer>>,
                      Void> collect) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000, 1_000_000)

            // Run the tests for various input data sizes.
            .forEach (count -> {
                    // Get a List of 'count' random numbers.
                    List<Integer> randomNumbers = 
                        getRandomData(count);

                    // Print a message when the test starts.
                    System.out.println("Starting "
                                       + testType
                                       + " test for "
                                       + count
                                       + " random numbers..");

                    // Collect results into a sequential stream via a
                    // non-concurrent collector.
                    collect
                        .apply("non-concurrent " + testType,
                               false,
                               randomNumbers,
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
                               randomNumbers,
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
                               randomNumbers,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      concurrentMapSupplier));

                    // Collect results into a parallel stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               true,
                               randomNumbers,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
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
     * @param words A {@link List} of words to lowercase
     * @param collector The {@link Collector} used to combine the
     *                  results
     */
    private static Void timeStreamCollect
        (String testType,
         boolean parallel,
         List<Integer> randomNumbers,
         Collector<GCDResult, ?, Map<Integer[], Integer>> collector) {
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
                        Stream<Integer[]> intStream = StreamSupport
                            // Convert the List of Integer objects
                            // into a sequential stream of two-element
                            // Integer objects used to compute the
                            // GCD.
                            .stream(new ListSpliterator(randomNumbers),
                                    false);

                        // Conditionally convert stream to parallel
                        // stream.
                        if (parallel)
                            intStream.parallel();

                        Map<Integer[], Integer> resultMap = intStream
                            // Compute the GCD of the params.
                            .map(params -> computeGCD(params))

                            // Trigger intermediate processing and
                            // collect GCDResults into the given
                            // collector.
                            .collect(collector);

                        // printResults(resultMap, testName);
                    }},
            testName);
        return null;
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
    private static List<Integer> getRandomData(int count) {
        return sRandomNumbers
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
    private static GCDResult computeGCD(Integer[] integers) {
        // Create a record to hold the GCD results.
        return new GCDResult(integers,
                             gcd(integers[0], integers[1]));
    }

    /**
     * Provides an iterative implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD) of {@code number1}
     * and {@code number2}.
     */
    private static int gcd(int number1, int number2) {
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
            Stream<Integer[]> intStream = StreamSupport
                // Convert the List of Integer objects
                // into a sequential stream of two-element
                // Integer objects used to compute the
                // GCD.
                .stream(new ListSpliterator(getRandomData(100_000)),
                        false);

            intStream.parallel();

            Map<Integer[], Integer> resultMap = intStream
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
