package edu.vandy;

import edu.vandy.GCDRunnable;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * This program demonstrates the difference between a Java user thread
 * and a daemon thread in the context of thread pools created by the
 * Java Executor framework.  If it's launched with no command-line
 * parameters the main thread creates a user thread, which can outlive
 * the main thread (i.e., it continues to run even after the main
 * thread exits).  If it's launched with a command-line parameter then
 * it creates a daemon thread, which exits when the main thread exits.
 */
public class TestExecutor {
    /**
     * Entry point method into the program's main thread, which uses
     * the Java Executor framework to create/start a pool of the
     * desired type of thread (i.e., either "user" or "daemon") and
     * sleeps for 1 second while threads in the pool run.  If "daemon"
     * threads are created they will only run as long as the main
     * thread runs.  Conversely, if "user" threads are created they
     * will continue to run even after the main thread exits.
     */
    public static void main(String[] args) {
        System.out.println("Entering main()");

        // Create a "daemon" thread if any command-line parameter is
        // passed to the program.
        final Boolean daemonThread = args.length > 0;

        // Create the GCD Runnable, passing in the type of thread it
        // runs in (i.e., "user" or "daemon").
        GCDRunnable runnableCommand =
            new GCDRunnable(daemonThread ? "daemon" : "user");

        // Maximum number of threads in the pool.
        final int POOL_SIZE = 2;
        
        // Create a new ThreadFactory object that will spawn either
        // "user" threads or "daemon" threads.
        final ThreadFactory threadFactory = 
        	new ThreadFactory() {
            public Thread newThread(Runnable runnable) {
                Thread thr = new Thread(runnable);
                if (daemonThread)
                    thr.setDaemon(true);
                return thr;
            }
        };

        // Create a pool of threads that's will concurrently execute
        // the runnableCommands.
        final Executor executor = 
            Executors.newFixedThreadPool(POOL_SIZE,
                                         threadFactory);
        
        // Execute multiple runnableCommands in the thread pool.
        for (int i = 0; i < POOL_SIZE; ++i)
            executor.execute(runnableCommand);

        // Sleep for 2 seconds and then exit.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}

        System.out.println("Leaving main()");
    }
}

