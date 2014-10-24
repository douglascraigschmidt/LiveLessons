package edu.vuum.mocca;

/**
 * @class PlayPingPong
 *
 * @brief This class implements a Java program that creates two
 *        threads, Ping and Pong, that use BinarySemaphores
 *        implemented using Java built-in monitor objects to
 *        alternately print "Ping" and "Pong", respectively, on the
 *        display.  It implements Runnable so that it can either be
 *        run in a new Thread or run sequentially.
 */
public class PlayPingPong implements Runnable {
    /**
     * Number of iterations to ping/pong.
     */
    private final int mMaxIterations;

    /**
     * A factory that initializes the pingPongThreads parameter.
     */
    private PingPongThread[] makePingPongThreads() {
        /**
         * Constants used to distinguish between ping and pong
         * threads.
         */
        final int PING_THREAD = 0;
        final int PONG_THREAD = 1;
        final int NUMBER_OF_THREADS = 2;

        // Create an array of two PingPongThreads.
        PingPongThread[] pingPongThreads =
            new PingPongThread[NUMBER_OF_THREADS];

        // Create the pair of semaphores that schedule threads
        // printing "ping" and "pong" in the correct alternating
        // order.
        BinarySemaphore pingSema = 
            new BinarySemaphore(true); // Starts out available.
        BinarySemaphore pongSema = 
            new BinarySemaphore(false); // Starts out unavailable.

        // Initialize the PING_THREAD.
        pingPongThreads[PING_THREAD] = 
            new PingPongThread("ping",
                               pingSema,
                               pongSema,
                               mMaxIterations);
        // Initialize the PONG_THREAD.
        pingPongThreads[PONG_THREAD] = 
            new PingPongThread("pong",
                               pongSema,
                               pingSema,
                               mMaxIterations);
        return pingPongThreads;
    }
     
    /**
     * Constructor stores the number of iterations to play ping/pong
     * and the desired synchronization mechanism.
     */
    private PlayPingPong(int maxIterations) {
        // Number of iterations to perform pings and pongs.
        mMaxIterations = maxIterations;
    }

    /**
     * Factory method that creates a new PlayPingPong object and
     * returns it as a Runnable.
     */
    static public PlayPingPong makePlayPingPong(int maxIterations) {
        return new PlayPingPong(maxIterations);
    }

    /**
     * Start running the ping/pong code, which can be called from a
     * main() function in a Java class.
     */ 
    public void run() {
        /** Let the user know we're starting. */
        System.out.println("Ready...Set...Go!");

        /** Create the ping and pong threads. */
        PingPongThread pingPongThreads[] =
            makePingPongThreads();

        /**
         * Start ping and pong threads, which calls their run() hook
         * methods to implement the concurrent ping/pong algorithm.
         */
        for (Thread thread : pingPongThreads)
            thread.start();

        /**
         * Barrier synchronization to wait for all work to be done
         * before exiting play().
         */
        for (Thread thread : pingPongThreads)
            try {
            	thread.join();
            } catch (InterruptedException e) {
            }

        /** Let the user know we're done. */
        System.out.println("Done!");
    }
}
