package atomiclongs;

import java.util.concurrent.locks.StampedLock;

/**
 * This class implements a subset of the Java {@link AbstractAtomicLong} class
 * using a {@link StampedLock} to illustrate how they work.
 */
public class AtomicLongStampedLock
       implements AbstractAtomicLong {
    /**
     * The value that's manipulated atomically via the methods.
     */
    private long mValue;

    /**
     * The {@link StampedLock} used to serialize access to mValue.
     */
    private final StampedLock mStampedLock =
        new StampedLock();

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

        // Assign mValue to a local variable.
        value = mValue;

        if (mStampedLock.validate(stamp))
            // If the optimistic read succeeds, we're done.
            return value;
        else {
            // Otherwise, block to get a readLock.
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
        // Block until we get a writeLock.
        long stamp = mStampedLock.writeLock();

        try {
            // writeLock held.
            return mValue++; 
        } finally {
          mStampedLock.unlockWrite(stamp);
        }
    }
    
    /**
     * Atomically decrements by one the current value.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        // Block until we get a writeLock.
        long stamp = mStampedLock.writeLock();

        try {
            // writeLock held.
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
        // Block until we get a read lock.
        long stamp = mStampedLock.readLock();
        long value = 0;

        try {
            // Keep looping until we get a write lock.
            for (;;) {
                // This read is guaranteed to be valid.
                value = mValue;

                // Try to convert to a write lock.
                long ws = mStampedLock.tryConvertToWriteLock(stamp);

                // If conversion succeeded (ws != 0), we're done.
                if (ws != 0) {
                    mValue++;
                    stamp = ws;
                    break;
                } else {
                    // Otherwise, unlock the read lock and try again.
                    // (the read lock may have just been acquired
                    // by another Thread.)

                    // Unlock the read lock.
                    mStampedLock.unlockRead(stamp);

                    // Reacquire the read lock.
                    stamp = mStampedLock.readLock();
                }
            }
        } finally {
            // Unlock the write lock.
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
        // Block until we get a read lock.
        long stamp = mStampedLock.readLock();
        long value = 0;

        try {
            // Keep looping until we get a write lock.
            for (;;) {
                // This read is guaranteed to be valid.
                value = mValue;

                // Try to a convert to a write lock.
                long ws = mStampedLock.tryConvertToWriteLock(stamp);
                if (ws != 0) {
                    mValue--;
                    stamp = ws;
                    break;
                } else {
                    // Otherwise, unlock the read lock and try again.
                    // (the read lock may have just been acquired
                    // by another Thread.)

                    // Unlock the read lock.
                    mStampedLock.unlockRead(stamp);

                    // Reacquire the read lock.
                    stamp = mStampedLock.readLock();
                }
            }
        } finally {
            // Unlock the write lock.
            mStampedLock.unlockWrite(stamp);
        }
        return value;
    }
}

