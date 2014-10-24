/**
 * @class TestThread
 *
 * @brief This program demonstrates the difference between a Java user
 *        thread and a daemon thread.  If it's launched with no
 *        command-line parameters the main thread creates a user
 *        thread, which can outlive the main thread (i.e., it
 *        continues to run even after the main thread exits).  If it's
 *        launched with a command-line parameter then it creates a
 *        daemon thread, which exits when the main thread exits.
 */
public class TestThread {
    /**
     * Entry point method into the program's main thread, which
     * creates/starts the desired type of thread (i.e., either "user"
     * or "daemon") and sleeps for 3 seconds while that thread runs.
     * If a "daemon" thread is created it will only run as long as the
     * main thread runs.  Conversely, if a "user" thread is created it
     * will continue to run even after the main thread exits.
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
