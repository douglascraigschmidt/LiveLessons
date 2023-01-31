import utils.CountDownTimer;
import utils.ExampleCountDownTimer;
import utils.NonReentrantSpinLock;

import java.io.IOException;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This example shows the difference between a reentrant lock (e.g.,
 * Java {@link ReentrantLock}) and a non-reentrant lock (e.g., a
 * spin-lock implemented using Java {@link VarHandle} features) when
 * applied in a framework that allows callbacks where the framework
 * holds a lock protecting internal framework state.  As you'll see
 * when you run this program, the reentrant lock supports this
 * use-case nicely, whereas the non-reentrant lock incurs
 * "self-deadlock."
 */
public class ex24 {
    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) 
        throws IOException, InterruptedException {
        // Continue the countdown for 10 seconds.
        int millisInFuture = 10_000;

        // Print a message every second.
        int countDownInterval = 1_000;

        // Run the test using the ReentrantLock.
        runTest(new ReentrantLock(),
                millisInFuture,
                countDownInterval,
                "ReentrantLock");

        // Run the test using the NonReentrantSpinLock.
        runTest(new NonReentrantSpinLock(),
                millisInFuture,
                countDownInterval,
                "NonReentrantSpinLock");
    }

    /**
     * Run the CountDownTimer test for a given type of lock.
     * 
     * @param lock The lock to use for the test
     * @param millisInFuture The number of millis in the future from
     *                       the call to {@code start()} until the
     *                       countdown is done and {@code onFinish()}
     *                       is called
     * @param countDownInterval The interval along the way to receive
     *                          {@code onTick(long)} callbacks
     * @param lockName The name of the lock (e.g., ReentrantLock
     *                 vs. NonReentrantSpinLock)
     */
    private static void runTest(Lock lock,
                                long millisInFuture,
                                long countDownInterval,
                                String lockName)
            throws InterruptedException {
        // Used to wait for the test to finish running.
        CountDownLatch cdl =
            // Test the CountDownTimer that's configured with the
            // lock.
            testCountDownTimer(lock,
                               millisInFuture,
                               countDownInterval);

        System.out.println(lockName
                           + " test started...");

        // Blocks for 10 seconds since the countdown timer will
        // deadlock and thus never finish.
        if (cdl.await(10, TimeUnit.SECONDS))
            // Indicate that the test finished successfully.
            System.out.println("... and finished successfully");
        else
            // Indicate that the test finished unsuccessfully.
            System.out
                .println("... and failed due to self-deadlock");
    }

    /**
     * Test the CountDownTimer using the given {@code lock}.
     *
     * @param lock The lock used to synchronize access to the object,
     *        which can either be reentrant (good) or non-reentrant (bad) 
     * @param millisInFuture The number of millis in the future from
     *        the call to {@code start()} until the countdown is done
     *        and {@code onFinish()} is called
     * @param countDownInterval The interval along the way to receive
     *        {@code onTick(long)} callbacks
     */
    private static CountDownLatch testCountDownTimer(Lock lock,
                                           long millisInFuture,
                                           long countDownInterval) {
        // Initialize the countDownLatch used to wait until the test
        // is done.
        CountDownLatch cdl = new CountDownLatch(1);

        // Create an ExampleCountDownTimer that displays a text string.
        CountDownTimer cdt = new ExampleCountDownTimer(lock,
                                                       millisInFuture,
                                                       countDownInterval,
                                                       cdl);

        // Start the countdown timer.
        cdt.start();

        // Return the CountDownLatch.
        return cdl;
    }
}

