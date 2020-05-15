import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This example shows several ways to implement the Singleton pattern
 * via a Java AtomicReference and a Java volatile variable (via the
 * Double-Checked Locking pattern).
 */
@SuppressWarnings("ALL")
public class ex28 {
    /**
     * Logging tag.
     */
    private static final String TAG = ex28.class.getName();

    /**
     * The Java execution environment requires a static main() entry
     * point method to run the app.
     */
    public static void main(String[] args) {
        // Run the test program.

        // Set the value of the singleton's field to an Integer.
        SingletonAR.instance().setField(new Integer(100));

        // Return the current value of the singleton.
        Integer i = (Integer) SingletonAR.instance().getField();

        // Return the result.
        System.out.println("value = " + i);

        // Set the value of the singleton's field to an Integer.
        SingletonV.instance().setField(new Integer(1000));

        // Return the current value of the singleton.
        Integer ii = (Integer) SingletonV.instance().getField();

        // Return the result.
        System.out.println("value = " + ii);
    }
}
