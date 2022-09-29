import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This example shows how to implement the Singleton pattern via Java
 * AtomicReference.
 */
@SuppressWarnings("ALL")
public class ex28 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex28.class.getName();

    static class Singleton<T> {
        /**
         * Implement the singleton using an AtomicReference.
         */
        public static AtomicReference<Singleton> sSingleton =
            new AtomicReference<Singleton>(null);

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
        public static <T> Singleton instance() {
            // Get the current value of the singleton.
            Singleton<T> singleton = sSingleton.get();

            // Run this code if the singleton is not yet initialized.
            if (singleton == null) {
                // Create a new singleton object.  This constructor
                // may be called more than once..
                singleton = new Singleton<>();

                // Atomically set the reference's value to the
                // singleton iff the current value is null.
                if (!sSingleton.compareAndSet(null, singleton))
                    // If the return is false then this not the first
                    // time in, so just return the singleton's value.
                    singleton = sSingleton.get();
            }

            // Return the singleton's current value.
            return singleton;
        }
    }

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Run the test program.

        // Set the value of the singleton's field to an Integer.
        Singleton.instance().setField(new Integer(100));

        // Return the current value of the singleton.
        Integer i = (Integer) Singleton.instance().getField();

        // Return the result.
        System.out.println("value = " + i);
    }
}
