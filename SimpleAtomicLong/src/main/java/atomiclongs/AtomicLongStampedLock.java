package atomiclongs;

import java.util.concurrent.locks.StampedLock;

/**
 * This class implements a subset of the Java {@link AtomicLong} class
 * using a {@link StampedLock} to illustrate how they work.
 */
public class AtomicLongStampedLock
       implements AtomicLong {
    /**
     * The value that's manipulated atomically via the methods.
     */
    private long mValue;

    /**
     * The {@link StampedLock} used to serialize access to mValue.
     */
    private final StampedLock mStampedLock = new StampedLock();

    /**
     * Creates a new {@link AtomicLongStampedLock} with the
     * given initial value.
     */
    public AtomicLongStampedLock(long initialValue) {
        mValue = initialValue;
    }

    /**
     * Gets the current value.
     * 
     * @return The current value
     */
    public long get() {
        long stamp = mStampedLock.readLock();

        try {
            return mValue;
        } finally {
            mStampedLock.unlockRead(stamp);
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
        long stamp = mStampedLock.writeLock();
        try {
          mValue++; // writeLock held.

          // Downgrade to a readlock.
          stamp = mStampedLock.tryConvertToReadLock(stamp);
          return mValue;
        } finally {
          mStampedLock.unlock(stamp); 
        }
    }
    
    /**
     * Atomically decrements by one the current value.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        long stamp = mStampedLock.writeLock();

        try {
            return --mValue;
        } finally {
            mStampedLock.unlockWrite(stamp);
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the previous value
     */
    public long getAndIncrement() {
        long stamp = mStampedLock.writeLock();
        
        try {
            return mValue++;
        } finally {
            mStampedLock.unlockWrite(stamp);
        }
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return The previous value
     */
    public long getAndDecrement() {
        long stamp = mStampedLock.writeLock();

        try {
            return mValue--;
        } finally {
            mStampedLock.unlockWrite(stamp);
        }
    }
}

