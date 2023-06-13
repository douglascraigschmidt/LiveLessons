package atomiclongs;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * This class implements an {@link AbstractAtomicLong} that uses
 * a {@link VarHandle} to access the underlying volatile value
 * in a thread-safe and portable manner.
 */
public class AtomicLongVarHandle
    implements AbstractAtomicLong {
    /**
     * The {@link VarHandle} used to access {@code mValue}.
     */
    private static final VarHandle VALUE;

    /*
     * This static initializer initializes the {@link #VALUE} field.
     */
    static {
        try {
            // These method calls use Java reflection get a VarHandle to
            // access the private mValue field of this class.
            MethodHandles.Lookup l = MethodHandles.lookup();
            VALUE = l.findVarHandle(AtomicLongVarHandle.class,
                "mValue",
                long.class);
        } catch (ReflectiveOperationException e) {
            throw new Error(e);
        }
    }

    /**
     * The current value that's protected by the {@link VarHandle} above.
     */
    @SuppressWarnings("FieldMayBeFinal")
    private volatile long mValue;

    /**
     * Creates a new {@link AtomicLongVarHandle} with the given
     * initial value.
     */
    public AtomicLongVarHandle(long initialValue) {
        // Store the initial value in the mValue field.
        mValue = initialValue;
    }

    /**
     * Get the current value.
     *
     * @return The current value
     */
    public long get() {
        // This read is atomic because mValue is a volatile field.
        return mValue;
    }

    /**
     * Atomically increments the current value by one.
     *
     * @return the updated value
     */
    public long incrementAndGet() {
        return ((long) VALUE
            // Atomically increment by 1 and return the updated value.
            .getAndAdd(this, 1L)) + 1L;
    }

    /**
     * Atomically decrement the current value by one.
     *
     * @return The updated value
     */
    public long decrementAndGet() {
        return ((long) VALUE
            // Atomically decrement by 1 and return the updated value.
            .getAndAdd(this, -1L)) - 1L;
    }

    /**
     * Atomically increment the current value by one.
     *
     * @return The previous value
     */
    public long getAndIncrement() {
        return (long) VALUE
            // Atomically increment by 1 and return the previous value.
            .getAndAdd(this, 1L);
    }

    /**
     * Atomically decrement the current value by one.
     *
     * @return The previous value
     * h
     */
    public long getAndDecrement() {
        return (long) VALUE
            // Atomically decrement by 1 and return the previous value.
            .getAndAdd(this, -1L);
    }
}  
