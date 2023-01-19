package vandy.mooc.threadconfig.activities;

import android.app.Activity;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/*
  * Displays values from an initial count down to 0 using a thread
  * that waits 1.0 seconds between displaying the values.
  */
public class CountdownDisplay
       extends Thread {
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /**
     * Initial value to start the the count down.
     */
    private final int mInitialCount = 10;

    /**
     * Handler used to print output to the display.
     */
    private Handler mHandler;

    /**
     * Store the activity.
     */
    private Activity mActivity;

    /**
     * Current count.
     */
    private volatile int mCount = mInitialCount;

    /** 
     * A colorful TextView that prints mCount to the display.
     */
    private TextView mColorOutput;

    /**
     * Constructor initializes the fields.
     */
    CountdownDisplay(Activity activity) {
        // Create a new handler that will have an affinity with the UI
        // thread.
        mHandler = new Handler();
        
        // Store the activity for later use.
        mActivity = activity;
    }

    /**
     * Get the current count.
     */
    public int getCount() {
        return mCount;
    }

    /**
     * Set the current count.
     */
    public void setCount(int count) {
        mCount = count;
    }

    /**
     * Set the output textview.
     */
    public void setOutput(TextView output) {
        mColorOutput = output;
    }

    /**
     * Hook method that runs the main loop, which prints the current
     * value of count to the display and waits 1 second.
     */
    @Override
    public void run() {
        // Keep looping until mCount reaches 0.
        while (mCount > 0) {
            // Break out of the loop if this thread has been
            // interrupted.
            if (Thread.interrupted()) {
                Log.d(TAG, "thread interrupted: shutting down");
                break;
            }

            // Print count to the display.
            print(mCount--);

            // Wait 1 second before handling the next message.
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d(TAG, "sleep interrupted: thread shutting down");
                return;
            }
        }

        // Finish the activity.
        mActivity.finish();
    }

    /**
     * Prints @a count to the display.
     */
    public void print(int count) {
        // Post a runnable whose run() method instructs the UI to
        // print the count to the display.
        mHandler.post(() -> {
                if ((count % 2) == 0) {
                    mColorOutput.setBackgroundColor(Color.WHITE);
                    mColorOutput.setTextColor(Color.BLACK);
                    mColorOutput.setText(String.valueOf(count));
                }
                else {
                    mColorOutput.setBackgroundColor(Color.BLACK);
                    mColorOutput.setTextColor(Color.WHITE);
                    mColorOutput.setText(String.valueOf(count));
                }
            });
    }
}
