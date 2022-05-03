package atomiclongs;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 *
 */
public class AtomicLongVarHandle
       implements AtomicLong {
    private static final VarHandle VALUE;

    static {
        try {
            MethodHandles.Lookup l = MethodHandles.lookup();
            VALUE = l.findVarHandle (AtomicLongVarHandle.class, "mValue", long.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    private volatile long mValue;

    /**
     * Creates a new {@link AtomicLongVarHandle} with the given
     * initial value.
     */
    public AtomicLongVarHandle(long initialValue) {
        mValue = initialValue;
    }

    /**
     * Get the current value.
     * 
     * @return The current value
     */
    public long get() {
        return mValue;
    }

    /**
     * Atomically increments the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
        return ((long) VALUE.getAndAdd(this, 1L)) + 1L;
    }

    /**
     * Atomically decrement the current value by one.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        return ((long) VALUE.getAndAdd(this, -1L)) - 1L;
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return The previous value
     */
    public long getAndIncrement() {
        return (long) VALUE.getAndAdd(this, 1L);
    }

    /**
     * Atomically decrement the current value by one.
     *
     * @return The previous value
h     */
    public long getAndDecrement() {
        return (long) VALUE.getAndAdd(this, -1L);
    }
}  
