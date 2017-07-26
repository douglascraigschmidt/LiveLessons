package vandy.mooc.gcd.activities;

import java.util.Random;

/**
 * Computes the greatest common divisor (GCD) of two numbers, which is
 * the largest positive integer that divides two integers without a
 * remainder.  This implementation extends Random and implements the
 * Runnable interface's run() hook method.
 */
public class GCDRunnable
       extends Random // Inherits random number generation capabilities.
       implements Runnable {
    /**
     * A reference to the MainActivity. 
     */
    private final MainActivity mActivity;

    /** 
     * Number of times to iterate, which is 100 million to ensure the
     * program runs for a while.
     */
    private final int MAX_ITERATIONS = 100000000;

    /** 
     * Number of times to iterate before calling print, which is 10
     * million to ensure the program runs for a while.
     */
    private final int MAX_PRINT_ITERATIONS = 10000000;

    /**
     * Constructor initializes the field.
     */
    public GCDRunnable(MainActivity activity) {
        mActivity = activity;
    }
    
    /**
     * Provides a recursive implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD).
     */
    private int computeGCD(int number1,
                           int number2) {
        // Basis case.
        if (number2 == 0)
            return number1;
        // Recursive call.
        return computeGCD(number2,
                          number1 % number2);
    }

    /**
     * Hook method that runs for MAX_ITERATIONs computing the GCD of
     * randomly generated numbers.
     */
    public void run() {
        final String threadString = 
            " with thread id " 
            + Thread.currentThread();

        mActivity.println("Entering run()"
                          + threadString);

        // Generate random numbers and compute their GCDs.

        for (int i = 0; i < MAX_ITERATIONS; ++i) {
            // Generate two random numbers.
            int number1 = nextInt(); 
            int number2 = nextInt();
                
            // Print results every 10 million iterations.
            if ((i % MAX_PRINT_ITERATIONS) == 0)
                mActivity.println("In run()"
                                  + threadString 
                                  + " the GCD of " 
                                  + number1
                                  + " and "
                                  + number2
                                  + " is "
                                  + computeGCD(number1,
                                               number2));
        }

        mActivity.println("Leaving run() "
                          + threadString);
    }
}
