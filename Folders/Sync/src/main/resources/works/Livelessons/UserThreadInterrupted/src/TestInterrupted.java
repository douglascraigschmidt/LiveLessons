/**
 * @class TestInterrupted
 *
 * @brief This program demonstrates how to interrupt a running user
 *        Java thread that computes the greatest common divisor (GCD).
 *        If there are no command-line arguments then sleep for 4
 *        seconds while the thread computes the GCD in the background
 *        and then send it an interrupt request to cause the thread to
 *        exit.  If it's launched with any command-line arguments the
 *        thread computing the GCD can continue to run even after the
 *        main thread exits.
 */
public class TestInterrupted {
    /**
     * Entry point method into the program's main thread, which
     * creates/starts a user thread to compute the GCD and interrupt
     * it.
     */
    public static void main(String[] args) {
        System.out.println("Entering main()");

        // If there are no arguments then interrupt the thread,
        // otherwise, don't interrupt it.
        final Boolean interruptThread = args.length == 0;

        // Create the GCD Runnable, passing in the type of thread it
        // runs in (i.e., "user" or "daemon").
        GCDRunnable runnableCommand =
            new GCDRunnable();

        // Create a new Thread that's will execute the runnableCommand
        // concurrently.
        Thread thr = new Thread(runnableCommand);

        // Start the user thread.
        thr.start();

        try {
            if(interruptThread) {
                // Sleep for 4 seconds and then interrupt the Thread.
                Thread.sleep(4000);

                System.out.println("interrupting thread " 
                                   + thr.getName());

                // Send the thread an interrupt request. 
                thr.interrupt();
            }

            // Sleep for another second and then exit.
            Thread.sleep(1000);
        } catch (InterruptedException x) {}

        System.out.println("Leaving main()");
    }
}

