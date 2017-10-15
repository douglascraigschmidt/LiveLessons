import utils.FuturesCollector;
import utils.StreamsUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;

/**
 * This program shows how to wait for the results of a stream of
 * completable futures using (1) a custom collector and (2) the
 * StreamsUtils.joinAll() method (which is a wrapper for
 * CompletableFuture.allOf()).
 */
public class ex18 {
    /**
     * Default factorial number.  
     */
    private static final int sDEFAULT_N = 1000;

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Starting Factorial Tests");

        // Create a new test object.
        ex18 test = new ex18();

        // Create a list containing all the factorial methods.
        List<Function<BigInteger, BigInteger>> factList =
            Arrays.asList(SynchronizedParallelFactorial::factorial,
                          SequentialStreamFactorial::factorial,
                          ParallelStreamFactorial2::factorial,
                          ParallelStreamFactorial3::factorial);

        // Initialize to the default value.
        final BigInteger n = (args.length > 0)
                ? BigInteger.valueOf(Long.valueOf(args[0]))
                : BigInteger.valueOf(sDEFAULT_N);

        // Test the StreamsUtils.joinAll() method.
        test.testJoinAll(factList, n);

        // Test the FuturesCollector.
        test.testFuturesCollector(factList, n);

        System.out.println("Ending Factorial Tests");
    }

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

            /**
             * Synchronize get to ensure visibility of the data.
             */
            BigInteger get() {
                synchronized (this) {
                    return mTotal;
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
            return t.get();
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
     * Test the StreamsUtils.joinAll() method.
     */
    private void testJoinAll
        (List<Function<BigInteger, BigInteger>> factList,
         BigInteger n) {
        System.out.println("Testing JoinAll");

        List<CompletableFuture<BigInteger>> resultsList = factList
            // Convert the list into stream.
            .stream()
            
            // Apply each factorial method asynchronously in the
            // common fork-join pool.
            .map(func
                 -> CompletableFuture.supplyAsync(()
                                                  -> func.apply(n)))

            // Trigger intermediate operations and return a list of
            // completable futures.
            .collect(toList());

        StreamsUtils
            // Create a single future that will complete when all
            // futures in resultsList complete.
            .joinAll(resultsList)

            // Wait for the single future to complete.
            .join()

            // Printout all the results.
            .forEach(System.out::println);
    }

    /**
     * Test the FuturesCollector.
     */
    private void testFuturesCollector
        (List<Function<BigInteger, BigInteger>> factList,
         BigInteger n) {
        System.out.println("Testing FuturesCollector");

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

            // Trigger intermediate processing and return a single
            // completable future.
            .collect(FuturesCollector.toFuture());

        resultsFuture
            // Wait for the single future to complete.
            .join()

            // Printout all the results.
            .forEach(System.out::println);
    }
}
