import utils.CountDownTimer;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.StampedLock;

/**
 * This example shows the difference between a reentrant lock (e.g.,
 * Java ReentrantLock) and a non-reentrant lock (e.g., Java
 * StampedLock) when applied in a framework that allows callbacks
 * where the framework holds a lock protecting framework state.  As
 * you'll see when you run this program, the reentrant lock supports
 * this use-case nicely, whereas the non-reentrant lock incurs
 * "self-deadlock."
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

        // Test the CountDownTimer that's configured with a
        // ReentrantLock.
        testCountDownTimer(new ReentrantLock(), 10000, 1000);

        // Block until the countdown timer is canceled.
        sCdl.await();

        // Indicate that the test finished.
        System.out.println("ReentrantLock test finished cleanly");

        // Test the CountDownTimer that's configured with a
        // NonReentrantLock.
        testCountDownTimer(new NonReentrantLock(), 10000, 1000);

        // Blocks for 10 seconds since the countdown timer will
        // deadlock and thus never finish..
        sCdl.await(10, TimeUnit.SECONDS);

        // Indicate that the test finished.
        System.out.println("NonReentrantLock test finished after a delay due to self-deadlock");
    }

    /**
     * Test the CountDownTimer using the given {@code lock}.
     *
     * @param lock The lock used to synchronize access to the object,
     *   which can either be reentrant (good) or non-reentrant (bad) 
     * @param millisInFuture The number of millis in the future from the call
     *   to {@link #start()} until the countdown is done and {@link #onFinish()}
     *   is called.
     * @param countDownInterval The interval along the way to receive
     *   {@link #onTick(long)} callbacks.
     */
    private static void testCountDownTimer(Lock lock,
                                           long millisInFuture,
                                           long countDownInterval) {
        // Initialize the countdownlatch used to wait until the test
        // is done.
        sCdl = new CountDownLatch(1);

        // Create a countdown that displays a text string.
        new CountDownTimer(lock, 
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
        }
        // Start the countdown timer.
        .start();
    }

    /**
     * Use a StampedLock to implement a non-reentrant lock.
     */
    private static class NonReentrantLock 
                   implements Lock {
        /**
         * StampedLock is non-reentrant.
         */
        private StampedLock mLock = new StampedLock();

        /**
         * Store the stamp to use for unlocking.
         */
        private long mStamp;

        @Override
        public void lock() {
            mStamp = mLock.writeLock();
        }

        @Override
        public void lockInterruptibly() throws InterruptedException {
            mStamp = mLock.writeLock();
        }

        @Override
        public boolean tryLock() {
            mStamp = mLock.tryWriteLock();
            return mStamp != 0;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            mStamp = mLock.tryWriteLock(time, unit);
            return mStamp != 0;
        }

        @Override
        public void unlock() {
            mLock.unlockWrite(mStamp);
        }

        @SuppressWarnings("ConstantConditions")
        @Override
        public Condition newCondition() {
            return null;
        }
    }
}
