package atomiclongs;

import java.util.concurrent.locks.StampedLock;

/**
 * This class implements a subset of the {@link AbstractAtomicLong}
 * interface using a {@link StampedLock} to illustrate its "optimistic" read
 * locks, its "pessimistic" write locks, and its conditional write locks.
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
        // Store the initial value.
        mValue = initialValue;
    }

    /**
     * Gets the current value using an "optimistic" read lock.
     * 
     * @return The current value
     */
    public long get() {
        // Local variable to hold the value;
        long value;

        // First try an optimistic read.
        long stamp = mStampedLock.tryOptimisticRead();

        // Assign mValue to a local variable.
        value = mValue;

        // If the optimistic read succeeds, simply return the value.
        if (mStampedLock.validate(stamp))
            return value;
        else {
            // Otherwise, block to get a read lock.
            stamp = mStampedLock.readLock();
            try {
                // Return the value with the read lock held.
                return mValue;
            } finally {
                // Always unlock the lock in a finally block.
                mStampedLock.unlockRead(stamp);
            }
        }
    }

    /**
     * Atomically increment the current value by one using
     * a "pessimistic" write lock.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
        // Block until we get a write lock.
        long stamp = mStampedLock.writeLock();

        try {
            // Increment & return the value with the write lock held.
            return mValue++; 
        } finally {
            // Always unlock the lock in a finally block.
            mStampedLock.unlockWrite(stamp);
        }
    }
    
    /**
     * Atomically decrements by one the current value using
     * a "pessimistic" write lock.
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
            // Always unlock the lock in a finally block.
            mStampedLock.unlockWrite(stamp);
        }
    }

    /**
     * Atomically increment the current value by one using
     * a conditional write lock.
     *
     * @return the previous value before the increment
     */
    public long getAndIncrement() {
        // Block until we get a read lock.
        long stamp = mStampedLock.readLock();

        // Local variable to hold the value;
        long value = 0;

        try {
            // Keep looping until we get a write lock.
            for (;;) {
                // This read is guaranteed to be valid.
                value = mValue;

                // Try to convert to a write lock.
                long ws = mStampedLock
                    .tryConvertToWriteLock(stamp);

                // If conversion succeeded (ws != 0), we're done.
                if (ws != 0) {
                    mValue++;
                    stamp = ws;
                    break;
                } else {
                    // Otherwise, unlock the read lock and acquire a
                    // write lock.

                    // Unlock the read lock.
                    mStampedLock.unlockRead(stamp);

                    // Block until we get a write lock.
                    stamp = mStampedLock.writeLock();
                }
            }
        } finally {
            // Always unlock the lock in a finally block.
            mStampedLock.unlockWrite(stamp);
        }

        // Return the incremented value.
        return value;
    }

    /**
     * Atomically decrements by one the current value using
     * a conditional write lock.
     *
     * @return The previous value before the decrement
     */
    public long getAndDecrement() {
        // Block until we get a read lock.
        long stamp = mStampedLock.readLock();
        long value = 0;

        try {
            // Keep looping until we get a write lock.
            for (;;) {
                // This read is guaranteed to be valid since
                // the read lock is held.
                value = mValue;

                // Try to a convert to a write lock.
                long ws = mStampedLock
                    .tryConvertToWriteLock(stamp);

                // If conversion succeeded (ws != 0), we're done.
                if (ws != 0) {
                    mValue--;
                    stamp = ws;
                    break;
                } else {
                    // Otherwise, unlock the read lock and acquire a
                    // write lock.

                    // Unlock the read lock.
                    mStampedLock.unlockRead(stamp);

                    // Block until we get a write lock.
                    stamp = mStampedLock.writeLock();
                }
            }
        } finally {
            // Always unlock the lock in a finally block.
            mStampedLock.unlockWrite(stamp);
        }

        // Return the decremented value.
        return value;
    }
}

