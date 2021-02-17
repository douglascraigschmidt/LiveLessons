package example.pingpong.threads;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import example.pingpong.platform.PlatformStrategy;
import example.pingpong.synchronizers.PingPongThreadBlockingQueue;
import example.pingpong.synchronizers.PingPongThreadCond;
import example.pingpong.synchronizers.PingPongThreadMonObj;
import example.pingpong.synchronizers.PingPongThreadSema;

/**
 * This class implements a Java program that creates two threads, Ping
 * and Pong, to alternately print "Ping" and "Pong", respectively, on
 * the display. It uses the Template Method, Strategy, and Factory
 * Method patterns to factor out common code, enhance portability, and
 * simplify the program design.
 */
public class PlayPingPong 
       implements Runnable {
    /**
     * Number of iterations to ping/pong.
     */
    private final int mMaxIterations;

    /**
     * Which synchronization to use, e.g., "SEMA", "COND", "MONOBJ",
     * "QUEUE".
     */
    private final String mSyncMechanism;

    /**
     * Maximum number of ping pong Threads.
     */
    private final static int MAX_PING_PONG_THREADS = 2;

    /**
     * Constants used to distinguish between ping and pong threads.
     */
    private final static int PING_THREAD = 0;
    private final static int PONG_THREAD = 1;

    /**
     * Constructor initializes the data members.
     */
    public PlayPingPong(int maxIterations,
                        String syncMechanism) {
        // Number of iterations to perform pings and pongs.
        mMaxIterations = maxIterations;

        // Which type of synchronization mechanism to use.
        mSyncMechanism = syncMechanism;
    }

    /**
     * Start running the ping/pong code, which can be called from a
     * main() method in a Java class, an Android Activity, etc.
     */
    public void run() {
        // Indicate a new game is beginning.
        PlatformStrategy.instance().begin();

        // Let the user know we're starting. 
        PlatformStrategy.instance().print("Ready...Set...Go!");

        // Create the appropriate type of ping and pong threads with
        // the designated synchronization mechanism.
        List<PingPongThread> pingPongThreads =
            makePingPongThreads(mSyncMechanism);

        // Start ping and pong threads, which calls their run()
        // methods.
        pingPongThreads.forEach(Thread::start);

        // Barrier synchronization to wait for all work to be done
        // before exiting play().
        PlatformStrategy.instance().awaitDone();

        // Let the user know we're done.
        PlatformStrategy.instance().print("Done!");
    }

    /**
     * Factory method that creates the designated instances of the
     * PingPongThread subclass based on the @code syncMechanism
     * parameter.
     */
    private List<PingPongThread> makePingPongThreads(String syncMechanism) {
        // Create an array of PingPongThreads.
        PingPongThread[] pingPongThreads =
            new PingPongThread[MAX_PING_PONG_THREADS];

        switch (syncMechanism) {
        case "SEMA": {
            // Create the Java Semaphores that ensure threads print
            // "ping" and "pong" in the correct alternating order.
            Semaphore pingSema =
                new Semaphore(1); // Starts out unlocked.

            Semaphore pongSema =
                new Semaphore(0); // Starts out locked.

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
            break;
        }
        case "MONOBJ": {
            // Create the BinarySemaphores (implemented as Java
            // build-in monitor objects) that ensure threads print
            // "ping" and "pong" in the correct alternating order.
            PingPongThreadMonObj.BinarySemaphore pingSema =
                new PingPongThreadMonObj
                .BinarySemaphore(true); // Starts out unlocked.

            PingPongThreadMonObj.BinarySemaphore pongSema = 
                new PingPongThreadMonObj
                .BinarySemaphore(false); // Starts out locked.

            pingPongThreads[PING_THREAD] =
                new PingPongThreadMonObj("ping",
                                         pingSema,
                                         pongSema,
                                         mMaxIterations);

            pingPongThreads[PONG_THREAD] =
                new PingPongThreadMonObj("pong",
                                         pongSema,
                                         pingSema,
                                         mMaxIterations);
            break;
        }
        case "COND":
            // Create the ReentrantLock and Conditions that ensure
            // threads print "ping" and "pong" in the correct
            // alternating order.
            ReentrantLock lock = new ReentrantLock();
            Condition pingCond = lock.newCondition();
            Condition pongCond = lock.newCondition();

            PingPongThreadCond pingThread =
                new PingPongThreadCond("ping",
                                       lock,
                                       pingCond,
                                       pongCond,
                                       true,
                                       mMaxIterations);
            PingPongThreadCond pongThread =
                new PingPongThreadCond("pong",
                                       lock,
                                       pongCond,
                                       pingCond,
                                       false,
                                       mMaxIterations);
            // Exchange Thread IDs.
            pingThread.setOtherThreadId(pongThread.getId());
            pongThread.setOtherThreadId(pingThread.getId());

            pingPongThreads[PING_THREAD] = pingThread;
            pingPongThreads[PONG_THREAD] = pongThread;

            break;
        case "QUEUE":
            // Create the LinkedBlockingQueues that ensure threads
            // print "ping" and "pong" in the correct alternating
            // order.
            LinkedBlockingQueue<Object> pingQueue =
                new LinkedBlockingQueue<>();

            LinkedBlockingQueue<Object> pongQueue =
                new LinkedBlockingQueue<>();

            Object pingPongBall = new Object();

            try {
                // Initialize this implementation by putting an object
                // onto the "pong" queue.
                pongQueue.put(pingPongBall);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }

            pingPongThreads[PING_THREAD] =
                new PingPongThreadBlockingQueue("ping",
                                                pingQueue,
                                                pongQueue,
                                                mMaxIterations);

            pingPongThreads[PONG_THREAD] =
                new PingPongThreadBlockingQueue("pong",
                                                pongQueue,
                                                pingQueue,
                                                mMaxIterations);
            break;
        }

        // Return the initialized threads as a list.
        return Arrays.asList(pingPongThreads);
    }
}
