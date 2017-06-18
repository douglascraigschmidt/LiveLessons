import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.LongStream;

/**
 * This program implements various ways of computing factorials for
 * BigIntegers to demonstrate the performance of alternative parallel
 * and sequential algorithms, as well as the dangers of sharing
 * unsynchronized state between threads.
 */
public class ex16 {
    /**
     * Max number of times to run the tests.
     */
    private static final int sMAX_ITERATIONS = 100000;

    /**
     * Default factorial number.  Going above this number will create
     * incorrect results.
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

            // Return the total.
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
     * This class demonstrates how the Java 8 reduce() operation
     * avoids sharing state between Java threads altogether.
     */
    private static class ParallelStreamFactorial {
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

                .mapToObj(BigInteger::valueOf)

                // Performs a reduction on the elements of this stream
                // to compute the factorial.  Note that there's no
                // shared state at all!
                .reduce(BigInteger.ONE, BigInteger::multiply);
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
    private <T> void runFactorialTest(String factorialTest,
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
                           + " milliseconds to compute "
                           + sMAX_ITERATIONS
                           + " iterations of the factorial for "
                           + n 
                           + " = "
                           + factorial.apply(n)
                           + " for " 
                           + factorialTest);

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
            ParallelStreamFactorial.factorial(BigInteger.valueOf(sDEFAULT_N));
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
        test.runFactorialTest("SynchronizedParallelFactorial",
                              SynchronizedParallelFactorial::factorial,
                              n);

        test.runFactorialTest("SequentialStreamFactorial",
                              SequentialStreamFactorial::factorial,
                              n);

        test.runFactorialTest("BuggyFactorial",
                              BuggyFactorial::factorial,
                              n);

        test.runFactorialTest("ParallelStreamFactorial",
                              ParallelStreamFactorial::factorial,
                              n);

        System.out.println("Ending Factorial Tests");
    }
}

