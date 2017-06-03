import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.lang.Math.min;

/**
 * This program implements various ways of computing factorials to
 * demonstrate alternative techniques and the dangers of shared state.
 */
public class Factorials {
    // Default factorial number.  Going above this number will create
    // incorrect results.
    static final int sDEFAULT_N = 20;

    /**
     * This class demonstrates how race conditions can occur when
     * state is shared between Java threads.
     */
    static class BuggyFactorial {
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
    static class SynchronizedParallelFactorial {
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
     * This class demonstrates how the Java 8 reduce() operation avoid
     * sharing state between Java threads altogether.
     */
    static class ParallelStreamFactorial {
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
                .reduce(1, (a, b) -> a * b);
        }
    }

    /**
     * Run the given @a factorial test and print the result.
     */
    private static void runFactorialTest(String factorialTest,
                                         Function<Long, Long> factorial,
                                         long n) {
        System.out.println("Factorial of "
                           + n 
                           + " = "
                           + factorial.apply(n)
                           + " for " 
                           + factorialTest);
    }

    /**
     * This is the entry point into the test program.
     */
    static public void main(String[] args) {
        System.out.println("Starting Factorial Tests");

        long n = sDEFAULT_N;

        if (args.length > 0) 
            // Ensure the value of n isn't out of range.
            n = min(Long.valueOf(args[1]), sDEFAULT_N);

        runFactorialTest("SynchronizedParallelFactorial",
                         SynchronizedParallelFactorial::factorial,
                         n);

        runFactorialTest("ParallelStreamFactorial",
                         ParallelStreamFactorial::factorial,
                         n);

        runFactorialTest("BuggyFactorial",
                         BuggyFactorial::factorial,
                         n);

        System.out.println("Ending Factorial Tests");
    }
}

