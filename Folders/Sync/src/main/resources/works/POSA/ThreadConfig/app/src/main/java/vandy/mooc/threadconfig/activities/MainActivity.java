package vandy.mooc.threadconfig.activities;

import android.os.Bundle;
import android.view.View;

import vandy.mooc.threadconfig.R;

/**
 * This activity is a "facade" whose methods launch other activities
 * to that show various ways to handle the state associated with a
 * thread when an activity does a runtime configuration change.
 */
public class MainActivity 
       extends LifecycleLoggingActivity {
    /**
     * Hook method called when the activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file.
        setContentView(R.layout.main_activity);
    }

    /**
     * Method called back when the "buggy config" button is pressed.
     * It does nothing and doesn't handle the runtime configuration
     * change properly.
     */
    public void buggy(View v) {
        // Call factory method to get an intent and launch the
        // designated activity.
        startActivity(BuggyActivity.makeIntent(this));
    }

    /**
     * Method called back when the "save/restore" button is pressed.
     * It handles runtime configuration changes by using
     * onSaveInstanceState() and onRestoreInstanceState().
     */
    public void saveRestore(View v) {
        // Call factory method to get an intent and launch the
        // designated activity.
        startActivity(SaveRestoreActivity.makeIntent(this));
    }

    /**
     * Method called back when the "non config" button is pressed.  It
     * handles runtime configuration changes by using
     * onRetainNonConfigurationInstance() and
     * getLastNonConfigurationInstance().
     */
    public void nonConfig(View v) {
        // Call factory method to get an intent and launch the
        // designated activity.
        startActivity(NonConfigActivity.makeIntent(this));
    }

    /**
     * Method called back when the "retained frag" button is pressed.
     * It handles runtime configuration changes by the
     * RetainedFragmentManager framework.
     */
    public void retainedFrag(View v) {
        // Call factory method to get an intent and launch the
        // designated activity.
        startActivity(RetainedFragmentActivity.makeIntent(this));
    }
}
