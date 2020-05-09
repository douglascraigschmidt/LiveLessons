package vandy.mooc.threadconfig.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import vandy.mooc.threadconfig.R;
import vandy.mooc.threadconfig.utils.RetainedFragmentManager;
import vandy.mooc.threadconfig.utils.UiUtils;

/**
 * An activity that handles runtime configuration changes by using the
 * RetainedFragmentManager framework.
 */
public class RetainedFragmentActivity
       extends LifecycleLoggingActivity {
    /**
     * A thread that delays output by 1 second to make it easier to
     * visualize on the user's display.
     */
    protected CountdownDisplay mThread;

    /**
     * Retain the state of this activity across runtime configuration
     * changes.
     */
    RetainedFragmentManager mRetainedFragmentManager
        = new RetainedFragmentManager(this.getFragmentManager(),
                                      TAG);

    /**
     * Factory method that creates an intent that will launch this
     * activity.
     */ 
    public static Intent makeIntent(Context context) {
        return new Intent(context, RetainedFragmentActivity.class);
    }

    /**
     * Hook method called when the activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file.
        setContentView(R.layout.thread_activity);

        // This method returns true if it's the first time the
        // activity has been launched.
        if (mRetainedFragmentManager.firstTimeIn()) {
            Log.d(TAG,
                  "First time calling onCreate()");

            // Create and start a new CountdownDisplay thread.
            mThread = new CountdownDisplay(this);
            mThread.start();

            // Store the CountdownDisplay thread in the
            // RetainedFragmentManager.
            mRetainedFragmentManager.put("THREAD",
                                         mThread);
            UiUtils.showToast(this,
                              "starting a new thread");
        } 
        // This branch is run if it's the second (or subsequent) time
        // an activity is recreated after a runtime configuration
        // change.
        else {
            UiUtils.showToast(this,
                              "continuing to run the thread");
            // Get the CountdownDisplay thread from the
            // RetainedFragmentManager.
            mThread = mRetainedFragmentManager.get("THREAD");
        }

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

        // If this activity is going away permanently then interrupt
        // the thread.
        if (!isChangingConfigurations())
            // Interrupt the thread.
            mThread.interrupt();
    }
}

