package atomiclongs;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * This class implements a subset of the Java {@link AbstractAtomicLong}
 * class using a {@link ReentrantReadWriteLock} to illustrate how its
 * methods work.
 */
public class AtomicLongRWL
       implements AbstractAtomicLong {
    /**
     * The value that's manipulated atomically via the methods below.
     */
    private long mValue;

    /**
     * The {@link ReentrantReadWriteLock} used to serialize access to
     * mValue.
     */
    private final ReentrantReadWriteLock mRWLock =
        new ReentrantReadWriteLock();

    /**
     * The {@link Lock} used for reading.
     */
    private final Lock mReadLock = 
        mRWLock.readLock();

    /**
     * The {@link Lock} used for writing.
     */
    private final Lock mWriteLock =
        mRWLock.writeLock();

    /**
     * Creates a new {@link AtomicLongRWL} with the given initial
     * value.
     */
    public AtomicLongRWL(long initialValue) {
        // Assign the initial value.
        mValue = initialValue;
    }

    /**
     * Gets the current value.
     * 
     * @return The current value
     */
    public long get() {
        // Only a read lock is needed here.
        mReadLock.lock();

        try {
            // Return the value since it's protected by the read lock.
            return mValue;
        } finally {
            // Always unlock a lock in the finally block.
            mReadLock.unlock();
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
        // This implementation demonstrates how to use readers-writer
        // lock downgrading (see
        // https://medium.com/double-pointer/guide-to-readwritelock-in-java-72c3a273b6e9
        // for more info on this technique).

    	long value = 0;

        // Start out with a write lock.
        Lock lock = mWriteLock;

        // Block until the lock is acquired.
        lock.lock();

        try {
            // Increment the value with the write lock held.
            mValue++;

            // Downgrade to a read lock to minimize the time
            // spent holding the write lock.
            Lock readLock = mReadLock;
            readLock.lock();

            try {
                // Unlock the write lock.
                lock.unlock();

                // Read the value with the read lock held.
                value = mValue; 
            } finally {
                // Assign 'lock' to the read lock.
                lock = readLock; 
            }
        } finally {
            // Always unlock a lock in the finally block.
            lock.unlock(); 
        }

        // Return the incremented value.
        return value;
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        // Block until the write lock is acquired.
        mWriteLock.lock();

        try {
            // Decrement and return the value with the write lock held.
            return --mValue;
        } finally {
            // Always unlock a lock in the finally block.
            mWriteLock.unlock();
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the previous value
     */
    public long getAndIncrement() {
        // Block until the write lock is acquired.
        mWriteLock.lock();

        try {
            // Increment & return the old value with the write lock held.
            return mValue++;
        } finally {
            // Always unlock a lock in the finally block.
            mWriteLock.unlock();
        }
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return The previous value
     */
    public long getAndDecrement() {
        // Block until the write lock is acquired.
        mWriteLock.lock();

        try {
            // Decrement & return the old value with the write lock held.
            return mValue--;
        } finally {
            // Always unlock a lock in the finally block.
            mWriteLock.unlock();
        }
    }
}

