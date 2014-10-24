import java.util.Random;

/**
 * @class UserOrDaemonThread
 *
 * @brief This class demonstrates the difference between a Java user
 *        thread and a daemon thread.  If it's constructor is passed
 *        "true" it becomes a "daemon" thread, which exits when the
 *        main thread exits.  If it's passed "false" it's a "user"
 *        thread, which can continue to run even after the main thread
 *        exits.
 */
public class UserOrDaemonThread extends Thread  { 
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
    public UserOrDaemonThread(Boolean daemonThread) {
        if (daemonThread) {
            // Become a daemon thread.
            setDaemon(true);
            threadType = "daemon";
        } else
            threadType = "user";
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

        // Create a new Random number generator.  We need to allocate
        // a new Random object dyanmically since we can't inherit from
        // Random since we already inherit from Thread and Java only
        // allows single inheritance.
        Random random = new Random();

        try {
            for (int i = 0; i < MAX_ITERATIONS; ++i) {
                // Generate two random numbers.
                int number1 = random.nextInt(); 
                int number2 = random.nextInt();
                
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
