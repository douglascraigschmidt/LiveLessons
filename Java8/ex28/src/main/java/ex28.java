import singletons.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static utils.ExceptionUtils.*;

/**
 * This example shows several ways to implement the Singleton pattern
 * via a Java AtomicReference, a Java volatile variable (via the
 * Double-Checked Locking pattern), the "holder" idiom, and a Java
 * enum.  It also tests all the Singleton implementations concurrently.
 */
@SuppressWarnings("ALL")
public class ex28 {
    /**
     * The maximum number of Thread objects that access each
     * type of Singleton.
     */
    private static int sMAX_THREADS_PER_SINGLETON = 2;

    /**
     * Java requires a static main() entry point method to run the
     * program.
     */
    public static void main(String[] args)
        throws InterruptedException {
        // Used to wait for a random amount of time.
        Random random = new Random();

        // Create multiple threads to access the Singleton instances
        // concurrently.
        List<Thread> threads = new ArrayList<>();

        // Initialize Thread objects.
        for (int i = 0; i < sMAX_THREADS_PER_SINGLETON; i++) {
            // Initialize Thread objects using the AtomicReference
            // singleton.
            threads
                .add(new Thread(() ->
                                runTask(random,
                                        SingletonAR.<String>instance()),
                    "SingletonAR(" + i + ")"));

            // Initialize Thread objects using the holder idiom singleton.
            threads
                .add(new Thread(() ->
                                runTask(random,
                                        SingletonH.<String>instance()),
                    "SingletonH(" + i + ")"));

            // Initialize Thread objects using the volatile singleton.
            threads
                .add(new Thread(() ->
                                runTask(random,
                                        SingletonV.<String>instance()),
                    "SingletonV(" + i + ")"));

            // Initialize Thread objects using the enum singleton.
            threads
                .add(new Thread(() ->
                                runTask(random,
                                        SingletonE.INSTANCE),
                    "SingletonE(" + i + ")"));
        }

        // Start all the Thread objects.
        threads.forEach(Thread::start);

        // Wait for all Thread objects to complete.
        threads.forEach(rethrowConsumer(Thread::join));
    }

    /**
     * Run a task that accesses a Singleton instance.
     *
     * @param random The {@link Random} number generator
     * @param singleton The Singleton instance to access
     */
    private static void runTask(Random random,
                                Singleton<String> singleton) {
        // Get the name of the Thread.
        String threadName = Thread.currentThread().getName();

        // Sleep for a random amount of time between 0 and 10 seconds.
        rethrowRunnable(() ->
            Thread.sleep(random.nextInt(10000)));

        // Get the initial value of the singleton field.
        var initialValue = singleton.getField();

        // Access the singleton and print the field value
        System.out.println(threadName
                           + ": Field value before update: "
                           + initialValue);

        // Update the singleton's field value.
        initialValue = singleton.setField(threadName);

        // Get the updated value of the singleton field.
        var updatedValue = singleton.getField();

        // Access the singleton again and print the updated field value
        System.out.println(threadName
                           + ": Field value after update: "
                           + updatedValue);
    }
}
