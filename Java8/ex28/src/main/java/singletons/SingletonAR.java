package singletons;

import java.util.concurrent.atomic.AtomicReference;

/**
 * This class implements the Singleton pattern via a Java
 * {@link AtomicReference}.
 */
public class SingletonAR<T>
       implements Singleton<T> {
    /**
     * Implement the singleton using an AtomicReference.
     */
    @SuppressWarnings("rawtypes")
    public static AtomicReference<SingletonAR> sSingletonAR =
        new AtomicReference<>(null);

    /**
     * Define a non-static field.
     */
    private T mField;

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private SingletonAR() {
        System.out.println(Thread.currentThread().getName()
            + ": creating SingletonAR");
    }

    /**
     * * @return The value of the field
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
        // Atomically set the reference's value to a new singleton iff
        // the current value is null. This constructor will most likely
        // be called more than once if instance() is called from
        // multiple threads, but only the first one is used.
        sSingletonAR
            .updateAndGet(u ->
                          u != null ? u : new SingletonAR<T>());

        // Return the singleton's current value.
        return (Singleton<T>) sSingletonAR.get();
    }
}

