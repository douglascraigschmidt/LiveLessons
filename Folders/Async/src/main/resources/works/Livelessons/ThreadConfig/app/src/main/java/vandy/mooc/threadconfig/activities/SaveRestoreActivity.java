package vandy.mooc.threadconfig.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import vandy.mooc.threadconfig.R;
import vandy.mooc.threadconfig.utils.UiUtils;

/**
 * An activity that shows how to handle runtime configuration changes
 * by using onSaveInstanceState() and onRestoreInstanceState().
 */
public class SaveRestoreActivity
       extends LifecycleLoggingActivity {
    /**
     * Key used to store the value of count across runtime
     * configuration changes.
     */
    final String COUNT = "count";

    /**
     * A thread that delays output by 1 second to make it easier to
     * visualize on the user's display.
     */
    protected CountdownDisplay mThread;

    /**
     * Factory method that creates an intent that will launch this
     * activity.
     */ 
    public static Intent makeIntent(Context context) {
        return new Intent(context, SaveRestoreActivity.class);
    }

    /**
     * Hook method called when the Activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file.
        setContentView(R.layout.thread_activity);
    }

    /**
     * Lifecycle hook method that's called when an activity is about
     * to gain focus.
     */
    protected void onResume() {
        // Call the super class.
        super.onResume();

        // Start the new CountdownDisplay thread.
        mThread.start();

        UiUtils.showToast(this,
                          "starting a new thread");
    }

    /**
     * Lifecycle hook method that's called when an activity is about
     * to become visible.
     */
    protected void onStart() {
        // Call the super class.
        super.onStart();

        // Create a new CountdownDisplay if it's not currently
        // initialized.
        if (mThread == null) {
            // Create a new CountdownDisplay thread.
            mThread = new CountdownDisplay(this);
        } else
            // This should never happen in this implementation.
            UiUtils.showToast(this,
                              "continuing to run the thread");

        // Set the output TextView.
        mThread.setOutput((TextView) findViewById(R.id.color_output));
    }

    /**
     * Called to retrieve per-instance state from an activity before
     * being killed so that the state can be restored in
     * onRestoreInstanceState().
     */
    protected void onSaveInstanceState(Bundle outState) {
        // Call the super class.
        super.onSaveInstanceState(outState);

        // Interrupt the thread so it doesn't change its current count
        // after we store it below.
        mThread.interrupt();

        try {
            // Wait for the thread to finish.
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Store the current count so that it will be available when
        // the runtime configuration change is done.
        outState.putInt(COUNT,
                        mThread.getCount());
    }

    /**
     * This method is called after onStart() when the activity is
     * being re-initialized from a previously saved state, given here
     * in savedInstanceState.
     */
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Call the super class.
        super.onRestoreInstanceState(savedInstanceState);

        // Set the count of the thread to the save value.
        mThread.setCount(savedInstanceState.getInt(COUNT));
    }
}
