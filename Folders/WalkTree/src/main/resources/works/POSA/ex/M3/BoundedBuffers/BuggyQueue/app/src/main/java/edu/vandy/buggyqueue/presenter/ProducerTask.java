package edu.vandy.buggyqueue.presenter;

import android.widget.ProgressBar;
import android.widget.TextView;

import edu.vandy.buggyqueue.R;
import edu.vandy.buggyqueue.model.BuggyQueue;
import edu.vandy.buggyqueue.view.MainActivity;

/**
 * The producer runs in a background thread and passes integers to the
 * ConsumerTask via a shared SimpleBlockingQueue.
 */
public class ProducerTask
       extends ProducerConsumerTaskBase {
    /**
     * Constructor initializes the superclass.
     */
    public ProducerTask(BuggyQueue<Integer> queue,
                        int maxIterations,
                        MainActivity activity) {
        super("producer percentage = ",
              maxIterations,
              queue,
              activity,
              (ProgressBar) activity.findViewById(R.id.progressProducerBar),
              (TextView) activity.findViewById(R.id.progressProducerCount));
    }

    /**
     * This method is called back after every runtime configuration
     * change in the Mainactivity.
     */
    public void onConfigurationChange(MainActivity activity) {
        mActivity = activity;
        mProgressBar = (ProgressBar) activity.findViewById(R.id.progressProducerBar);
        mProgressCount = (TextView) activity.findViewById(R.id.progressProducerCount);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressCount.setText(mMessage + mPercentage);
    }

    /**
     * This method runs in a background thread and passes integers to
     * the ConsumerTask via a shared SimpleBlockingQueue.
     */
    @Override
    protected Void doInBackground(Void... v) {
        // Calls the offer() method to get the next integer.  This
        // call returns false if there are no integers available.
        for (int i = 1; i <= mMaxIterations; )
            // Break out of the loop if we're cancelled.
            if (isCancelled())
                break;

        // Try to queue a new integer (this call returns false if
        // the queue is full).
            else if (mQueue.offer(i)) {
                // Publish the progress every 10%.
                if ((i % (mMaxIterations / 10)) == 0) {
                    /*
                      Log.d(TAG,
                      "doInBackground() on iteration "
                      + i);
                    */

                    // Convert to a percentage of 100.
                    Double percentage =
                        ((double) i / (double) mMaxIterations) * 100.00;

                    // Publish progress as a % in the UI thread.
                    publishProgress(percentage.intValue());
                }

                // Advance the count by 1.
                ++i;
            }

        return null;
    }
}

