package atomiclongs;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * This class implements a subset of the Java {@link AbstractAtomicLong} class
 * using a {@link ReentrantReadWriteLock} to illustrate how they work.
 */
public class AtomicLongRWL
       implements AbstractAtomicLong {
    /**
     * The value that's manipulated atomically via the methods.
     */
    private long mValue;

    /**
     * The {@link ReentrantReadWriteLock} used to serialize access to
     * mValue.
     */
    private final ReentrantReadWriteLock mRWLock =
        new ReentrantReadWriteLock();
    private final Lock mReadLock = 
        mRWLock.readLock();
    private final Lock mWriteLock =
        mRWLock.writeLock();

    /**
     * Creates a new {@link AtomicLongRWL} with the given initial
     * value.
     */
    public AtomicLongRWL(long initialValue) {
        mValue = initialValue;
    }

    /**
     * Gets the current value.
     * 
     * @return The current value
     */
    public long get() {
        mReadLock.lock();

        try {
            return mValue;
        } finally {
            mReadLock.unlock();
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
    	long value = 0;
        Lock lock = mWriteLock;
        lock.lock(); 
        try {
          mValue++; // writeLock held.
          final Lock readLock = mReadLock;
          readLock.lock(); // Downgrade.
          try {
            lock.unlock(); 
            value = mValue; 
          } finally { 
              lock = readLock; 
          }
        } finally {
          lock.unlock(); 
        }
        return value;
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        mWriteLock.lock();

        try {
            return --mValue;
        } finally {
            mWriteLock.unlock();
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return the previous value
     */
    public long getAndIncrement() {
        mWriteLock.lock();

        try {
            long temp = mValue;
            mValue++;
            return temp;
        } finally {
            mWriteLock.unlock();
        }
    }

    /**
     * Atomically decrements by one the current value.
     *
     * @return The previous value
     */
    public long getAndDecrement() {
        mWriteLock.lock();

        try {
            return mValue--;
        } finally {
            mWriteLock.unlock();
        }
    }
}

