import utils.ConcurrentHashSetCollector;
import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * This example shows the difference in overhead for using a parallel
 * spliterator to split a Java LinkedList and an ArrayList into
 * chunks.  It also shows the difference in overhead between combining
 * and collecting LinkedList results in a parallel stream
 * vs. sequential stream using concurrent and non-concurrent
 * collectors.
 */
public class ex14 {
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 100;

    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        System.out.println("Entering the test program with "
                           + Runtime.getRuntime().availableProcessors()
                           + " cores available");

        // Warm up the threads in the fork/join pool so the timing
        // results will be more accurate.
        warmUpForkJoinPool();

        runSpliteratorTests();
        runJoiningTests();
        runCollectorTests();

        System.out.println("Exiting the test program");
    }

    /**
     * Run tests that demonstrate performance differences between
     * ArrayList and LinkedList spliterators.
     */
    private static void runSpliteratorTests() {
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

                    // Create a LinkedList from the ArrayList.
                    List<CharSequence> linkedWords = 
                        new LinkedList<>(arrayWords);

                    // Print a message when the test starts.
                    System.out.println("Starting spliterator tests for "
                                       + arrayWords.size()
                                       + " words..");

                    // Compute the time required to split/uppercase an
                    // ArrayList via a parallel stream (and thus a
                    // parallel spliterator).  The performance of this
                    // test will be good since ArrayLists have low
                    // split costs (just a few arithmetic operations
                    // and an object creation) and also split evenly
                    // (leading to balanced computation trees).
                    timeParallelStreamUppercase("ArrayList", arrayWords);

                    // Compute the time required to split/uppercase a
                    // LinkedList via a parallel stream (and thus a
                    // parallel spliterator).  The performance of this
                    // test will be worse than the ArrayList test
                    // since a LinkedList splits poorly.
                    timeParallelStreamUppercase("LinkedList", linkedWords);

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Run tests that demonstrate performance differences between
     * concurrent and non-concurrent techniques for joining results in
     * a stream.
     */
    private static void runJoiningTests() {
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

                    // Print a message when the test starts.
                    System.out.println("Starting joining tests for " 
                                       + arrayWords.size() 
                                       + " words..");

                    // Compute the time required to join arrayWords
                    // via collect() and Collectors.joining() in a
                    // sequential stream.  The performance of this
                    // test will be better than the parallel stream
                    // version below since there's less overhead for
                    // combining/joining the various partial results.
                    timeStreamJoining("ArrayList", false, arrayWords);

                    // Compute the time required to join arrayWords
                    // via collect() and Collectors.joining() in a
                    // parallel stream.  The performance of this test
                    // will be worse than the sequential stream
                    // version above due to the overhead of
                    // combining/joining the various partial results
                    // in parallel.
                    timeStreamJoining("ArrayList", true, arrayWords);

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Run tests that demonstrate the performance differences between
     * concurrent and non-concurrent techniques for collecting results
     * in a stream.
     */
    private static void runCollectorTests() {
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

                    // Print a message when the test starts.
                    System.out.println("Starting collector tests for " 
                                       + arrayWords.size() 
                                       + " words..");

                    // Compute the time required to collect partial
                    // results into a HashSet in a sequential stream.
                    // The performance of this test will be better
                    // than the parallel stream version below since
                    // there's less overhead collecting the various
                    // partial results into a HashSet.
                    timeStreamCollectToSet("ArrayList", false, arrayWords);

                    // Compute the time required to collect partial
                    // results into a HashSet in a parallel stream.
                    // The performance of this test will be worse than
                    // the sequential stream version above due to the
                    // overhead of collecting the various partial
                    // results into a HashSet in parallel.
                    timeStreamCollectToSet("ArrayList", true, arrayWords);

                    // Compute the time required to collect partial
                    // results into a ConcurrentHashSet in a
                    // sequential stream.  The performance of this
                    // test will be similar to the sequential stream
                    // version of timeStreamCollectToSet() above.
                    timeStreamCollectToConcurrentSet("ArrayList", false, arrayWords);

                    // Compute the time required to collect partial
                    // results into a ConcurrentHashSet in a parallel
                    // stream.  The performance of this test will be
                    // better than the parallel stream version of
                    // timeStreamCollectToSet() above since there's no
                    // overhead of collecting the partial results into
                    // a HashSet in parallel.
                    timeStreamCollectToConcurrentSet("ArrayList", true, arrayWords);

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        List<CharSequence> words =
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                     // Split input into "words"
                                     // by ignoring whitespace.
                                     "\\s+");
        // Create an empty list.
        List<String> list = new ArrayList<>();

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            // Append the new words to the end of the list.
            list.addAll(words
                        // Convert the list into a parallel stream
                        // (which uses a spliterator internally).
                        .parallelStream()

                        // Uppercase each string.
                        .map(CharSequence::toString)
                        .map(String::toUpperCase)

                        // Collect the stream into a list.
                        .collect(toList()));
    }

    /**
     * Determines how long it takes to split and uppercase the word
     * list via a parallel spliterator for various types of lists.
     */
    private static void timeParallelStreamUppercase(String testName,
                                                    List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName += " parallel";
        // System.out.println("Starting " + testName);

        RunTimer.timeRun(() -> {
                // Create an empty list.
                List<String> list = new ArrayList<>();

                for (int i = 0; i < sMAX_ITERATIONS; i++) 
                    // Append the new words to the end of the list.
                    list.addAll(words
                                // Convert the list into a parallel stream
                                // (which uses a spliterator internally).
                                .parallelStream()

                                // Uppercase each string.
                                .map(CharSequence::toString)
                                .map(String::toUpperCase)

                                // Collect the stream into a list.
                                .collect(toList()));
            },
            testName);
    }

    /**
     * Determines how long it takes to combine partial results in the
     * word list via collect() and Collectors.joining() in a stream.
     * If @a parallel is true then a parallel stream is used, else a
     * sequential stream is used.
     */
    private static void timeStreamJoining(String testName,
                                          boolean parallel,
                                          List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamJoining()";

        // System.out.println("Starting " + testName);

        RunTimer.timeRun(() -> {
                StringBuilder results = new StringBuilder();

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

                    // Join all the words in the stream.
                    CharSequence charSequence = wordStream
                        .collect(joining(" "));

                    // Add the joined results to the string builder.
                    results.append(charSequence);
                }},
            testName);
    }

    /**
     * Determines how long it takes to collect partial results into a
     * HashSet.  If @a parallel is true then a parallel stream is
     * used, else a sequential stream is used.
     */
    private static void timeStreamCollectToSet(String testName,
                                               boolean parallel,
                                               List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamCollectToSet()";

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
     * ConcurrentHashSet.  If @a parallel is true then a parallel
     * stream is used, else a sequential stream is used.
     */
    private static void timeStreamCollectToConcurrentSet(String testName,
                                                         boolean parallel,
                                                         List<CharSequence> words) {
        // Run the garbage collector before each test.
        System.gc();

        testName +=
            (parallel ? " parallel" : " sequential")
            + " timeStreamCollectToConcurrentSet()";

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
}
