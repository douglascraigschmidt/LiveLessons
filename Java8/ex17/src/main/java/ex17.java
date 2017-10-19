import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;

/**
 * This example shows various issues associated with using the Java 8
 * stream reduce() terminal operation, including the need to use the
 * correct identity value and to ensure operations are associative.
 * It also demonstrates what goes wrong when reduce() performs a
 * mutable reduction on a parallel stream.
 */
public class ex17 {
    /**
     * Main entry point into the program.
     */
    public static void main(String[] argv) {
        // Run the difference reduction test sequentially.
        testDifferenceReduce(false);

        // Run the difference reduction test in parallel.
        testDifferenceReduce(true);

        // Run the summation reduction test sequentially with the
        // correct identity value.
        testSum(0L, false);

        // Run the summation reduction test in parallel with the
        // correct identity value.
        testSum(0L, true);

        // Run the summation reduction test sequentially with an
        // incorrect identity value.
        testSum(1L, false);

        // Run the summation reduction test in parallel with an
        // incorrect identity value.
        testSum(1L, true);

        // Reduce partial results into a string using a sequential
        // stream and the three parameter version of reduce().
        buggyStreamReduce3(false);

        // Reduce partial results into a string using a parallel
        // stream and the three parameter version of reduce().
        buggyStreamReduce3(true);
    }

    /**
     * Print out the results of subtracting the first 100 numbers.
     * If @a parallel is true then a parallel stream is used, else a
     * sequential stream is used.  The results for each of these tests
     * will differ since subtraction is not associative.
     */
    private static void testDifferenceReduce(boolean parallel) {
        LongStream rangeStream = LongStream
            .rangeClosed(1, 100);

        if (parallel)
            rangeStream.parallel();

        long difference = rangeStream
            .reduce(1L,
                    (x, y) -> x - y);

        System.out.println((parallel ? "Parallel" : "Sequential")
                           + " difference of first 100 numbers = "
                           + difference);
    }

    /**
     * Print out the results of summing the first 100 numbers,
     * using @a identity as the initial value of the summation.  If @a
     * parallel is true then a parallel stream is used, else a
     * sequential stream is used.  When a sequential or parallel
     * stream is used with an identity of 0 the results of this test
     * will be correct.  When a sequential or parallel stream is used
     * with an identity of 0, however, results of this test will be
     * incorrect.
     */
    private static void testSum(long identity,
                                boolean parallel) {
        LongStream rangeStream = LongStream
            .rangeClosed(1, 100);

        if (parallel)
            rangeStream.parallel();

        long sum = rangeStream
            .reduce(identity,
                    // Could also use (x, y) -> x + y
                    Math::addExact);

        System.out.println((parallel ? "Parallel" : "Sequential")
                           + " sum of first 100 numbers with identity "
                           + identity
                           + " = "
                           + sum);
    }

    /**
     * Reduce partial results into a StringBuilder using the three
     * parameter version of reduce().  If @a parallel is true then a
     * parallel stream is used, else a sequential stream is used.
     * When a sequential stream is used the results of this test will
     * be correct even though a mutable object (StringBuilder) is used
     * with reduce().  When a parallel stream is used, however, the
     * results of this test will be incorrect due to the use of a
     * mutable object (StringBuilder) with reduce(), which performs
     * immutable reduction.
     */
    private static void buggyStreamReduce3(boolean parallel) {
        System.out.println("\n++Running the "
                           + (parallel ? "parallel" : "sequential")
                           + "BuggyStreamReduce3 implementation");

        List<String> allWords =
            Arrays.asList("The quick brown fox jumps over the lazy dog\n",
                          "A man, a plan, a canal: Panama\n",
                          "Now is the time for all good people " 
                          + "to come to the aid of their party");

        // Record the start time.
        long startTime = System.nanoTime();

        Stream<String> wordStream = allWords
            // Convert the list into a stream (which uses a
            // spliterator internally).
            .stream();

        if (parallel)
            // Convert to a parallel stream.
            wordStream.parallel();

        // A "real" application would likely do something
        // interesting with the words at this point.

        // Create a string that contains all the strings appended together.
        String words = wordStream
            // Use reduce() to append all the strings in the stream.
            // This implementation will fail when used with a parallel
            // stream since reduce() expects to do "immutable"
            // reduction, but there's just a single StringBuilder!
            .reduce(new StringBuilder(),
                    StringBuilder::append,
                    StringBuilder::append)
            // Create a string.
            .toString();


        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("The time to collect "
                           + words.split("\\s+").length
                           + " words took "
                           + stopTime
                           + " milliseconds.  Here are the words:\n"
                           + words);
    }
}
