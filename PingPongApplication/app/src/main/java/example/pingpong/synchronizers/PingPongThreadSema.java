package example.pingpong.synchronizers;

import java.util.concurrent.Semaphore;

import example.pingpong.threads.PingPongThread;

/**
 * This class uses Java semaphores to implement the acquire() and
 * release() hook methods that synchronize the ping/pong algorithm.
 * It plays the role of the "Concrete Class" in the Template Method
 * pattern.
 */
public class PingPongThreadSema 
       extends PingPongThread {
    /**
     * Semaphores that schedule the ping/pong algorithm.
     */
    private final Semaphore mMine;
    private final Semaphore mOther;

    /**
     * Constructor initializes the fields.
     */
    public PingPongThreadSema(String stringToPrint, 
                              Semaphore mine,
                              Semaphore other,
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
        // Block until we acquire the semaphore.
        mMine.acquireUninterruptibly();
    }

    /**
     * Hook method for ping/pong release.
     */
    @Override
    protected void release() {
        // Release the other semaphore.
        mOther.release();
    }
}
