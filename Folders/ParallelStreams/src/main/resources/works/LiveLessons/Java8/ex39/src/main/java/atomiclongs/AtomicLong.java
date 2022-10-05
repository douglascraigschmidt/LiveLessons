package atomiclongs;

/**
 * Define an interface for the various SimpleAtomicLong implementations.
 */
public interface AtomicLong {
    /**
     * Gets the current value.
     * 
     * @return The current value
     */
    public long get();

    /**
     * Atomically decrements by one the current value.
     *
     * @return The updated value
     */
    public long decrementAndGet();

    /**
     * Atomically increment the current value by one.
     *
     * @return the previous value
     */
    public long getAndIncrement();

    /**
     * Atomically increment the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet();

    /**
     * Atomically decrements by one the current value.
     *
     * @return The previous value
     */
    public long getAndDecrement();
}


