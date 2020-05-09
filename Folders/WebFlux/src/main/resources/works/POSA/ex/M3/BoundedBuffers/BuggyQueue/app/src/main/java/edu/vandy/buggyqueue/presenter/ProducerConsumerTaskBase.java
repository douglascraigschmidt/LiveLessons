package edu.vandy.buggyqueue.presenter;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.vandy.buggyqueue.model.BuggyQueue;
import edu.vandy.buggyqueue.view.MainActivity;

/**
 * This class factors out code that's common to the ProducerTask and
 * ConsumerTask classes.
 */
public abstract class ProducerConsumerTaskBase
      extends AsyncTask<Void, Integer, Void> {
    /**
     * Debugging tag used by the Android logger.
     */
    private final String TAG =
        getClass().getSimpleName();

    /**
     * Maximum number of iterations.
     */
    final int mMaxIterations;

    /**
     * This queue is shared with the producer.
     */
    final BuggyQueue<Integer> mQueue;
        
    /**
     * The MainActivity for this app.
     */
    MainActivity mActivity;

    /**
     * The progress bar that's used to show how the computations are
     * proceeding.
     */
    ProgressBar mProgressBar;

    /**
     * Stores the current message to print above the progress bar.
     */
    final String mMessage;

    /**
     * Stores the current percentage in the progress bar.
     */
    int mPercentage;

    /**
     * Displays the current message and percentage above the progress
     * bar.
     */
    TextView mProgressCount;

    /**
     * Constructor initializes the fields.
     */
    ProducerConsumerTaskBase(String message, 
                             int maxIterations,
                             BuggyQueue<Integer> queue,
                             MainActivity activity,
                             ProgressBar progressBar,
                             TextView progressCount) {
        mPercentage = 0;
        mMessage = message; 
        mMaxIterations = maxIterations;
        mQueue = queue;
        mActivity = activity;
        mProgressBar = progressBar;
        mProgressCount = progressCount;
    }

    /**
     * This method is called back after every runtime configuration
     * change in the Mainactivity.
     */
    public abstract void onConfigurationChange(MainActivity activity);

    /**
     * Runs in the UI thread before doInBackground() is called.
     */
    @Override
    public void onPreExecute() {
        mProgressBar.setProgress(0);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressCount.setText(mMessage + mPercentage);
    }

    @Override
    public void onProgressUpdate(Integer... progressPercentage) {
        Log.d(TAG,
              "onProgressUpdate() with progressPercentage "
              + progressPercentage[0]);

        mProgressBar.setProgress(progressPercentage[0]);
        mPercentage = progressPercentage[0];
        mProgressCount.setText(mMessage + mPercentage);
    }
}
