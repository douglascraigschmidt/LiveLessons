import utils.CountDownTimer;
import utils.NonReentrantSpinLock;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

/**
 * This example shows the difference between a reentrant lock (e.g.,
 * Java ReentrantLock) and a non-reentrant lock (e.g., a spin-lock
 * implemented using Java VarHandle features) when applied in a
 * framework that allows callbacks where the framework holds a lock
 * protecting internal framework state.  As you'll see when you run
 * this program, the reentrant lock supports this use-case nicely,
 * whereas the non-reentrant lock incurs "self-deadlock."
 */
public class ex24 {
    /**
     * Used to wait for the test to finish running.
     */
    private static CountDownLatch sCdl;

    /**
     * Main entry point into the test program.
     */
    public static void main (String[] argv) 
        throws IOException, InterruptedException {

        // Run the test using the ReentrantLock.
        runTest(new ReentrantLock(),
                10000,
                1000,
                "ReentrantLock");

        // Run the test using the NonReentrantSpinLock.
        runTest(new NonReentrantSpinLock(),
                10000,
                1000,
                "NonReentrantSpinLock");
    }

    /**
     * Run the CountDownTimer test for a given type of lock.
     * 
     * @param lock The lock to use for the test
     * @param millisInFuture The number of millis in the future from
     *                       the call to {@code start()} until the
     *                       countdown is done and {@code onFinish()}
     *                       is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@code onTick(long)} callbacks.
     * @param lockName The name of the lock (e.g., ReentrantLock
     *                vs. NonReentrantSpinLock)
     */
    private static void runTest(Lock lock,
                                long millisInFuture,
                                long countDownInterval,
                                String lockName) throws InterruptedException {
        // Test the CountDownTimer that's configured with the lock.
        testCountDownTimer(lock, millisInFuture, countDownInterval);

        // Blocks for 10 seconds since the countdown timer will
        // deadlock and thus never finish..
        if (sCdl.await(10, TimeUnit.SECONDS))
            // Indicate that the test finished successfully.
            System.out.println(lockName
                               + " test finished successfully");
        else
            // Indicate that the test finished unsuccessfully.
            System.out.println(lockName
                               + " test finished unsuccessfully due to self-deadlock");
    }

    /**
     * Test the CountDownTimer using the given {@code lock}.
     *
     * @param lock The lock used to synchronize access to the object,
     *   which can either be reentrant (good) or non-reentrant (bad) 
     * @param millisInFuture The number of millis in the future from the call
     *   to {@code start()} until the countdown is done and {@code onFinish()}
     *   is called.
     * @param countDownInterval The interval along the way to receive
     *   {@code onTick(long)} callbacks.
     */
    private static void testCountDownTimer(Lock lock,
                                           long millisInFuture,
                                           long countDownInterval) {
        // Initialize the countDownLatch used to wait until the test
        // is done.
        sCdl = new CountDownLatch(1);

        // Create a countdown that displays a text string.
        CountDownTimer cdt = new CountDownTimer(lock, 
                                                millisInFuture,
                                                countDownInterval) {
                /**
                 * Callback fired on regular interval.
                 * @param millisUntilFinished The amount of time until finished.
                 */
                public void onTick(long millisUntilFinished) {
                    System.out.println("seconds remaining: " + 
                                       millisUntilFinished / 1000);
                    // Try to cancel the timer after its about halfway
                    // done.
                    if ((millisInFuture - millisUntilFinished) > millisUntilFinished) {
                        // This call will trigger self-deadlock if a
                        // non-reentrant lock is used.
                        this.cancel();

                        // Decrement the latch to release the waiter.
                        sCdl.countDown();
                    }
                }
 
                /**
                 * Callback fired when the time is up.
                 */
                public void onFinish() {
                    System.out.println("done");
                }
            };

        // Start the countdown timer.
        cdt.start();
    }
}
