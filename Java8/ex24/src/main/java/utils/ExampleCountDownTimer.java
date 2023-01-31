package utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

/**
 * An example {@link CountDownTimer} that displays a text string on
 * each {@code onTick()} and {@code onFinish()} call.
 */ 
public class ExampleCountDownTimer
      extends CountDownTimer {
    /**
     * Used to wait for the test to finish running.
     */
    private final CountDownLatch mCdl;

    /**
     * @param lock The {@link Lock} used to synchronize access to the
     *             object
     * @param millisInFuture The number of millis in the future from
     *                       the call to {@link #start()} until the
     *                       countdown is done and {@link #onFinish()}
     *                       is called
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks
     * @param cdl The countDownLatch used to wait until the test is
     *            done
     */
    public ExampleCountDownTimer(Lock lock,
                                 long millisInFuture,
                                 long countDownInterval,
                                 CountDownLatch cdl) {
        super(lock, millisInFuture, countDownInterval);
        mCdl = cdl;
    }

    /**
     * Callback fired on regular interval.
     *
     * @param millisUntilFinished The amount of time until finished
     */
    @Override
    public void onTick(long millisUntilFinished) {
        // Display a result to the user.
        System.out.println("seconds remaining: " + 
                           millisUntilFinished / 1000);

        // Try to cancel the timer after it's roughly halfway done.
        if ((mMillisInFuture - millisUntilFinished) 
            > millisUntilFinished) {
            // This call will trigger self-deadlock if a non-reentrant
            // lock is used.
            this.cancel();

            // Decrement the latch to release the waiter.
            mCdl.countDown();
        }
    }
 
    /**
     * Callback fired when the time is up.
     */
    @Override
    public void onFinish() {
        System.out.println("done");
    }
}
