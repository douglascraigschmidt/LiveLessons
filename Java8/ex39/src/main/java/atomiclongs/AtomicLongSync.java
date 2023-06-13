package atomiclongs;

/**
 * This class implements a subset of the {@code AtomicLong} class
 * using synchronized statements and volatile to illustrate how they
 * work.
 */
public class AtomicLongSync
       implements AbstractAtomicLong {
    /**
     * The value that's manipulated atomically via the methods.
     */
    private volatile long mValue;

    /**
     * Creates a new SimpleAtomicLong with the given initial value.
     */
    public AtomicLongSync(long initialValue) {
        // Store the initial value in mValue.
        mValue = initialValue;
    }

    /**
     * Get the current value.
     * 
     * @return The current value
     */
    public long get() {
        // This read is guaranteed to be atomic.
        return mValue;
    }

    /**
     * Atomically increments the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
        synchronized (this) {
            // This read and write are guaranteed to be atomic.
            return ++mValue;
        }
    }

    /**
     * Atomically decrement the current value by one.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        synchronized (this) {
            // This read and write are guaranteed to be atomic.
            return --mValue;
        }
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return The previous value
     */
    public long getAndIncrement() {
        synchronized (this) {
            // This read and write are guaranteed to be atomic.
            return mValue++;
        } 
    }

    /**
     * Atomically decrement the current value by one.
     *
     * @return The previous value
     */
    public long getAndDecrement() {
        synchronized (this) {
            // This read and write are guaranteed to be atomic.
            return mValue--;
        } 
    }
}

