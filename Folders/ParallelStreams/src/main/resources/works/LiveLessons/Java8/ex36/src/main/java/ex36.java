import utils.ConcurrentSetCollector;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toCollection;

/**
 * This program creates various {@link Set} objects containing the
 * unique words appearing in the complete work of William Shakespeare.
 * It also shows the difference in overhead between collecting results
 * in a parallel stream vs. sequential stream using concurrent and
 * non-concurrent collectors for various types of Java {@link Set}
 * implementations, including {@link HashSet} and {@link TreeSet}.
 */
@SuppressWarnings("ALL")
public class ex36 {
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
    private static final int sMAX_ITERATIONS = 5;

    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * A regular expression that matches whitespace and punctuation to
     * split the text of the complete works of Shakespeare into
     * individual words.
     */
    private static final String sSPLIT_WORDS =
        "[\\t\\n\\x0B\\f\\r'!()\"#&-.,;0-9:@<>\\[\\]}_|? ]+";

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
        // that use HashSets, which are unordered.
        runSetCollectorTests("HashSet",
                             HashSet::new,
                             ConcurrentHashMap::newKeySet,
                             ex36::timeStreamCollect);

        // Run tests that demonstrate the performance differences
        // between concurrent and non-concurrent collectors when
        // collecting results in Java sequential and parallel streams
        // that use TreeSets, which are ordered.
        runSetCollectorTests("TreeSet",
                             TreeSet::new,
                             TreeSet::new,
                             ex36::timeStreamCollect);

        // Print the results.
        printResults(getResults(true,
                                TestDataFactory
                                .getInput(sSHAKESPEARE_DATA_FILE,
                                          sSPLIT_WORDS,
                                          1_000_000),
                                ConcurrentSetCollector
                                .toSet(TreeSet::new)),
                     "Final results");

        System.out.println("Exiting the test program");
    }

    /**
     * Run tests that demonstrate the performance differences between
     * concurrent and non-concurrent collectors when collecting
     * results in Java sequential and parallel streams for various
     * types of Java {@link Set} types.
     * 
     * @param testType The type of test, i.e., HashSet or TreeSet
     * @param setSupplier A {@link Supplier} that creates the given
     *                    non-concurrent {@link Set}
     * @param concurrentSetSupplier A {@link Supplier} that creates
     *                              the given concurrent {@link Set}
     * @param collect A {@link Function} that performs the test using
     *                either a non-concurrent or concurrent {@link
     *                Collector
     */
    private static void runSetCollectorTests
        (String testType,
         Supplier<Set<String>> setSupplier,
         Supplier<Set<String>> concurrentSetSupplier,
         QuadFunction<String,
         Boolean,
         List<String>,
         Collector<String, ?, Set<String>>,
         Void> collect) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000, 1_000_000)

            // Run the tests for various input data sizes.
            .forEach (limit -> {
                    // Create a List of String objects containing
                    // 'limit' words from the works of Shakespeare.
                    List<String> arrayWords = TestDataFactory
                        .getInput(sSHAKESPEARE_DATA_FILE,
                                  // Split input into "words" by
                                  // ignoring whitespace.
                                  sSPLIT_WORDS,
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
                               toCollection(setSupplier));

                    // Collect results into a parallel stream via a
                    // non-concurrent collector.
                    collect
                        .apply("non-concurrent " + testType,
                               true,
                               arrayWords,
                               toCollection(setSupplier));

                    // Collect results into a sequential stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               false,
                               arrayWords,
                               ConcurrentSetCollector
                               .toSet(concurrentSetSupplier));

                    // Collect results into a parallel stream via a
                    // concurrent collector.
                    collect
                        .apply("concurrent " + testType,
                               true,
                               arrayWords,
                               ConcurrentSetCollector
                               .toSet(concurrentSetSupplier));

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
     * @param testType The type of test, i.e., HashSet or TreeSet
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
         Collector<String, ?, Set<String>> collector) {
        // Run the garbage collector before each test.
        System.gc();

        String testName =
            (parallel ? " parallel" : " sequential")
            + " "
            + testType;

        RunTimer
            // Time how long it takes to run the test.
            .timeRun(() -> {
                    // Create an empty list.
                    List<String> list = new ArrayList<>();

                    IntStream
                        // Iterate sMAX_ITERATIONS times.
                        .range(0, sMAX_ITERATIONS)
                    
                        // Perform the following action each iteration.
                        .forEach((i) -> list
                                 // Append new words to end of the list.
                                 .addAll(getResults(parallel, words, collector)));
                },
                testName);
        return null;
    }

    /**
     * Perform computations that create a {@link List} of unique words
     * in Shakespeare's works.
     * 
     * @param parallel If true then a parallel stream is used, else a
     *                 sequential stream is used
     * @param words A {@link List} of words to lowercase
     * @param collector The {@link Collector} used to combine the
     *                  results
     * @return A {@link List} containing the unique words in
     *         Shakespeare's works
     */
    private static Set<String> getResults
        (boolean parallel,
         List<String> words,
         Collector<String, ?, Set<String>> collector) {
        return // Convert List to parallel or sequental stream.
            (parallel 
             ? words.parallelStream()
             : words.stream())

            // Modify each word.
            .map(word ->
                 rot13(rot13(word.toUpperCase()).toLowerCase()))

            // Convert the Stream into a List.
            .collect(collector);
    }

    /**
     * Computes and returns the rot13 encoding of the {@code input}.
     *
     * @param input The {@link String} to encode
     * @return The rot13 encoding of the {@code input} {@link String}
     */
    static String rot13(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if       (c >= 'a' && c <= 'm') c += 13;
            else if  (c >= 'A' && c <= 'M') c += 13;
            else if  (c >= 'n' && c <= 'z') c -= 13;
            else if  (c >= 'N' && c <= 'Z') c -= 13;
            sb.append(c);
        }
        return sb.toString();
    }

    /**
     * Print the {@code results} of the {@code testName}.
     *
     * @param results The results of applying the test
     * @param testName The name of the test
     */
    private static void printResults(Set<String> results,
                                     String testName) {
        // Convert the first sMAX_WORDS elements of the Map contents
        // into a String.
        var allWords = results
            .stream()
            .map(Objects::toString)
            .toList();

        // Print the results.
        System.out.println("Results for "
                           + testName
                           + " of size "
                           + results.size()
                           + " was:\n"
                           + allWords);
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
                      sSPLIT_WORDS);

        // Create an empty list.
        List<String> list = new ArrayList<>();

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            // Append the new words to the end of the list.
            list.addAll(words
                        // Convert the list into a parallel stream
                        // (which uses a spliterator internally).
                        .parallelStream()

                        // Lowercase each String.
                        .map(word -> word.toString().toLowerCase())

                        // Collect the Stream into a List.
                        .toList());
    }
}
