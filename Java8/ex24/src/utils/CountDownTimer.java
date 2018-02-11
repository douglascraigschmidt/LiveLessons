package utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.StampedLock;

/**
 * Schedule a countdown until a time in the future, with regular
 * notifications on intervals along the way.
 *
 * Example of showing a 30 second countdown in a text field:
 *
 * <pre class="prettyprint">
 * new CountDownTimer(lock, 30000, 1000) {
 *
 *     public void onTick(long millisUntilFinished) {
 *         mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
 *     }
 *
 *     public void onFinish() {
 *         mTextField.setText("done!");
 *     }
 *  }.start();
 * </pre>
 *
 * The calls to {@link #onTick(long)} are synchronized to this object
 * so that one call to {@link #onTick(long)} won't ever occur before
 * the previous callback is complete.  This is only relevant when the
 * implementation of {@link #onTick(long)} takes an amount of time to
 * execute that is significant compared to the countdown interval.
 *
 * This framework implementation works fine if the {@code lock}
 * parameter has reentrant lock semantics.  It will self-deadlock,
 * however, if the {@code lock} parameter has non-reentrant lock
 * semantics.
 */
public abstract class CountDownTimer {
    /**
     * Millis since epoch when alarm should stop.
     */
    private final long mMillisInFuture;

    /**
     * The interval in millis that the user receives callbacks
     */
    private final long mCountdownInterval;

    /**
     * Executor service that executes runnables after a given timeout.
     */
    private ScheduledExecutorService mScheduledExecutorService = 
        Executors.newScheduledThreadPool(1);

    /**
     * When to stop the timer.
     */
    private long mStopTimeInFuture;
    
    /**
    * Boolean representing if the timer was cancelled.
    */
    private boolean mCancelled = false;

    /**
     * The lock used to synchronize access to the object.  If this is
     * a reentrant lock then all is well.  However, if it's a
     * non-reentrant lock the calling code can self-deadlock.
     */
    private final Lock mLock;

    /**
     * @param lock The lock used to synchronize access to the object.
     * @param millisInFuture The number of millis in the future from the call
     *   to {@link #start()} until the countdown is done and {@link #onFinish()}
     *   is called.
     * @param countDownInterval The interval along the way to receive
     *   {@link #onTick(long)} callbacks.
     */
    public CountDownTimer(Lock lock,
                          long millisInFuture,
                          long countDownInterval) {
        mLock = lock;
        mMillisInFuture = millisInFuture;
        mCountdownInterval = countDownInterval;

        // Set the policies to clean everything up on shutdown.
        ScheduledThreadPoolExecutor exec =
                (ScheduledThreadPoolExecutor) mScheduledExecutorService;
        exec.setRemoveOnCancelPolicy(true);
        exec.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        exec.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
    }

    /**
     * Cancel the countdown.
     */
    public final void cancel() {
        mLock.lock();
        try {
            mCancelled = true;

            // Shutdown the ScheduledExecutorService immediately.
            mScheduledExecutorService.shutdownNow();
        } finally {
            mLock.unlock();
        }
    }

    /**
     * Start the countdown.
     */
    public final CountDownTimer start() {
        // We haven't been canceled (yet).
        mCancelled = false;

        // Handle odd starting point.
        if (mMillisInFuture <= 0) {
            onFinish();
            return this;
        }

        // Calculate when to stop.
        mStopTimeInFuture = 
            System.currentTimeMillis() + mMillisInFuture;

        // Schedule the initial timer.
        scheduleTimer();

        // Return this object to support a fluent interface.
        return this;
    }

    /**
     * Callback fired on regular interval.
     * @param millisUntilFinished The amount of time until finished.
     */
    public abstract void onTick(long millisUntilFinished);

    /**
     * Callback fired when the time is up.
     */
    public abstract void onFinish();

    /**
     * Schedules a timer that performs the count down logic.
     */
    private void scheduleTimer() {
        // Create an object that's (re)scheduled to run periodically.
        Runnable timerHandler = new Runnable() {
                @Override
                public void run() {
                    mLock.lock();
                    try {
                        // Stop running if we've been canceled.
                        if (mCancelled) {
                            return;
                        }

                        // Determine how much time is left.
                        final long millisLeft =
                            mStopTimeInFuture - System.currentTimeMillis();

                        // If all the time has elapsed dispatch the
                        // onFinish() hook method.
                        if (millisLeft <= 0) {
                            onFinish();
                        } else {
                            long lastTickStart = System.currentTimeMillis();
                            // Dispatch the onTick() hook method.
                            onTick(millisLeft);

                            // Take into account user's onTick taking time to
                            // execute.
                            long lastTickDuration =
                                System.currentTimeMillis() - lastTickStart;
                            long delay;

                            if (millisLeft < mCountdownInterval) {
                                // Just delay until done.
                                delay = millisLeft - lastTickDuration;

                                // Special case: user's onTick took
                                // more than interval to complete,
                                // trigger onFinish without delay
                                if (delay < 0) delay = 0;
                            } else {
                                delay = mCountdownInterval - lastTickDuration;

                                // Special case: user's onTick took more than
                                // interval to complete, skip to next interval
                                while (delay < 0) delay += mCountdownInterval;
                            }

                            // Reschedule timer handler to run again
                            // at the appropriate delay in the future.
                            mScheduledExecutorService
                                .schedule(this,
                                          delay,
                                          TimeUnit.MILLISECONDS);
                        }
                    } finally {
                        // Always unlock the lock.
                        mLock.unlock();
                    }
                }
            };

        // Initially schedule the timerHandler to run immediately.
        mScheduledExecutorService
            .schedule(timerHandler,
                      0,
                      TimeUnit.MILLISECONDS);
    }
}
