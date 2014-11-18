package example.pingpong;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @class PingPongThreadBlockingQueue
 * 
 * @brief This class uses blocking queues to implement the acquire()
 *        and release() hook methods that synchronize the ping/pong
 *        algorithm. It plays the role of the "Concrete Class" in the
 *        Template Method pattern.
 */
public class PingPongThreadBlockingQueue extends PingPongThread {
    /**
     * These queues handle synchronization between our thread and
     * the other Thread.  We exploit the blocking features of the
     * queues, calling take() on the other thread's empty queue to
     * simulate conditional waiting and calling put() on our
     * thread to simulate notifying a waiter.
     */
    private final LinkedBlockingQueue<Object> mMine;
    private final LinkedBlockingQueue<Object> mOther;

    /**
     * This "ball" is used to pass control between two Threads,
     * which avoids having to allocate memory dynamically each
     * time control is passed.
     */
    private Object mPingPongBall = null;

    /**
     * Constructor initializes the various fields.
     */
    public PingPongThreadBlockingQueue(String stringToPrint,
                                       LinkedBlockingQueue<Object> mine,
                                       LinkedBlockingQueue<Object> other,
                                       int maxIterations) {
        super(stringToPrint, maxIterations);
        mMine = mine;
        mOther = other;
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    protected void acquire() {
        try {
            // Block until there's an item to take from the other
            // BlockingQueue.
            mPingPongBall = mOther.take();
        } catch (InterruptedException e) {
            // Ignore InterruptedExceptions.
        }
    }

    @Override
    protected void release() {
        try {
            // Put the ball back in the other BlockingQueue to notify
            // it to wakeup and run.
            mMine.put(mPingPongBall);
        } catch (InterruptedException e) {
            // Ignore InterruptedExceptions.
        }
    }
}
