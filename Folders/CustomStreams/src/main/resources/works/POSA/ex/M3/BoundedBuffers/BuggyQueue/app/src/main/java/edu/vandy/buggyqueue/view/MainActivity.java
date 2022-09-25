package edu.vandy.buggyqueue.view;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import edu.vandy.buggyqueue.R;
import edu.vandy.buggyqueue.model.BuggyQueue;
import edu.vandy.buggyqueue.presenter.ConsumerTask;
import edu.vandy.buggyqueue.presenter.ProducerConsumerTaskBase;
import edu.vandy.buggyqueue.presenter.ProducerTask;
import edu.vandy.buggyqueue.utils.UiUtils;

/**
 * Main activity that shows how the BuggyQueue can be used to produce
 * and consumer integers via AsyncTasks.  Since the BuggyQueue
 * contains no synchronization this app will crash almost immediately!
 */
public class MainActivity 
       extends ActivityBase {
    /**
     * Maximum size of the queue.
     */
    private final static int sQUEUE_SIZE = 10;

    /**
     * A list of that will contain a ProducerTask and a ConsumerTask.
     */
    private List<ProducerConsumerTaskBase> mTasks;

    /**
     * Hook method called when the activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Set mTasks to the object that was stored by
        // onRetainNonConfigurationInstance().
        mTasks =
            (List<ProducerConsumerTaskBase>) getLastNonConfigurationInstance();

        // This is the first time in, so allocate mTasks.
        if (mTasks == null) 
            // Create a new ArrayList.
            mTasks = new ArrayList<>();

        // There are already computations running after a runtime
        // configuration change, so keep going.
        else if (mTasks.size() != 0) {
            // Reset widgets and the activity for each async task.
            mTasks.forEach(task 
                           -> task.onConfigurationChange(this));

            mChronometer.setVisibility(TextView.VISIBLE);
            mChronometer.start();

            // Update the start/stop FAB to display a stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);

            // Show the "startOrStop" FAB.
            UiUtils.showFab(mStartOrStopFab);
        }
    }

    /**
     * This hook method is called by Android as part of destroying an
     * activity due to a configuration change, when it is known that a
     * new instance will immediately be created for the new
     * configuration.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        // Call the super class.
        super.onRetainNonConfigurationInstance();

        // Returns mTasks so that it will be saved across runtime
        // configuration changes.
        return mTasks;
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "startOrStartComputations" button.
     *
     * @param view
     *            The view.
     */
    public void startOrStopComputations(View view) {
        if (mTasks.size() != 0)
            // The thread only exists while GCD computations are in
            // progress.
            cancelComputations();
        else 
            // Start running the computations.
            startComputations(Integer.valueOf(mCountEditText.getText().toString()));
    }

    /**
     * Start the the producer/consumer computations in the AsyncTasks.
     */
    public void startComputations(int count) {
        // Make sure there's a non-0 count.
        if (count <= 0) 
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                              "Please specify a count value that's > 0");
        else {
            // Create a new blocking bounded queue that will be shared
            // between the ProducerTask and ConsumerTask.
            BuggyQueue<Integer> buggyQueue =
                new BuggyQueue<>(sQUEUE_SIZE);

            // Create the ProducerTask and ConsumerTask.
            mTasks.add(new ProducerTask(buggyQueue,
                                        count,
                                        this));
            mTasks.add(new ConsumerTask(buggyQueue,
                                        count,
                                        this));

            // Execute both async tasks in the default thread pool
            // executor.
            mTasks.forEach(task 
                           -> task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR));

            // Initialize and start the Chronometer.
            mChronometer.setBase(SystemClock.elapsedRealtime());
            mChronometer.setVisibility(TextView.VISIBLE);
            mChronometer.start();

            // Update the start/stop FAB to display a stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
        }
    }

    /**
     * Stop the producer/consumer computations.
     */
    private void cancelComputations() {
        // Stop the async task computations.
        mTasks.forEach(task -> task.cancel(true));

        UiUtils.showToast(this,
                          "Canceling the async tasks");
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        // Create a command to reset the UI.
        Runnable command = () -> {
            // Clear out the async tasks to avoid later problems.
            mTasks.clear();

            // Reset the start/stop FAB to the play icon.
            mStartOrStopFab.setImageResource(android.R.drawable.ic_media_play);

            // Stop the chronometer.
            mChronometer.stop();
        };

        // Run the command on the UI thread.  This call is optimized
        // for the case where println() is called from the UI thread.
        runOnUiThread(command);
    }

    /**
     * Lifecycle hook method called when this activity is being
     * destroyed.
     */
    protected void onDestroy() {
        // Call the super class.
        super.onDestroy();

        // Only cancel the AsyncTasks when an activity is actually
        // being destroyed, but not when it's simply being rotated due
        // to a runtime configuration change.
        if (mTasks.size() != 0
            && !isChangingConfigurations()) {
            Log.d(TAG,
                  "cancelling async tasks");

            // Cancel the AsyncTasks since the activity is being
            // destroyed.
            mTasks.forEach(task -> task.cancel(true));
        }
    }
}
