package edu.vandy.pubsub.utils;

/**
 * Atomically increments and decrements an internal count, calling an
 * action when the internal count equals a given parameter.
 */
class ThresholdCrosser {
    /**
     * The internal count that incremented or decremented atomically
     * by the methods below.
     */
    private int mCount;

    /**
     * Constructor sets the field.
     */
    ThresholdCrosser(int initialCount) {
        mCount = initialCount;
    }
    
    /**
     * (Re)set the initial count.
     */
    void setInitialCount(int initialCount) {
        mCount = initialCount;
    }

    /**
     * Invoke {@code action} iff the internal count equals {@code n}
     * after being incremented.
     */
    void incrementAndCallAtN(int n,
                             Runnable action) {
        synchronized(this) {
            // Atomically run action iff preincrementing mCount == n.
            if (++mCount == n)
                action.run();
        }
    }

    /**
     * Invoke {@code action} iff the internal count equals {@code n}
     * after being decremented.
     */
    void decrementAndCallAtN(int n,
                             Runnable action) {
        synchronized(this) {
            // Atomically run action iff predecrementing mCount == n.
            if (--mCount == n)
                action.run();
        }
    }
}
