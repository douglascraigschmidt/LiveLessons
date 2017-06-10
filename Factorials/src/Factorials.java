import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.lang.Math.min;

/**
 * This program implements various ways of computing factorials to
 * demonstrate the performance of alternative techniques and the
 * dangers of sharing unsynchronized state between threads.
 */
public class Factorials {
    /**
     * Max number of times to run the tests.
     */
    private static final int sMAX_ITERATIONS = 100000;

    /**
     * Default factorial number.  Going above this number will create
     * incorrect results.
     */
    private static final int sDEFAULT_N = 20;

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
            public long mTotal = 1;

            /**
             * Multiply the running total by @a n.  This method is not
             * synchronized, so it may incur race conditions.
             */
            public void multiply(long n) {
                mTotal *= n;
            }
        }  

        /**
         * Attempts to return the factorial for the given @a n.  There
         * are race conditions wrt accessing shared state, however, so
         * the result may not always be correct.
         */
        static long factorial(long n) {
            Total t = new Total();

            LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n)

                // Run the forEach() terminal operation concurrently.
                .parallel()

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
            public long mTotal = 1;

            /**
             * Multiply the running total by @a n.  This method is
             * synchronized to avoid race conditions.
             */
            public synchronized void multiply(long n) {
                mTotal *= n;
            }
        }

        /**
         * Return the factorial for the given @a n using a parallel
         * stream and the forEach() terminal operation.
         */
        static long factorial(long n) {
            Total t = new Total();

            LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n)

                // Run the forEach() terminal operation concurrently.
                .parallel()

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
        static long factorial(long n) {
            return LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n)

                // Run the reduce() terminal operation concurrently.
                .parallel() 

                // Performs a reduction on the elements of this stream
                // to compute the factorial.  Note that there's no
                // shared state at all!
                .reduce(1, Factorials::product);
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
        static long factorial(long n) {
            return LongStream
                // Create a stream of longs from 1 to n.
                .rangeClosed(1, n)

                // Performs a reduction on the elements of this stream
                // to compute the factorial.
                .reduce(1, Factorials::product);
        }
    }

    /**
     * Compute the product of two longs.
     */
    static long product(long a, long b) {
        return a * b;
    }

    /**
     * Run the given @a factorial test and print the result.
     */
    private static void runFactorialTest(String factorialTest,
                                         Function<Long, Long> factorial,
                                         long n) {
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
    }

    /**
     * Warm up the threads in the fork/join pool so that the timing
     * results will be more accurate.
     */
    private static void warmUpForkJoinThreads() {
        System.out.println("Warming up the fork/join pool\n");

        for (int i = 0; i < sMAX_ITERATIONS; i++)
            ParallelStreamFactorial.factorial(sDEFAULT_N);
    }

    /**
     * This is the entry point into the test program.
     */
    public static void main(String[] args) {
        System.out.println("Starting Factorial Tests");

        long n = sDEFAULT_N;

        if (args.length > 0) 
            // Ensure the value of n isn't out of range.
            n = min(Long.valueOf(args[1]), sDEFAULT_N);

        warmUpForkJoinThreads();

        runFactorialTest("SynchronizedParallelFactorial",
                         SynchronizedParallelFactorial::factorial,
                         n);

        runFactorialTest("ParallelStreamFactorial",
                         ParallelStreamFactorial::factorial,
                         n);

        runFactorialTest("BuggyFactorial",
                         BuggyFactorial::factorial,
                         n);

        runFactorialTest("SequentialStreamFactorial",
                         SequentialStreamFactorial::factorial,
                         n);

        System.out.println("Ending Factorial Tests");
    }
}

