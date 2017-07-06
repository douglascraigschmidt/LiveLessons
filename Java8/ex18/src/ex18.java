import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;

/**
 * This program shows how to use a custom collector in conjunction
 * with a stream of completable futures.
 */
public class ex18 {
    /**
     * Default factorial number.  
     */
    private static final int sDEFAULT_N = 1000;

    /**
     * This class demonstrates how a synchronized statement can avoid
     * race conditions when state is shared between Java threads.
     */
    private static class SynchronizedParallelFactorial {
        /**
         * This class keeps a running total of the factorial and
         * provides a synchronized method for multiplying this running
         * total with a value n.
         */
        static class Total {
            /**
             * The running total of the factorial.
             */
            BigInteger mTotal = BigInteger.ONE;

            /**
             * Multiply the running total by @a n.  This method is
             * synchronized to avoid race conditions.
             */
            void multiply(BigInteger n) {
                synchronized (this) {
                    mTotal = mTotal.multiply(n);
                }
            }
        }

        /**
         * Return the factorial for the given @a n using a parallel
         * stream and the forEach() terminal operation.
         */
        static BigInteger factorial(BigInteger n) {
            Total t = new Total();

            LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n.longValue())

                // Run the forEach() terminal operation concurrently.
                .parallel()

                // Create a BigInteger from the long value.
                .mapToObj(BigInteger::valueOf)

                // Multiple the latest value in the range by the
                // running total (properly synchronized).
                .forEach(t::multiply);

            // Return the total.
            return t.mTotal;
        }
    }

    /**
     * This class demonstrates how the two parameter Java 8 reduce()
     * operation avoids sharing state between Java threads altogether.
     */
    private static class ParallelStreamFactorial2 {
        /**
         * Return the factorial for the given @a n using a parallel
         * stream and the reduce() terminal operation.
         */
        static BigInteger factorial(BigInteger n) {
            return LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n.longValue())

                // Run the reduce() terminal operation concurrently.
                .parallel()

                // Create a BigInteger from the long value.
                .mapToObj(BigInteger::valueOf)

                // Use the two parameter variant of reduce() to
                // perform a reduction on the elements of this stream
                // to compute the factorial.  Note that there's no
                // shared state at all!
                .reduce(BigInteger.ONE, BigInteger::multiply);
        }
    }

    /**
     * This class demonstrates how the three parameter Java 8 reduce()
     * operation avoids sharing state between Java threads altogether.
     */
    private static class ParallelStreamFactorial3 {
        /**
         * Return the factorial for the given @a n using a parallel
         * stream and the reduce() terminal operation.
         */
        static BigInteger factorial(BigInteger n) {
            return LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n.longValue())

                // Run the reduce() terminal operation concurrently.
                .parallel()

                // Create a BigInteger from the long value.
                .mapToObj(BigInteger::valueOf)

                // Use the three parameter variant of reduce() to
                // perform a reduction on the elements of this stream
                // to compute the factorial.  Note that there's no
                // shared state at all!
                .reduce(BigInteger.ONE,
                        BigInteger::multiply,
                        BigInteger::multiply);
        }
    }

    /**
     * This class demonstrates a baseline sequential factorial
     * implementation.
     */
    private static class SequentialStreamFactorial {
        /**
         * Return the factorial for the given @a n using a sequential
         * stream and the reduce() terminal operation.
         */
        static BigInteger factorial(BigInteger n) {
            return LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n.longValue())

                // Create a BigInteger from the long value.
                .mapToObj(BigInteger::valueOf)

                // Performs a reduction on the elements of this stream
                // to compute the factorial.
                .reduce(BigInteger.ONE, BigInteger::multiply);
        }
    }

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Starting Factorial Tests");

        // Initialize to the default value.
        final BigInteger n = (args.length > 0)
            ? BigInteger.valueOf(Long.valueOf(args[0]))
            : BigInteger.valueOf(sDEFAULT_N);

        // Create a new test object.
        ex18 test = new ex18();

        // Create a list containing all the factorial methods.
        List<Function<BigInteger, BigInteger>> factList =
            Arrays.asList(SynchronizedParallelFactorial::factorial,
                          SequentialStreamFactorial::factorial,
                          ParallelStreamFactorial2::factorial,
                          ParallelStreamFactorial3::factorial);

        // Create a single completable future to a list of completed
        // BigIntegers.
        CompletableFuture<List<BigInteger>> resultsFuture = factList
            // Convert the list into a parallel stream.
            .parallelStream()
            
            // Apply each factorial method asynchronously in the
            // common fork-join pool.
            .map(func
                 -> CompletableFuture.supplyAsync(()
                                                  -> func.apply(n)))

            // Collect the results into a single completable future.
            .collect(new FuturesCollector<>());

        // Wait for the single future to complete and then printout
        // all the results.
        resultsFuture.join().forEach(System.out::println);

        System.out.println("Ending Factorial Tests");
    }
}

