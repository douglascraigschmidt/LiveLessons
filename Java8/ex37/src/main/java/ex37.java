import utils.ConcurrentMapCollector;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * This program creates various {@link Map} objects that associate
 * unique words in the complete works of William Shakespeare with the
 * number of times each word appears.  It also shows the difference in
 * overhead between collecting results in a parallel stream
 * vs. sequential stream using concurrent and non-concurrent
 * collectors for various types of Java {@link Map} implementations,
 * including {@link HashMap} and {@link TreeMap}.
 */
@SuppressWarnings("ALL")
public class ex37 {
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
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A regular expression that matches whitespace and punctuation.
     */
    private static final String sWHITESPACE_AND_PUNCTUATION = 
        "[\\t\\n\\x0B\\f\\r'!()\"#&-.,;0-9:@<>\\[\\]? ]+";

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
                             Function.identity(),
                             (s) -> 1,
                             HashMap::new,
                             ConcurrentHashMap::new,
                             ex37::timeStreamCollect);

        // Run tests that demonstrate the performance differences
        // between concurrent and non-concurrent collectors when
        // collecting results in Java sequential and parallel streams
        // that use TreeMaps, which are ordered.
        runMapCollectorTests("TreeMap",
                             Function.identity(),
                             (s) -> 1,
                             TreeMap::new,
                             TreeMap::new,
                             ex37::timeStreamCollect);

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
     * @param collect A {@link QuadFunction} that performs the test
     *                using either a non-concurrent or concurrent
     *                {@link Collector}
     */
    private static void runMapCollectorTests
        (String testType,
         Function<String, String> keyMapper,
         Function<String, Integer> valueMapper,
         Supplier<Map<String, Integer>> mapSupplier,
         Supplier<Map<String, Integer>> concurrentMapSupplier,
         QuadFunction<String,
                      Boolean,
                      List<String>,
                      Collector<String, ?, Map<String, Integer>>,
                      Void> collect) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1000, 10000, 100000, 1000000)

            // Run the tests for various input data sizes.
            .forEach (limit -> {
                    // Create a List of Strings containing
                    // 'limit' words from the works of Shakespeare.
                    List<String> arrayWords = TestDataFactory
                        .getInput(sSHAKESPEARE_DATA_FILE,
                                  // Split input into "words" by
                                  // ignoring whitespace and
                                  // punctuation.
                                  sWHITESPACE_AND_PUNCTUATION,
                                  limit);

                    // Print a message when the test starts.
                    System.out.println("Starting "
                                       + testType
                                       + " test for "
                                       + arrayWords.size() 
                                       + " words..");

                    // Collect results into a sequential stream via a
                    // non-concurrent collector.
                    collect
                        .apply("non-concurrent " + testType,
                               false,
                               arrayWords,
                               Collectors.
                               toMap(keyMapper,
                                     valueMapper,
                                     (o1, o2) -> o1 + o2,
                                     mapSupplier));

                    // Collect results into a parallel stream via a
                    // non-concurrent collector.
                    collect
                        .apply("non-concurrent " + testType,
                               true,
                               arrayWords,
                               Collectors
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1 + o2,
                                      mapSupplier));

                    // Collect results into a sequential stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               false,
                               arrayWords,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1 + o2,
                                      concurrentMapSupplier));

                    // Collect results into a parallel stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               true,
                               arrayWords,
                               ConcurrentMapCollector
                               .toMap(keyMapper,
                                      valueMapper,
                                      (o1, o2) -> o1 + o2,
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
         List<String> words,
         Collector<String, ?, Map<String, Integer>> collector) {
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
                        Stream<String> wordStream = words
                            // Convert the list into a stream (which
                            // uses a spliterator internally).
                            .stream();

                        if (parallel)
                            // Convert to a parallel stream.
                            wordStream.parallel();

                        // A Map of unique words in Shakespeare's
                        // works.
                        Map<String, Integer> uniqueWords = wordStream
                            // Map each string to lower case.
                            .map(word -> word.toString().toLowerCase())

                            // Trigger intermediate processing and
                            // collect the unique words into the given
                            // collector.
                            .collect(collector);
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
    private static void printResults(Map<String, Integer> results,
                                     String testName) {
        // Convert the first 10 elements of the Map contents into a
        // String.
        var output = results
            .entrySet()
            .stream()
            .limit(10)
            .map(Objects::toString)
            .collect(toList());

        // Convert the top 10 most common words into a String.
        var topWords = results
            .entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .limit(10)
            .map(Objects::toString)
            .collect(toList());

        // Print the results.
        System.out.println("Results for "
                           + testName
                           + " of size "
                           + results.size()
                           + " was:\n"
                           + output
                           + "\nwith top words being\n"
                           + topWords);
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        List<String> words = TestDataFactory
            .getInput(sSHAKESPEARE_DATA_FILE,
                      // Split input into "words" by ignoring
                      // whitespace.
                      "\\s+");

        // Create an empty list.
        List<String> list = new ArrayList<>();

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            // Append the new words to the end of the list.
            list.addAll(words
                        // Convert the list into a parallel stream
                        // (which uses a spliterator internally).
                        .parallelStream()

                        // Uppercase each string.  A "real"
                        // application would likely do something
                        // interesting with the words at this point.
                        .map(word -> word.toString().toUpperCase())

                        // Collect the stream into a list.
                        .collect(toList()));
    }
}
