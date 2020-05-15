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
        new AtomicReference<SingletonAR>(null);

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
     * The static instance() method from the SingletonAR pattern.
     */
    public static <T> SingletonAR instance() {
        // Get the current value of the singleton.
        SingletonAR<T> singleton = sSingletonAR.get();

        // Run this code if the singleton is not yet initialized.
        if (singleton == null) {
            // Create a new singleton object.  This constructor
            // may be called more than once..
            singleton = new SingletonAR<>();

            // Atomically set the reference's value to the
            // singleton iff the current value is null.
            if (!sSingletonAR.compareAndSet(null, singleton))
                // If the return is false then this not the first
                // time in, so just return the singleton's value.
                singleton = sSingletonAR.get();
        }

        // Return the singleton's current value.
        return singleton;
    }
}

