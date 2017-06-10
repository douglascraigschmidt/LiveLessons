import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This example shows the difference in overhead for using a parallel
 * spliterator to split a Java LinkedList and an ArrayList into
 * chunks.  It also shows the difference in overhead between combining
 * LinkedList results in a parallel stream vs. sequential stream.
 */
public class ex14 {
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 50;

    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Create a list of strings containing all the words in the
        // complete works of Shakespeare.
        List<CharSequence> bardWords =             
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                     // Split input into "words" by
                                     // ignoring whitespace.
                                     "\\s+");

        List<CharSequence> linkedBardWords = 
            new LinkedList<>(bardWords);

        // Warm up the threads in the fork/join pool so the timing
        // results will be more accurate.
        warmUpForkJoinPool(new LinkedList<>(bardWords));

        // Compute/print the time required to split/count an ArrayList
        // via a parallel stream (and thus a parallel spliterator).
        // The performance of this test will be good since ArrayLists
        // have low split costs (just a few arithmetic operations and
        // an object creation) and also split evenly (leading to
        // balanced computation trees).
        timeParallelStreamCounting("ArrayList", bardWords);

        // Compute/print the time required to split/count a LinkedList
        // via a parallel stream (and thus a parallel spliterator).
        // The performance of this test will be worse than the
        // ArrayList test since a LinkedList splits poorly because
        // finding the midpoint requires traversing half the list, one
        // node at a time.
        timeParallelStreamCounting("LinkedList", linkedBardWords);

        // Compute/print the time required to join the LinkedList via
        // collect() and Collectors.joining() in a parallel stream.
        // The performance of this test will be poor due to the
        // overhead of combining/joining the various partial results.
        timeStreamJoining(true, linkedBardWords);

        // Compute/print the time required to join the LinkedList via
        // collect() and Collectors.joining() in a parallel stream.
        // The performance of this test will be better than the
        // parallel stream version above since there's less overhead
        // for combining/joining the various partial results.
        timeStreamJoining(false, linkedBardWords);
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool(List<CharSequence> words) {
        System.out.println("\n++Warming up the fork/join pool");

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            words
                // Convert the list into a parallel stream (which uses
                // a spliterator internally).
                .parallelStream()

                // Count the number of words in the stream.
                .count();

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Determines how long it takes to split the word list via a
     * parallel spliterator for various types of lists.
     */
    private static void timeParallelStreamCounting(String testName,
                                                   List<CharSequence> words) {
        System.out.println("\n++Timing the " 
                           + testName 
                           + " parallel implementation");

        // Record the start time.
        long startTime = System.nanoTime();

        long total = 0;

        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            total += words
                // Convert the list into a parallel stream (which uses
                // a spliterator internally).
                .parallelStream()

                // Count the number of words in the stream.
                .count();
        }

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to count "
                           + total
                           + " stream elements took "
                           + stopTime
                           + " milliseconds for "
                           + testName);

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Determines how long it takes to join the word list via
     * collect() and Collectors.joining() in a stream.  If @a parallel
     * is true then a parallel stream is used, else a sequential
     * stream is used.
     */
    private static void timeStreamJoining(boolean parallel, 
                                          List<CharSequence> words) {
        System.out.println("\n++Timing the "
                           + (parallel ? "parallel" : "sequential")
                           + "StreamJoining implementation");

        // Record the start time.
        long startTime = System.nanoTime();

        StringBuilder results = new StringBuilder();

        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            Stream<CharSequence> wordStream = words
                // Convert the list into a stream (which uses a
                // spliterator internally).
                .stream();

            if (parallel)
                //noinspection ResultOfMethodCallIgnored
                wordStream.parallel();

            results.append(wordStream
                           // Join all the words in the stream.
                           .collect(joining(" ")));
        }

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to join "
                           + results.toString().replaceAll("[^ ]", "").length()
                           + " stream elements took "
                           + stopTime
                           + " milliseconds");

        // Run the garbage collector after each test.
        System.gc();
    }
}

