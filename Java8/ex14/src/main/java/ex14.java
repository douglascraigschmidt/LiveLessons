import utils.RunTimer;
import utils.TestDataFactory;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * This example shows the difference in overhead/performance for using
 * parallel and sequential spliterators to split a Java {@link LinkedList}
 * and an {@link ArrayList} into chunks.
 */
@SuppressWarnings("ALL")
public class ex14 {
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

        // Run tests that demonstrate performance differences between
        // ArrayList and LinkedList spliterators.
        runSpliteratorTests();

        System.out.println("Exiting the test program");
    }

    /**
     * Run tests that demonstrate performance differences between
     * {@link ArrayList} and {@link LinkedList} spliterators.
     */
    private static void runSpliteratorTests() {
        Arrays
            // Create tests for different sizes of input data.
            .asList(1000, 10000, 100000, 1000000)

            // For each input data size run the following tests.
            .forEach (limit -> {
                    // Create a list of strings containing all the
                    // words in the complete works of Shakespeare.
                    List<String> arrayWords =
                        TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                                 // Split input into "words" by
                                                 // ignoring whitespace.
                                                 sWHITESPACE_AND_PUNCTUATION,
                                                 limit);

                    // Create a LinkedList from the ArrayList.
                    List<String> linkedWords =
                        new LinkedList<>(arrayWords);

                    // Print a message when the test starts.
                    System.out.println("Starting spliterator tests for "
                                       + arrayWords.size()
                                       + " words..");

                    // Compute the time required to split/uppercase a
                    // LinkedList via a sequential stream (and thus a
                    // sequential spliterator).
                    timeStreamUppercase("LinkedList",
                                        linkedWords,
                                        false);

                    // Compute the time required to split/uppercase an
                    // ArrayList via a sequential stream (and thus a
                    // sequential spliterator).
                    timeStreamUppercase("ArrayList",
                                        arrayWords,
                                        false);
                    
                    // Compute the time required to split/uppercase a
                    // LinkedList via a parallel stream (and thus a
                    // parallel spliterator).  The performance of this
                    // test should be worse than the ArrayList test
                    // since a LinkedList splits poorly.
                    timeStreamUppercase("LinkedList",
                                        linkedWords,
                                        true);

                    // Compute the time required to split/uppercase an
                    // ArrayList via a parallel stream (and thus a
                    // parallel spliterator).  The performance of this
                    // test should be good since ArrayLists have low
                    // split costs (just a few arithmetic operations
                    // and an object creation) and also split evenly
                    // (leading to balanced computation trees).
                    timeStreamUppercase("ArrayList",
                                        arrayWords,
                                        true);

                    // Print the results.
                    System.out.println("..printing results\n"
                                       + RunTimer.getTimingResults());
                });
    }

    /**
     * Determines how long it takes to split and uppercase the word
     * list via a parallel or sequential spliterator for various
     * types of lists.
     *
     * @param testName The name of the test being run
     * @param words The {@link List} of words to upper case
     * @param parallel True if stream should be parallel else false
     */
    private static void timeStreamUppercase(String testName,
                                            List<String> words,
                                            boolean parallel) {
        // Run the garbage collector before each test.
        System.gc();

        testName += parallel ? " parallel" : " sequential";

        RunTimer.timeRun(() -> {
                // Create an empty list.
                List<String> list = new ArrayList<>();

                IntStream
                    // Iterate sMAX_ITERATIONS times.
                    .range(0, sMAX_ITERATIONS)
                    
                    // Perform the following action each iteration.
                    .forEach((i) -> list
                             // Append new words to end of the list.
                             .addAll(StreamSupport
                                     // Convert List to sequential
                                     // or parallel stream.
                                     .stream(words.spliterator(),
                                             parallel)

                                     // Modify each string.
                                     .map(string ->
                                          rot13(string.toUpperCase()).toLowerCase())

                                     // Convert the Stream into a List.
                                     .toList()));
            },
            testName);
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
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("\n++Warming up the fork/join pool\n");

        List<String> words = Objects
            .requireNonNull(TestDataFactory
                            .getInput(sSHAKESPEARE_DATA_FILE,
                                      // Split input into "words" by
                                      // ignoring whitespace.
                                      sWHITESPACE_AND_PUNCTUATION));
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
                        .map(string -> string.toUpperCase())

                        // Collect the stream into a list.
                        .collect(toList()));
    }
}
