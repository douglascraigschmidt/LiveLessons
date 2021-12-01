import utils.*;

import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * This example shows the difference in overhead between combining and
 * collecting results in a parallel stream vs. sequential stream using
 * concurrent and non-concurrent collectors for various types of Java Set
 * implementations.
 */
@SuppressWarnings("ALL")
public class ex36 {
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
        // that use unordered HashSets.
        runCollectorTestsUnordered();

        // Run tests that demonstrate the performance differences
        // between concurrent and non-concurrent collectors when
        // collecting results in Java sequential and parallel streams
        // that use ordered TreeSets.
        runCollectorTestsOrdered();

        System.out.println("Exiting the test program");
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        List<CharSequence> words = Objects
            .requireNonNull(TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                                     // Split input into "words"
                                                     // by ignoring whitespace.
                                                     "\\s+"));
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
                        .map(charSeq -> charSeq.toString().toUpperCase())

                        // Collect the stream into a list.
                        .collect(toList()));
    }

    /**
     * Run tests that demonstrate the performance differences between
     * concurrent and non-concurrent collectors when collecting
     * results in Java sequential and parallel streams that use
     * unordered HashSets.
     */
    private static void runCollectorTestsUnordered() {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1000, 10000, 100000, 1000000)

            // For each input data size run the following tests.
            .forEach (limit -> {
                    // Create a list of strings containing all the
                    // words in the complete works of Shakespeare.
                    List<CharSequence> arrayWords =
                        TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                                 // Split input into "words" by
                                                 // ignoring whitespace.
                                                 "\\s+",
                                                 limit);

                    assert arrayWords != null;

                    // Print a message when the test starts.
                    System.out.println("Starting collector tests for "
                                       + arrayWords.size() 
                                       + " words that will be unordered..");

                    // Compute the time required to collect partial
                    // results into a HashSet in a sequential stream.
                    // The performance of this test will be better
                    // than the parallel stream version below since
                    // there's less overhead collecting the various
                    // partial results into a HashSet.
                    timeStreamCollectToHashSet("ArrayList",
                                               false,
                                               arrayWords);

                    // Compute the time required to collect partial
                    // results into a HashSet in a parallel stream.
                    // The performance of this test will be worse than
                    // the sequential stream version above due to the
                    // overhead of collecting the various partial
                    // results into a HashSet in parallel.
                    timeStreamCollectToHashSet("ArrayList",
                                               true,
                                               arrayWords);

                    // Compute the time required to collect partial
                    // results into a ConcurrentHashSet in a
                    // sequential stream.  The performance of this
                    // test will be similar to the sequential stream
                    // version of timeStreamCollectToHashSet() above.
                    timeStreamCollectToConcurrentHashSet("ArrayList",
                                                         false,
                                                         arrayWords);

                    // Compute the time required to collect partial
                    // results into a ConcurrentHashSet in a parallel
                    // stream.  The performance of this test will be
                    // better than the parallel stream version of
                    // timeStreamCollectToHashSet() above since there's no
                    // overhead of collecting the partial results into
                    // a HashSet in parallel.
                    timeStreamCollectToConcurrentHashSet("ArrayList",
                                                         true,
                                                         arrayWords);

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Run tests that demonstrate the performance differences between
     * concurrent and non-concurrent collectors when collecting
     * results in Java sequential and parallel streams that use
     * ordered TreeSets.
     */
    private static void runCollectorTestsOrdered() {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1000, 10000, 100000, 1000000)

            // For each input data size run the following tests.
            .forEach (limit -> {
                    // Create a list of strings containing all the
                    // words in the complete works of Shakespeare.
                    List<CharSequence> arrayWords =
                        TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                                 // Split input into "words" by
                                                 // ignoring whitespace.
                                                 "\\s+",
                                                 limit);

                    assert arrayWords != null;

                    // Print a message when the test starts.
                    System.out.println("Starting collector tests for "
                                       + arrayWords.size()
                                       + " words that will be ordered..");

                    // Compute the time required to collect partial
                    // results into a TreeSet in a sequential stream.
                    // The performance of this test will be better
                    // than the parallel stream version below since
                    // there's less overhead collecting the various
                    // partial results into a TreeSet.
                    timeStreamCollectToTreeSet("ArrayList",
                                               false,
                                               arrayWords);

                    // Compute the time required to collect partial
                    // results into a TreeSet in a parallel stream.
                    // The performance of this test will be worse than
                    // the sequential stream version above due to the
                    // overhead of collecting the various partial
                    // results into a TreeSet in parallel.
                    timeStreamCollectToTreeSet("ArrayList",
                                               true,
                                               arrayWords);

                    // Compute the time required to collect partial
                    // results into a ConcurrentHashSet in a
                    // sequential stream.  The performance of this
                    // test will be similar to the sequential stream
                    // version of timeStreamCollectToTreeSet() above.
                    timeStreamCollectToConcurrentTreeSet("ArrayList",
                                                         false,
                                                         arrayWords);

                    // Compute the time required to collect partial
                    // results into a ConcurrentHashSet in a parallel
                    // stream.  The performance of this test will be
                    // better than the parallel stream version of
                    // timeStreamCollectToTreeSet() above since
                    // there's no overhead of collecting the partial
                    // results into a HashSet in parallel.
                    timeStreamCollectToConcurrentTreeSet("ArrayList",
                                                         true,
                                                         arrayWords);

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Determines how long it takes to collect partial results into a
     * {@link HashSet} using a non-concurrent collector.  If {@code
     * parallel} is true then a parallel stream is used, else a
     * sequential stream is used.
     */
    private static void timeStreamCollectToHashSet(String testName,
                                                   boolean parallel,
                                                   List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamCollectToHashSet()";

        // System.out.println("Starting " + testName);

        RunTimer.timeRun(() -> {
                Set<CharSequence> uniqueWords = null;

                for (int i = 0; i < sMAX_ITERATIONS; i++) {
                    Stream<CharSequence> wordStream = words
                        // Convert the list into a stream (which uses a
                        // spliterator internally).
                        .stream();

                    if (parallel)
                        // Convert to a parallel stream.
                        wordStream.parallel();

                    // A "real" application would likely do something
                    // interesting with the words at this point.

                    // A set of unique words in Shakespeare's works.
                    uniqueWords = wordStream
                        // Map each string to lower case.  A "real" application
                        // would likely do something interesting with the words at
                        // this point.
                        .map(charSeq -> charSeq.toString().toLowerCase())

                        // Trigger intermediate processing and collect unique
                        // words into a HashSet.
                        .collect(toCollection(HashSet::new));
                }},
            testName);
    }

    /**
     * Determines how long it takes to collect partial results into a
     * {@link TreeSet} using a non-concurrent collector.  If {@code
     * parallel} is true then a parallel stream is used, else a
     * sequential stream is used.
     */
    private static void timeStreamCollectToTreeSet(String testName,
                                                   boolean parallel,
                                                   List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamCollectToTreeSet()";

        // System.out.println("Starting " + testName);

        RunTimer.timeRun(() -> {
                Set<CharSequence> uniqueWords = null;

                for (int i = 0; i < sMAX_ITERATIONS; i++) {
                    Stream<CharSequence> wordStream = words
                        // Convert the list into a stream (which uses a
                        // spliterator internally).
                        .stream();

                    if (parallel)
                        // Convert to a parallel stream.
                        wordStream.parallel();

                    // A "real" application would likely do something
                    // interesting with the words at this point.

                    // A set of unique words in Shakespeare's works.
                    uniqueWords = wordStream
                        // Map each string to lower case.  A "real"
                        // application would likely do something
                        // interesting with the words at this point.
                        .map(charSeq -> charSeq.toString().toLowerCase())

                        // Trigger intermediate processing and collect
                        // unique words into a TreeSet.
                        .collect(toCollection(TreeSet::new));
                }},
            testName);
    }

    /**
     * Determines how long it takes to collect partial results into a
     * {@link ConcurrentHashSet} using a concurrent collector.  If
     * {@code parallel} is true then a parallel stream is used, else a
     * sequential stream is used.
     */
    private static void timeStreamCollectToConcurrentHashSet(String testName,
                                                             boolean parallel,
                                                             List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamCollectToConcurrentHashSet()";

        RunTimer.timeRun(() -> {
                Set<CharSequence> uniqueWords = null;

                for (int i = 0; i < sMAX_ITERATIONS; i++) {
                    Stream<CharSequence> wordStream = words
                        // Convert the list into a stream (which uses a
                        // spliterator internally).
                        .stream();

                    if (parallel)
                        // Convert to a parallel stream.
                        wordStream.parallel();

                    // A set of unique words in Shakespeare's works.
                    uniqueWords = wordStream
                        // Map each string to lower case.  A "real" application
                        // would likely do something interesting with the words at
                        // this point.
                        .map(charSeq -> charSeq.toString().toLowerCase())

                        // Trigger intermediate processing and collect unique
                        // words into a ConcurrentHashSet.
                        .collect(ConcurrentHashSetCollector.toSet());
                }},
            testName);
    }

    /**
     * Determines how long it takes to collect partial results into a
     * {@link ConcurrentTreeSet} using a concurrent collector.  If
     * {@code parallel} is true then a parallel stream is used, else a
     * sequential stream is used.
     */
    private static void timeStreamCollectToConcurrentTreeSet(String testName,
                                                             boolean parallel,
                                                             List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamCollectToConcurrentTreeSet()";

        RunTimer.timeRun(() -> {
                Set<CharSequence> uniqueWords = null;

                for (int i = 0; i < sMAX_ITERATIONS; i++) {
                    Stream<CharSequence> wordStream = words
                        // Convert the list into a stream (which uses
                        // a spliterator internally).
                        .stream();

                    if (parallel)
                        // Convert to a parallel stream.
                        wordStream.parallel();

                    // A set of unique words in Shakespeare's works.
                    uniqueWords = wordStream
                        // Map each string to lower case.  A "real"
                        // application would likely do something
                        // interesting with the words at this point.
                        .map(charSeq -> charSeq.toString().toLowerCase())

                        // Trigger intermediate processing and collect
                        // unique words into a TreeSet.
                        .collect(ConcurrentTreeSetCollector.toSet());
                }},
            testName);
    }
}
