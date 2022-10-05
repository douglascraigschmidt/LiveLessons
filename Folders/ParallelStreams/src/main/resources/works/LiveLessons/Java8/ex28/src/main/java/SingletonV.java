/**
 * This class implements the Singleton pattern via a Java volatile
 * variable and the Double-Checked Locking pattern (see
 * https://en.wikipedia.org/wiki/Double-checked_locking).
 */
class SingletonV<T> {
    /**
     * Implement the singleton using a static field.
     */
    public static volatile SingletonV sSingleton;

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
    public static <T> SingletonV instance() {
        // Assign the volatile to a local variable.
        SingletonV inst = sSingleton;

        // Run this code if the singleton is not yet initialized.
        if (inst == null) {
            // Only synchronize if inst == null.
            synchronized(SingletonV.class) {
                inst = sSingleton;
                // Perform the second check (i.e., double-check)
                if (inst == null)
                    // Create the one-and-only instance.
                    sSingleton = inst = new SingletonV();
            }
        }

        // Return the singleton's current value.
        return inst;
    }
}

