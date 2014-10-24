import java.util.Random;

/**
 * @class GCDRunnable
 *
 * @brief Computes the greatest common divisor (GCD) of two numbers.
 */
public class GCDRunnable
       extends Random // Inherits random number generation capabilities.
       implements Runnable    
{
    /**
     * Keep track of whether this is a "user" or a "daemon" thread.
     */
    final private String threadType; 

    /** 
     * Number of times to iterate, which is 100 million to ensure the
     * program runs for a while.
     */
    private final int MAX_ITERATIONS = 100000000;

    /**
     * Constructor determines what type of thread it being created.
     */
    public GCDRunnable(String threadType) {
        this.threadType = threadType;
    }

    /**
     * Provides a recursive implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD), which is the
     * largest positive integer that divides two integers without a
     * remainder.
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
     * Hook method that runs for MAX_ITERATIONs, sleeping for half a
     * second at a time.
     */
    public void run() {
        final String threadString = 
            " with " 
            + threadType
            + " thread id " 
            + Thread.currentThread();

        System.out.println("Entering run()"
                           + threadString);

        try {
            for (int i = 0; i < MAX_ITERATIONS; ++i) {
                // Generate two random numbers.
                int number1 = nextInt(); 
                int number2 = nextInt();
                
                // Print results every 10 million iterations.
                if ((i % 10000000) == 0)
                	System.out.println("In run()"
                                           + threadString 
                                           + " the GCD of " 
                                           + number1
                                           + " and "
                                           + number2
                                           + " is "
                                           + computeGCD(number1,
                                                        number2));
            }
        }
        finally {
            System.out.println("Leaving run() "
                               + threadString);
        }
    }
}
