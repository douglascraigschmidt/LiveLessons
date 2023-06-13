package singletons;

/**
 * This class implements the Singleton pattern with a non-static
 * field.  It follows the "holder" idiom for lazy initialization to
 * ensure thread-safe access to the singleton instance.  See <a
 * href="https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom">
 * this link</a> for more information about this idiom.
 */
public class SingletonH<T>
       implements Singleton<T> {
    /**
     * Define a non-static field.
     */
    private T mField;

    /**
     * Private constructor prevents instantiation from outside the
     * class.
     */
    private SingletonH() {
            System.out.println(Thread.currentThread().getName()
                + ": creating SingletonH");
    }

    /**
     * @return The value of the field
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
    @SuppressWarnings("UnusedReturnValue")
    @Override
    public T setField(T f) {
        T temp = mField;
        mField = f;
        return temp;
    }

    /**
     * Implements the "holder" idiom.
     */
    public static class SingletonHolder {
        /**
         * Implement the singleton using a static final field.  This
         * field will be initialized when the {@link SingletonHolder}
         * class is loaded.
         */
        private static final SingletonH<?> INSTANCE =
            new SingletonH<>();
    }

    /**
     * Access the singleton instance.
     *
     * @return The singleton instance
     */
    @SuppressWarnings("unchecked")
    public static <T> Singleton<T> instance() {
        // Return the singleton instance from the SingletonHolder.
        return (Singleton<T>) SingletonHolder.INSTANCE;
    }
}
