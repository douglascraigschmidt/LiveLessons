package edu.vuum.mocca;

/**
 * @class BinarySemaphore
 * 
 * @brief This class uses Java built-in monitor objects to implement a
 *        simple binary semaphore.
 */
public class BinarySemaphore {
    /**
     * Keeps track of whether the semaphore is locked or not.
     */
    private Boolean mLocked;
    
    /**
     * Constructor sets whether the semaphore starts out locked or
     * unlocked.
     */
    public BinarySemaphore(Boolean locked) {
        mLocked = locked;
    }

    /**
     * Acquire the binary semaphore.
     */
    public void acquire() {
        synchronized(this) {
            while (mLocked)
                try {
                    wait();
                } catch (InterruptedException e) {
                    // ignore.
                }
            mLocked = true;
        }
    }

    /**
     * Release the binary semaphore.
     */
    public void release() {
        synchronized(this) {
            mLocked = false;
            notify();
        }
    }
}

