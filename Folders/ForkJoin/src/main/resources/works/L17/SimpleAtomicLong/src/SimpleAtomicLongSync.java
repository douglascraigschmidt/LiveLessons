import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * @class SimpleAtomicLongSync
 *
 * @brief This class implements a subset of the
 *        java.util.concurrent.atomic.AtomicLong class using
 *        synchronized statements and volatile to illustrate how they
 *        work.
 */
public class SimpleAtomicLongSync {
    /**
     * The value that's manipulated atomically via the methods.
     */
    private volatile long mValue;

    /**
     * Creates a new SimpleAtomicLong with the given initial value.
     */
    public SimpleAtomicLongSync(long initialValue) {
        mValue = initialValue;
    }

    /**
     * @brief Gets the current value
     * 
     * @returns The current value
     */
    public long get() {
        return mValue;
    }

    /**
     * @brief Atomically decrements by one the current value
     *
     * @returns the updated value
     */
    public long decrementAndGet() {
        synchronized (this) {
            return --mValue;
        }
    }

    /**
     * @brief Atomically increments by one the current value
     *
     * @returns the previous value
     */
    public long getAndIncrement() {
        synchronized (this) {
            return mValue++;
        } 
    }

    /**
     * @brief Atomically decrements by one the current value
     *
     * @returns the previous value
     */
    public long getAndDecrement() {
        synchronized (this) {
            return mValue--;
        } 
    }

    /**
     * @brief Atomically increments by one the current value
     *
     * @returns the updated value
     */
    public long incrementAndGet() {
        synchronized (this) {
            return ++mValue;
        }
    }
}

