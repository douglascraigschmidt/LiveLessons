import utils.ConcurrentSetCollector;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

/**
 * This program creates various {@link Set} objects containing the
 * unique words appearing in the complete work of William Shapespeare.
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
    private static final int sMAX_ITERATIONS = 10;

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
         Supplier<Set<CharSequence>> setSupplier,
         Supplier<Set<CharSequence>> concurrentSetSupplier,
         QuadFunction<String,
                      Boolean,
                      List<CharSequence>,
                      Collector<CharSequence, ?, Set<CharSequence>>,
                      Void> collect) {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1_000, 10_000, 100_000, 1_000_000)

            // Run the tests for various input data sizes.
            .forEach (limit -> {
                    // Create a List of CharSequences containing
                    // 'limit' words from the works of Shakespeare.
                    List<CharSequence> arrayWords = TestDataFactory
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
         List<CharSequence> words,
         Collector<CharSequence, ?, Set<CharSequence>> collector) {
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
                        Stream<CharSequence> wordStream = words
                            // Convert the list into a stream (which uses a
                            // spliterator internally).
                            .stream();

                        if (parallel)
                            // Convert to a parallel stream.
                            wordStream.parallel();

                        // A Set of unique words in Shakespeare's
                        // works.
                        Set<CharSequence> uniqueWords = wordStream
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
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        List<CharSequence> words = TestDataFactory
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

                        // Uppercase each string.  A "real"
                        // application would likely do something
                        // interesting with the words at this point.
                        .map(word -> word.toString().toUpperCase())

                        // Collect the stream into a list.
                        .collect(toList()));
    }
}
