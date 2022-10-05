import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements the Singleton pattern via a Java
 * AtomicReference.
 */
class SingletonAR<T> {
    /**
     * Implement the singleton using an AtomicReference.
     */
    public static AtomicReference<SingletonAR> sSingletonAR =
        new AtomicReference<>(null);

    /**
     * Define a non-static field.
     */
    private T mField;

    /**
     * * @return The value of the field.
     */
    public T getField() {
        return mField;
    }

    /**
     * Set and return the value of the field.
     */
    public T setField (T f) { return mField = f; }

    /**
     * The static instance() method from the Singleton pattern.
     */
    public static <T> SingletonAR instance() {
        // Atomically set the reference's value to a new singleton iff
        // the current value is null.  The SingletonAR constructor may
        // be called more than once..
        sSingletonAR.compareAndSet(null, new SingletonAR<>());

        // Return the singleton's current value.
        return sSingletonAR.get();
    }
}

