import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

/**
 * This example shows the difference in overhead for using a parallel
 * spliterator to split a Java LinkedList and an ArrayList into
 * chunks.  It also shows the difference in overhead between combining
 * and collecting LinkedList results in a parallel stream
 * vs. sequential stream.  Finally, it illustrates why reduce()
 * shouldn't be used with mutable objects..
 */
public class ex14 {
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 1;

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
        List<CharSequence> allBardWords =             
            TestDataFactory.getInput(sSHAKESPEARE_DATA_FILE,
                                     // Split input into "words" by
                                     // ignoring whitespace.
                                     "\\s+");

        List<CharSequence> linkedBardWords = 
            new LinkedList<>(allBardWords);

        // Warm up the threads in the fork/join pool so the timing
        // results will be more accurate.
        warmUpForkJoinPool(new LinkedList<>(allBardWords));

        // Compute/print the time required to split/count an ArrayList
        // via a parallel stream (and thus a parallel spliterator).
        // The performance of this test will be good since ArrayLists
        // have low split costs (just a few arithmetic operations and
        // an object creation) and also split evenly (leading to
        // balanced computation trees).
        timeParallelStreamCounting("ArrayList", allBardWords);

        // Compute/print the time required to split/count a LinkedList
        // via a parallel stream (and thus a parallel spliterator).
        // The performance of this test will be worse than the
        // ArrayList test since a LinkedList splits poorly because
        // finding the midpoint requires traversing half the list, one
        // node at a time.
        timeParallelStreamCounting("LinkedList", linkedBardWords);

        // Compute/print the time required to join the LinkedList via
        // collect() and Collectors.joining() in a sequential stream.
        // The performance of this test will be better than the
        // parallel stream version above since there's less overhead
        // for combining/joining the various partial results.
        timeStreamJoining(false, linkedBardWords);

        // Compute/print the time required to join the LinkedList via
        // collect() and Collectors.joining() in a parallel stream.
        // The performance of this test will be poor due to the
        // overhead of combining/joining the various partial results
        // in parallel.
        timeStreamJoining(true, linkedBardWords);

        // Compute/print the time required to collect partial results
        // into a TreeSet in a sequential stream.  The performance of
        // this test will be better than the parallel stream version
        // above since there's less overhead collecting the various
        // partial results into a TreeSet.
        timeStreamCollectToSet(false, linkedBardWords);

        // Compute/print the time required to collect partial results
        // into a TreeSet in a parallel stream.  The performance of
        // this test will be poor due to the overhead of collecting
        // the various partial results into a TreeSet in parallel.
        timeStreamCollectToSet(true, linkedBardWords);

        // Compute/print the time required to reduce partial results
        // into a string using a sequential stream.  Since a
        // sequential stream is used the results of this test will be
        // correct even though a mutable object (StringBuilder) is
        // used with reduce().
        timeBuggyStreamReduce(false, linkedBardWords);

        // Compute/print the time required to reduce partial results
        // into a string using a parallel stream.  The results of this
        // test will be incorrect due to the use of a mutable object
        // (StringBuilder) with reduce(), which performs immutable
        // reduction.
        timeBuggyStreamReduce(true, linkedBardWords);
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
                           + " all the words in Shakespeare's works took "
                           + stopTime
                           + " milliseconds for "
                           + testName);

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Determines how long it takes to combine partial results in the
     * word list via collect() and Collectors.joining() in a stream.
     * If @a parallel is true then a parallel stream is used, else a
     * sequential stream is used.
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
                // Convert to a parallel stream.
                wordStream.parallel();

            // A "real" application would likely do something
            // interesting with the words at this point.

            // Join all the words in the stream.
            CharSequence charSequence = wordStream
                .collect(joining(" "));

            // Add the joined results to the string builder.
            results.append(charSequence);
        }

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to join "
                           + (results.toString().replaceAll("[^ ]", "").length() + 1)
                           + " words in Shakespeare's works took "
                           + stopTime
                           + " milliseconds");

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Determines how long it takes to collect partial results into a
     * TreeSet.  If @a parallel is true then a parallel stream is
     * used, else a sequential stream is used.
     */
    private static void timeStreamCollectToSet(boolean parallel, 
                                               List<CharSequence> allWords) {
        System.out.println("\n++Timing the "
                           + (parallel ? "parallel" : "sequential")
                           + "StreamCollectToSet implementation");

        // Record the start time.
        long startTime = System.nanoTime();

        Set<CharSequence> uniqueWords = null;

        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            Stream<CharSequence> wordStream = allWords
                // Convert the list into a stream (which uses a
                // spliterator internally).
                .stream();

            if (parallel)
                // Convert to a parallel stream.
                wordStream.parallel();

            // A "real" application would likely do something
            // interesting with the words at this point.

            // Collect all the unique words in Shakespeare's works
            // into an ordered TreeSet.
            uniqueWords = wordStream
                .collect(toCollection(TreeSet::new));
        }

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                           + uniqueWords.size()
                           + " unique words in Shakespeare's works took "
                           + stopTime
                           + " milliseconds");

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Determines how long it takes to collect partial results into a
     * TreeSet.  If @a parallel is true then a parallel stream is
     * used, else a sequential stream is used.
     */
    private static void timeBuggyStreamReduce(boolean parallel, 
                                              List<CharSequence> allWords) {
        System.out.println("\n++Timing the "
                           + (parallel ? "parallel" : "sequential")
                           + "BuggyStreamReduce implementation");

        // Record the start time.
        long startTime = System.nanoTime();

        StringBuilder wordBuilder = new StringBuilder();

        try {
            for (int i = 0; i < sMAX_ITERATIONS; i++) {
                Stream<CharSequence> wordStream = allWords
                    // Convert the list into a stream (which uses a
                    // spliterator internally).
                    .stream();

                if (parallel)
                    // Convert to a parallel stream.
                    wordStream.parallel();

                // A "real" application would likely do something
                // interesting with the words at this point.

                StringBuilder stringBuilder = wordStream
                    // Use reduce() to append all the words in
                    // the stream.  This implementation will
                    // fail horribly when used with a parallel
                    // stream since reduce() expects to do
                    // "immutable" reduction.
                    .reduce(new StringBuilder(),
                            (sb, s) -> sb.append(s).append(" "),
                            StringBuilder::append);

                // Append all the words in Shakespeare's works into a
                // single string builder.
                wordBuilder
                    .append(stringBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        String words = wordBuilder.toString();

        System.out.println("The time to collect "
                           + words.replaceAll("[^ ]", "").length()
                           + " words in Shakespeare's works took "
                           + stopTime
                           + " milliseconds");

        // Run the garbage collector after each test.
        System.gc();
    }
}
