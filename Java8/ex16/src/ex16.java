import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.LongStream;

/**
 * This program implements various ways of computing factorials for
 * BigIntegers to demonstrate the performance of alternative parallel
 * and sequential algorithms, as well as the dangers of sharing
 * unsynchronized state between threads.  A conventional Java 8
 * parallel streams solution would look like this for 'n' of type
 * long:
 *
 * LongStream
 *   // Generate long values from 1 to n
 *   .rangeClosed(1, n)
 *
 *   // Run the stream in parallel.
 *   .parallel()
 *
 *   // Reduce the results of the parallel chunks.
 *   .reduce(1, (a, b) -> a * b);
 *
 * However, due to the expotential++ growth rate of factorials we use
 * a BigInteger result instead of a long result.  A more interesting
 * (albeit more complicated) solution that also uses BigIntegers for
 * the type of 'n' appears at https://github.com/jevad/egfactorial.
 */
public class ex16 {
    /**
     * Max number of times to run the tests.
     */
    private static final int sMAX_ITERATIONS = 10000;

    /**
     * Default factorial number.  
     */
    private static final int sDEFAULT_N = 400;

    /**
     * This class demonstrates how race conditions can occur when
     * state is shared between Java threads.
     */
    private static class BuggyFactorial {
        /**
         * This class keeps a running total of the factorial and
         * provides a (buggy) method for multiplying this running
         * total with a value n.
         */
        static class Total {
            /**
             * The running total of the factorial.
             */
            BigInteger mTotal = BigInteger.ONE;

            /**
             * Multiply the running total by @a n.  This method is not
             * synchronized, so it may incur race conditions.
             */
            void multiply(BigInteger n) {
                mTotal = mTotal.multiply(n);
            }
        }  

        /**
         * Attempts to return the factorial for the given @a n.  There
         * are race conditions wrt accessing shared state, however, so
         * the result may not always be correct.
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
                // running total (not properly synchronized).
                .forEach(t::multiply);

            // Return the total, which is also not properly
            // synchronized.
            return t.mTotal;
        }
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
     * Run the given @a factorial test and print the result.
     */
    private <T> void runTest(String factorialTest,
                             Function<T, T> factorial,
                             T n) {
        // Record the start time.
        long startTime = System.nanoTime();

        for (int i = 0; i < sMAX_ITERATIONS; i++)
            factorial.apply(n);

        // Record the stop time.
        long stopTime = (System.nanoTime() - startTime) / 1_000_000;

        System.out.println("It took "
                           + stopTime
                           + " milliseconds for "
                           + factorialTest
                           + " to compute "
                           + sMAX_ITERATIONS
                           + " iterations of the factorial for "
                           + n 
                           + " = "
                           + factorial.apply(n));

        // Help out the garbage collector.
        System.gc();
    }

    /**
     * Warm up the threads in the fork/join pool so that the timing
     * results will be more accurate.
     */
    private void warmUpForkJoinThreads() {
        System.out.println("Warming up the fork/join pool\n");

        for (int i = 0; i < sMAX_ITERATIONS; i++)
            ParallelStreamFactorial2.factorial(BigInteger.valueOf(sDEFAULT_N));
    }

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Starting Factorial Tests");

        // Initialize to the default value.
        BigInteger n = BigInteger.valueOf(sDEFAULT_N);

        // Change the size as requested.
        if (args.length > 0) 
            n = BigInteger.valueOf(Long.valueOf(args[0]));

        // Create a new test object.
        ex16 test = new ex16();

        // Warm up the fork-join pool to ensure accurate timings.
        test.warmUpForkJoinThreads();

        // Run the various tests.
        test.runTest("SynchronizedParallelFactorial",
                     SynchronizedParallelFactorial::factorial,
                     n);

        test.runTest("SequentialStreamFactorial",
                     SequentialStreamFactorial::factorial,
                     n);

        test.runTest("BuggyFactorial",
                     BuggyFactorial::factorial,
                     n);

        test.runTest("ParallelStreamFactorial2",
                     ParallelStreamFactorial2::factorial,
                     n);

        test.runTest("ParallelStreamFactorial3",
                     ParallelStreamFactorial3::factorial,
                     n);

        System.out.println("Ending Factorial Tests");
    }
}

