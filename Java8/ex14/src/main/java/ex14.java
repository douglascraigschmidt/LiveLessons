import java.util.LinkedList;
import java.util.List;

/**
 * This example shows the difference in overhead for using a
 * spliterator to split a Java LinkedList and an ArrayList into
 * chunks.
 */
public class ex14 {
    /**
     * Number of iterations to run the timing tests.
     */
    private static final int sMAX_ITERATIONS = 1000;

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

        // Warm up the threads in the fork/join pool so the timing
        // results will be more accurate.
        warmUpForkJoinPool(new LinkedList<>(bardWords));

        // Compute/print the time required to split/count an ArrayList
        // via a parallel stream (and thus a parallel spliterator).
        // The performance of this test will be good since ArrayLists
        // have low split costs (just a few arithmetic operations and
        // an object creation) and also split evenly (leading to
        // balanced computation trees).
        timeParallelStream("ArrayList", bardWords);

        // Compute/print the time required to split/count a LinkedList
        // via a parallel stream (and thus a parallel spliterator).
        // The performance of this test will be worse than the
        // ArrayList test since a LinkedList splits poorly because
        // finding the midpoint requires traversing half the list, one
        // node at a time.
        timeParallelStream("LinkedList", new LinkedList<>(bardWords));
    }

    /**
     * Warm up the threads in the fork/join pool so the timing results
     * will be more accurate.
     */
    private static void warmUpForkJoinPool(List<CharSequence> quote) {
        System.out.println("\n++Warming up the fork/join pool");

        for (int i = 0; i < sMAX_ITERATIONS; i++) 
            quote.parallelStream().count();

        // Run the garbage collector after each test.
        System.gc();
    }

    /**
     * Determine how long it takes to split the quote list via a
     * parallel spliterator for various types of lists.
     */
    private static void timeParallelStream(String testName, List<CharSequence> quote) {
        System.out.println("\n++Timing the " 
                           + testName 
                           + " parallel implementation");

        // Record the start time.
        long startTime = System.nanoTime();

        long total = 0;

        for (int i = 0; i < sMAX_ITERATIONS; i++) {
            total += quote
                    .parallelStream().count();
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
}

