package edu.vuum.mocca;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @class PingPongThreadBlockingQueue
 * 
 * @brief This class uses blocking queues to implement the
 *        acquire() and release() hook methods that schedule the
 *        ping/pong algorithm. It plays the role of the "Concrete
 *        Class" in the Template Method pattern.
 */
public class PingPongThreadBlockingQueue extends PingPongThread {
    /**
     * These queues handle synchronization between our thread and
     * the other Thread.  We exploit the blocking features of the
     * queues, calling take() on the other thread's empty queue to
     * simulate conditional waiting and calling put() on our
     * thread to simulate notifying a waiter.
     */
    // TODO - You fill in here.
    private final LinkedBlockingQueue<Object> mMine;
    private final LinkedBlockingQueue<Object> mOther;

    /**
     * This "ball" is used to pass control between two Threads,
     * which avoids having to allocate memory dynamically each
     * time control is passed.
     */
    // TODO - You fill in here.
    private Object mPingPongBall;

    /**
     * Constructor initializes the various fields.
     */
    PingPongThreadBlockingQueue(String stringToPrint,
                                LinkedBlockingQueue<Object> mine,
                                LinkedBlockingQueue<Object> other,
                                Object pingPongBall,
                                int maxIterations) {
        // TODO - You fill in here.
        super(stringToPrint, maxIterations);
        mMine = mine;
        mOther = other;
        mPingPongBall = pingPongBall;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    void acquire() {
        // TODO - You fill in here.

        // Since the binary semaphore above ignores exceptions, so
        // will we.
        try {
            // Block until there's an item in the queue.
            mPingPongBall = mOther.take();
        } catch (InterruptedException e) {
        }
    }

    @Override
    void release() {
        // TODO - You fill in here.

        try {
            mMine.put(mPingPongBall);
        } catch (InterruptedException e) {
        }
    }
}
