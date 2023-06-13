package singletons;

/**
 * This class implements the Singleton pattern via a Java volatile
 * variable and the Double-Checked Locking pattern (see <a
 * href="https://en.wikipedia.org/wiki/Double-checked_locking"> this
 * link</a>) for more information about this pattern.
 */
public
class SingletonV<T> 
      implements Singleton<T> {
    /**
     * Implement the singleton using a static field that's defined as
     * volatile. This technique only works with Java 5 and later.
     */
    @SuppressWarnings("rawtypes")
    public static volatile SingletonV sSingleton;

    /**
     * Define a non-static field.
     */
    private T mField;

    /**
     * Private constructor prevents instantiation from outside the
     * class.
     */
    private SingletonV() {
        System.out.println(Thread.currentThread().getName()
            + ": creating SingletonV");
    }

    /**
     * * @return The value of the field.
     */
    @Override
    public T getField() {
        return mField;
    }

    /**
     * Set the contents of a field and return the old contents.
     *
     * @param f The new contents of the field
     * @return The old contents of the field
     */
    @Override
    @SuppressWarnings("UnusedReturnValue")
    public T setField (T f) {
        T temp = mField;
        mField = f;
        return temp;
    }

    /**
     * The static instance() method from the Singleton pattern.
     */
    @SuppressWarnings("unchecked")
    public static <T> Singleton<T> instance() {
        // Assign the volatile to a local variable.
        SingletonV<T> inst = sSingleton;

        // Run this code if the singleton is not yet initialized (this
        // check is atomic).
        if (inst == null) {
            // Only synchronize if inst == null to ensure the
            // constructor is only called once.
            synchronized(SingletonV.class) {
                inst = sSingleton;
                // Perform the second check (i.e., double-check),
                // which is atomic.
                if (inst == null) {
                    // Create the one-and-only instance (which is
                    // atomic).
                    sSingleton = inst = new SingletonV<T>();
                }
            }
        }

        // Return the singleton's current value.
        return inst;
    }
}

