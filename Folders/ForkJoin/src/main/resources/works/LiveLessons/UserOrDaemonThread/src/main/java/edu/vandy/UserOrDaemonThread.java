package edu.vandy;

import static java.lang.Math.abs;

import java.util.Random;

/**
 * This class demonstrates the difference between a Java user thread
 * and a daemon thread.  If its constructor is passed "true" it
 * becomes a "daemon" thread, which exits when the main thread exits.
 * If it's passed "false" it's a "user" thread, which can continue to
 * run even after the main thread exits.
 *
 * The main() function demonstrates the difference between a Java user
 * thread and a daemon thread.  If it's launched with no command-line
 * parameters the main thread creates a user thread, which can outlive
 * the main thread (i.e., it continues to run even after the main
 * thread exits).  If it's launched with a command-line parameter then
 * it creates a daemon thread, which exits when the main thread exits.
 */
public class UserOrDaemonThread
       extends Thread  {
    /**
     * Keep track of whether this is a "user" or a "daemon" thread.
     */
    final private String threadType; 

    /** 
     * Number of times to iterate, which is 100 million to ensure the
     * program runs for a while.
     */
    private final int MAX_ITERATIONS = 100_000_000;

    /**
     * Constructor determines what type of thread it being created.
     */
    public UserOrDaemonThread(Boolean daemonThread) {
        if (daemonThread) {
            // Become a daemon thread (setDaemon() obtained from the
            // superclass).
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
     * Hook method that runs for MAX_ITERATIONs.
     */
    @Override
    public void run() {
        final String threadString = 
            " with " 
            + threadType
            + " thread id " 
            + Thread.currentThread();

        System.out.println("Entering run()"
                           + threadString);

        // Create a new Random number generator.  We need to allocate
        // a new Random object dynamically since we can't inherit from
        // Random since we already inherit from Thread and Java only
        // allows single inheritance.
        Random random = new Random();

        try {
            // Iterate for the given # of iterations.
            for (int i = 0; i < MAX_ITERATIONS; ++i) {
                // Generate two random numbers.
                int number1 = abs(random.nextInt());
                int number2 = abs(random.nextInt());
                
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

    /**
     * Entry point method into the program's main thread, which
     * creates/starts the desired type of thread (i.e., either "user"
     * or "daemon") and sleeps for 1 second while that thread runs in
     * the background.  If a "daemon" thread is created it will only
     * run as long as the main thread runs.  Conversely, if a "user"
     * thread is created it will continue to run even after the main
     * thread exits.
     */
    public static void main(String[] args) {
        System.out.println("Entering main()");

        // Create a "daemon" thread if any command-line parameter is
        // passed to the program.
        final Boolean daemonThread = args.length > 0;

        // Create the appropriate type of thread (i.e., "user" or
        // "daemon").
        UserOrDaemonThread thr =
            new UserOrDaemonThread(daemonThread);

        // Start the thread.
        thr.start();

        // Sleep for 1 second and then exit.
        try {
            Thread.sleep(1000);
        } catch (InterruptedException x) {}

        System.out.println("Leaving main()");
    }
}
