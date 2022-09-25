package vandy.mooc.pingpong.model;

import vandy.mooc.pingpong.presenter.PingPongPresenter;
import vandy.mooc.pingpong.presenter.PingPongThread;

/**
 * This class uses Binary Semaphores (implemented as Java built-in
 * monitor objects) to implement the acquire() and release() hook
 * methods that synchronize the ping/pong algorithm. It plays the role
 * of the "Concrete Class" in the Template Method pattern.
 */
class PingPongThreadMonObj 
      extends PingPongThread {
    /**
     * @class BinarySemaphore
     * 
     * @brief This class uses Java built-in monitor objects to
     *        implement a simple binary semaphore.
     */
    static public class BinarySemaphore {
        /**
         * Keeps track of whether the semaphore is unlocked or locked.
         */
        private boolean mUnLocked;
    
        /**
         * Constructor sets the boolean data member to start out
         * locked or unlocked.
         */
        public BinarySemaphore(boolean unlocked) {
            mUnLocked = unlocked;
        }

        /**
         * Acquire the binary semaphore.
         */
        public void acquire() {
            synchronized(this) {
                while (!mUnLocked)
                    try {
                        // Wait until we're notified that mUnLocked
                        // may be true.
                        wait();
                    } catch (InterruptedException e) {
                        // ignore.
                    }
                mUnLocked = false;
            }
        }

        /**
         * Release the binary semaphore.
         */
        public void release() {
            synchronized(this) {
                // Release the semaphore and notify() a waiting
                // Thread.
                mUnLocked = true;
                notify();
            }
        }
    }

    /**
     * Semaphores that schedule the ping/pong algorithm.
     */
    private final BinarySemaphore mMine;
    private final BinarySemaphore mOther;

    /**
     * Constructor initializes the fields and superclass.
     */
    public PingPongThreadMonObj(PingPongPresenter presenter,
                                String stringToPrint,
                                BinarySemaphore mine,
                                BinarySemaphore other,
                                int maxIterations) {
        super(presenter, stringToPrint, maxIterations);
        mMine = mine; 
        mOther = other; 
    }

    /**
     * Hook method for ping/pong acquire.
     */
    @Override
    protected void acquire() {
        // Block until we acquire the semaphore.

        mMine.acquire();
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
