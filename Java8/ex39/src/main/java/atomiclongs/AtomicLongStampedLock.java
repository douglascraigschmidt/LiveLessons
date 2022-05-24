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
        long value;
        // First try an optimistic read.
        long stamp = mStampedLock.tryOptimisticRead();
        value = mValue;
        if (mStampedLock.validate(stamp))
            // If the optimistic read succeeds we're done.
            return value;
        else {
            // Otherwise, block for a readLock.
            stamp = mStampedLock.readLock();
            try {
               return mValue;
            } finally {
              mStampedLock.unlockRead(stamp);
            }
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

          // Downgrade to a readLock.
          stamp = mStampedLock.tryConvertToReadLock(stamp);
          assert (stamp != 0);

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
            // writeLock held.
            --mValue;
            // Downgrade to a readLock.
            stamp = mStampedLock.tryConvertToReadLock(stamp);
            assert (stamp != 0);
            return mValue;
        } finally {
            mStampedLock.unlock(stamp);
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the previous value
     */
    public long getAndIncrement() {
        long stamp = mStampedLock.readLock();
        long value = 0;

        try {
            for (;;) {
                value = mValue;

                long ws = mStampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0) {
                    mValue++;
                    stamp = ws;
                    break;
                } else {
                    mStampedLock.unlockRead(stamp);
                    stamp = mStampedLock.writeLock();
                }
            }
        } finally {
            mStampedLock.unlockWrite(stamp);
        }
        return value;
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return The previous value
     */
    public long getAndDecrement() {
        long stamp = mStampedLock.readLock();
        long value = 0;

        try {
            for (;;) {
                value = mValue;

                long ws = mStampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0) {
                    mValue--;
                    stamp = ws;
                    break;
                } else {
                    mStampedLock.unlockRead(stamp);
                    stamp = mStampedLock.writeLock();
                }
            }
        } finally {
            mStampedLock.unlockWrite(stamp);
        }
        return value;
    }
}

