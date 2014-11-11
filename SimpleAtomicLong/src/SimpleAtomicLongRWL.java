import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * @class SimpleAtomicLongRWL
 *
 * @brief This class implements a subset of the
 *        java.util.concurrent.atomic.AtomicLong class using a
 *        ReentrantReadWriteLock to illustrate how they work.
 */
public class SimpleAtomicLongRWL {
    /**
     * The value that's manipulated atomically via the methods.
     */
    private long mValue;

    /**
     * The ReentrantReadWriteLock used to serialize access to mValue.
     */
    private ReentrantReadWriteLock mRWLock = new ReentrantReadWriteLock();
    private Lock mReadLock = mRWLock.readLock();
    private Lock mWriteLock = mRWLock.writeLock();

    /**
     * Creates a new SimpleAtomicLong with the given initial value.
     */
    public SimpleAtomicLongRWL(long initialValue) {
        mValue = initialValue;
    }

    /**
     * @brief Gets the current value
     * 
     * @returns The current value
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
     * @brief Atomically decrements by one the current value
     *
     * @returns the updated value
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
     * @brief Atomically increments by one the current value
     *
     * @returns the previous value
     */
    public long getAndIncrement() {
        mWriteLock.lock();
        
        long temp = mValue;
        mValue++;
        
        mWriteLock.unlock();
        return temp;
    }

    /**
     * @brief Atomically increments by one the current value
     *
     * @returns the updated value
     */
    public long incrementAndGet() {
    	long value = 0;
        Lock lock = mRWLock.writeLock();
        lock.lock(); 
        try {
          mValue++; // writeLock held.
          final Lock readLock = mRWLock.readLock();
          readLock.lock(); // Downgrade
          try {
            lock.unlock(); 
            value = mValue; 
          } finally { lock = readLock; }
        } finally {
          lock.unlock(); 
        }
        return value;
    }
    
    /**
     * @brief Atomically decrements by one the current value
     *
     * @returns the previous value
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

