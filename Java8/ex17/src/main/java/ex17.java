import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * This example shows various issues associated with using the Java
 * stream reduce() terminal operation, including the need to use the
 * correct identity value and to ensure operations are associative.
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

        // Run the product reduction test in parallel with an
        // incorrect identity value.
        testSum(0L, true);

        // Run the product reduction test sequentially with the
        // correct identity value.
        testProd(1L, false);

        // Run the product reduction test in parallel with the
        // correct identity value.
        testProd(1L, true);

        // Run the product reduction test sequentially with an
        // incorrect identity value.
        testProd(0L, false);
    }

    /**
     * Print out the results of subtracting the first 100 numbers.  If
     * {@code parallel} is true then a parallel stream is used, else a
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
     * Print out the results of summing the first 100 numbers, using
     * {@code identity} as the initial value of the summation.  If
     * {@code parallel} is true then a parallel stream is used, else a
     * sequential stream is used.  When a sequential or parallel
     * stream is used with an identity of 0 the results of this test
     * will be correct.  When a sequential or parallel stream is used
     * with an identity of 1, however, results of this test will be
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
     * Print out the results of multiplying the first 100 numbers,
     * using {@code identity} as the initial value of the summation.
     * If {@code parallel} is true then a parallel stream is used,
     * else a sequential stream is used.  When a sequential or
     * parallel stream is used with an identity of 1 the results of
     * this test will be correct.  When a sequential or parallel
     * stream is used with an identity of 0, however, results of this
     * test will be incorrect.
     */
    private static void testProd(long identity,
                                 boolean parallel) {
        LongStream rangeStream = LongStream
            .rangeClosed(1, 10);

        if (parallel)
            rangeStream.parallel();

        long product = rangeStream
            .reduce(identity,
                    (x, y) -> x * y);

        System.out.println((parallel ? "Parallel" : "Sequential")
                           + " product of first 10 numbers with identity "
                           + identity
                           + " = "
                           + product);
    }
}
