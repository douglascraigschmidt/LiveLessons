package vandy.mooc.threadconfig.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import vandy.mooc.threadconfig.R;
import vandy.mooc.threadconfig.utils.UiUtils;

/**
 * An activity that intentionally doesn't handle runtime configuration
 * changes properly.
 */
public class BuggyActivity
       extends LifecycleLoggingActivity {
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
        return new Intent(context, BuggyActivity.class);
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
     * to become visible.
     */
    protected void onStart() {
        super.onStart();

        // Create a new CountdownDisplay if it's not currently
        // initialized.
        if (mThread == null) {
            // Create and start a new CountdownDisplay thread.
            mThread = new CountdownDisplay(this);
            mThread.start();
            UiUtils.showToast(this,
                              "starting a new thread");
        } else
            // This should never happen in this implementation.
            UiUtils.showToast(this,
                              "continuing to run the thread");

        // Set the output TextView.
        mThread.setOutput((TextView) findViewById(R.id.color_output));
    }

    /**
     * This lifecycle hook method is called when the activity is about
     * to be destroyed.
     */
    protected void onDestroy() {
        // Call the super class.
        super.onDestroy();

        // Interrupt the thread.
        mThread.interrupt();

        try {
            // Wait for the thread to finish.
            mThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

