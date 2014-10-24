package edu.vuum.mocca;

/**
 * @class BinarySemaphore
 * 
 * @brief This class uses a Java build-in monitor object to
 *        implement a binary semaphore.
 */
public class BinarySemaphore {
    /**
     * Keeps track of whether the semaphore is available or not.
     */
    private Boolean mAvailable;

    /**
     * Constructor sets whether the BinarySemaphore starts out
     * locked or not.
     */
    public BinarySemaphore(Boolean available) {
        mAvailable = available;
    }

    /**
     * Block until the BinarySemaphore is acquired.
     */
    public synchronized void acquire() {
        while (!mAvailable)
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore.
            }
        mAvailable = false;
    }

    /**
     * Release the BinarySemaphore.
     */
    public synchronized void release() {
        mAvailable = true;
        notify();
    }
}

