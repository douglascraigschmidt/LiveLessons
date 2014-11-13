package edu.vuum.mocca;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @class PlayPingPong
 *
 * @brief This class implements a Java program that creates two
 *        threads, Ping and Pong, to alternately print "Ping" and
 *        "Pong", respectively, on the display. It uses the Template
 *        Method, Strategy, and Factory Method patterns to factor out
 *        common code and simplify the program design.
 */
public class PlayPingPong implements Runnable {
    /**
     * Number of iterations to ping/pong.
     */
    private volatile int mMaxIterations;

    /** Maximum number of iterations per "turn" (defaults to 1). */
    private volatile int mMaxTurns = 1;

    /**
     * Which synchronization to use, e.g., "SEMA", "COND", "MONOBJ",
     * "QUEUE", and "PADDLE".  Defaults to "PADDLE".
     */
    private String mSyncMechanism = "PADDLE";

    /**
     * Maximum number of ping pong threads.
     */
    private final static int MAX_PING_PONG_THREADS = 2;

    /**
     * Constants used to distinguish between ping and pong threads.
     */
    private final static int PING_THREAD = 0;
    private final static int PONG_THREAD = 1;

    /**
     * Constructor stores the PlatformStrategy and the number of
     * iterations to play ping/pong.
     */
    public PlayPingPong(int maxIterations,
                        String syncMechanism) {
            // Number of iterations to perform pings and pongs.
        mMaxIterations = maxIterations;

        // Which synchronization to use (e.g., "SEMA", "COND",
        // "MONOBJ", "QUEUE", or "PADDLE").
        mSyncMechanism = syncMechanism;
    }

    private void makePingPongThreads(String schedMechanism,
                                     PingPongThread[] pingPongThreads) {
        // Use a pair of Java Semaphores.
        if (schedMechanism.equals("SEMA")) {
            // Create the semaphores that schedule threads printing
            // "ping" and "pong" in the correct alternating order.
            Semaphore pingSema = new Semaphore(1); // Starts out unlocked.
            Semaphore pongSema = new Semaphore(0);

            pingPongThreads[PING_THREAD] = 
                new PingPongThreadSema("ping",
                                       pingSema,
                                       pongSema,
                                       mMaxIterations);
            pingPongThreads[PONG_THREAD] =
                new PingPongThreadSema("pong",
                                       pongSema,
                                       pingSema,
                                       mMaxIterations);
        }
        // Use a pair of Java ConditionObjects.
        else if (schedMechanism.equals("COND")) {
            ReentrantLock lock = new ReentrantLock();
            Condition pingCond = lock.newCondition();
            Condition pongCond = lock.newCondition();
            int numberOfTurnsEach = 2;

            pingPongThreads[PING_THREAD] = 
                new PingPongThreadCond("ping",
                                       lock,
                                       pingCond,
                                       pongCond,
                                       true,
                                       mMaxIterations);
            pingPongThreads[PONG_THREAD] =
                new PingPongThreadCond("pong",
                                       lock,
                                       pongCond,
                                       pingCond,
                                       false,
                                       mMaxIterations);
            pingPongThreads[PING_THREAD]
                .setOtherThreadId(pingPongThreads[PONG_THREAD].getId());
            pingPongThreads[PONG_THREAD]
                .setOtherThreadId(pingPongThreads[PING_THREAD].getId());
        }
        // Use a pair of Java BinarySemaphores implemented as monitor
        // objects.
        else if (schedMechanism.equals("MONOBJ")) {
            BinarySemaphore pingSema = new BinarySemaphore(false);
            BinarySemaphore pongSema = new BinarySemaphore(true);

            pingPongThreads[PING_THREAD] =
                new PingPongThreadMonObj("ping",
                                         pingSema,
                                         pongSema,
                                         true,
                                         mMaxIterations);
            pingPongThreads[PONG_THREAD] =
                new PingPongThreadMonObj("pong",
                                         pongSema,
                                         pingSema,
                                         false,
                                         mMaxIterations);
        }
        // Use a pair of Java LinkedBlockingQueue objects.
        else if (schedMechanism.equals("QUEUE")) {
            // TODO - You fill in here.
            LinkedBlockingQueue<Object> pingQueue =
                new LinkedBlockingQueue<Object>();
            LinkedBlockingQueue<Object> pongQueue =
                new LinkedBlockingQueue<Object>();
            Object ball = new Object();

            try {
                // Initialize this implementation by putting an object
                // onto the "pong" queue.
                pongQueue.put(ball);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }

            pingPongThreads[PING_THREAD] =
                new PingPongThreadBlockingQueue("ping",
                                                pingQueue,
                                                pongQueue,
                                                ball,
                                                mMaxIterations);
            pingPongThreads[PONG_THREAD] = 
                new PingPongThreadBlockingQueue("pong",
                                                pongQueue,
                                                pingQueue,
                                                ball,
                                                mMaxIterations);
        }
        // Use a pair of BallPaddles
        else if (schedMechanism.equals("PADDLE")) {
            // TODO - You fill in here.
            BallPaddle pingBall =
                new BallPaddle(true); // Start with the ball.
            BallPaddle pongBall =
                new BallPaddle(false);

            pingPongThreads[PING_THREAD] = 
                new PingPongThreadPaddle("ping",
                                         pingBall, 
                                         pongBall,
                                         mMaxIterations);
            pingPongThreads[PONG_THREAD] =
                new PingPongThreadPaddle("pong",
                                         pongBall,
                                         pingBall,
                                         mMaxIterations);
        }
    }

    /**
     * Start running the ping/pong code, which can be called from a
     * main() function in a Java class, an Android Activity, etc.
     */
    public void run() {
        // Indicate a new game is beginning.
        PlatformStrategy.instance().begin();

        // Let the user know we're starting. 
        PlatformStrategy.instance().print("Ready...Set...Go!");

        // Create the ping and pong threads. 
        PingPongThread pingPongThreads[] =
            new PingPongThread[MAX_PING_PONG_THREADS];
        pingPongThreads[PING_THREAD] = null;
        pingPongThreads[PONG_THREAD] = null;

        // Create the appropriate type of threads with the designated
        // scheduling mechanism (e.g., "SEMA" for Semaphores, "COND"
        // for ConditionObjects, "MONOBJ" for monitor objects, etc.).
        makePingPongThreads(mSyncMechanism,
                            pingPongThreads);

        // Start ping and pong threads, which calls their run()
        // methods.
        pingPongThreads[PING_THREAD].start();
        pingPongThreads[PONG_THREAD].start();

        // Barrier synchronization to wait for all work to be done
        // before exiting play().
        PlatformStrategy.instance().awaitDone();

        // Let the user know we're done.
        PlatformStrategy.instance().print("Done!");
    }
}
