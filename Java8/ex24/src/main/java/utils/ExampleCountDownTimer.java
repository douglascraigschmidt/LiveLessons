package utils;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;

public class ExampleCountDownTimer
      extends CountDownTimer {
    /**
     * Used to wait for the test to finish running.
     */
    private final CountDownLatch mCdl;

    public ExampleCountDownTimer(Lock lock,
                                 long millisInFuture,
                                 long countDownInterval,
                                 CountDownLatch cdl) {
        super(lock, millisInFuture, countDownInterval);
        mCdl = cdl;
    }

    /**
     * Callback fired on regular interval.

     * @param millisUntilFinished The amount of time until finished
     */
    @Override
    public void onTick(long millisUntilFinished) {
        System.out.println("seconds remaining: " + 
                           millisUntilFinished / 1000);
        // Try to cancel the timer after it's about halfway
        // done.
        if ((mMillisInFuture - millisUntilFinished) > millisUntilFinished) {
            // This call will trigger self-deadlock if a
            // non-reentrant lock is used.
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
