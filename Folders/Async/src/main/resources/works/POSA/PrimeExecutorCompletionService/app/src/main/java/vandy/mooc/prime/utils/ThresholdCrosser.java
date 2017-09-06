package vandy.mooc.prime.utils;

/**
 * Atomically increments and decrements an internal count, invoking an
 * action when the internal count equals a parameter passed to the
 * methods.
 */
public class ThresholdCrosser {
    /**
     * The internal count that incremented or decremented atomically
     * by the methods below.
     */
    private int mCount;

    /**
     * Constructor sets the field.
     */
    public ThresholdCrosser(int initialCount) {
        mCount = initialCount;
    }
    
    /**
     * (Re)set the initial count.
     */
    void setInitialCount(int initialCount) {
        mCount = initialCount;
    }

    /**
     * Invoke @a action iff the internal count equals @a n after being incremented.
     */
    public void incrementAndCallAtN(int n,
                                    Runnable action) {
        synchronized(this) {
            if (++mCount == n)
                action.run();
        }
    }

    /**
     * Invoke @a action iff the internal count equals @a n after being decremented.
     */
    public void decrementAndCallAtN(int n,
                                    Runnable action) {
        synchronized(this) {
            if (--mCount == n)
                action.run();
        }
    }
}
