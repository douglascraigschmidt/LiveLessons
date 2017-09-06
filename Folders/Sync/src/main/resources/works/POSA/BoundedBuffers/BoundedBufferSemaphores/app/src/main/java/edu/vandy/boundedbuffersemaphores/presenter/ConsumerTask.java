package edu.vandy.simpleblockingboundedqueue.presenter;

import android.widget.ProgressBar;
import android.widget.TextView;

import edu.vandy.simpleblockingboundedqueue.R;
import edu.vandy.simpleblockingboundedqueue.model.SimpleBlockingBoundedQueue;
import edu.vandy.simpleblockingboundedqueue.view.MainActivity;

/**
 * The consumer runs in a background thread and receives integers from
 * the ProducerTask via a shared BoundedQueue.
 */
public class ConsumerTask
      extends ProducerConsumerTaskBase {
    /**
     * Constructor initializes the superclass.
     */
    public ConsumerTask(SimpleBlockingBoundedQueue<Integer> blockingQueue,
                        int maxIterations,
                        MainActivity activity) {
        super("consumer percentage = ",
              maxIterations,
              blockingQueue,
              activity,
              (ProgressBar) activity.findViewById(R.id.progressConsumerBar),
              (TextView) activity.findViewById(R.id.progressConsumerCount));
    }

    /**
     * This method is called back after every runtime configuration
     * change in the Mainactivity.
     */
    public void onConfigurationChange(MainActivity activity) {
        mActivity = activity;
        mProgressBar = (ProgressBar) activity.findViewById(R.id.progressConsumerBar);
        mProgressCount = (TextView) activity.findViewById(R.id.progressConsumerCount);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mProgressCount.setText(mMessage + mPercentage);
    }
    
    /**
     * This method runs in a background thread and receives integers
     * sent by the ProducerTask via a shared BoundedQueue.
     */
    @Override
    protected Void doInBackground(Void... v) {
        try {
            for (int i = 1; i <= mMaxIterations; ++i) {
                // Break out of the loop if we're cancelled.
                if (isCancelled())
                    break;

                // Calls the take() method to get the next integer.
                Integer integer = mQueue.take();
                        
                // Publish the progress every 10%.
                if ((i % (mMaxIterations / 10)) == 0) {
                    /*
                    Log.d("Consumer",
                          "doInBackground() on iteration "
                          + i);
                    */

                    // Convert to a percentage of 100.
                    Double percentage =
                        ((double) integer / (double) mMaxIterations) * 100.00;

                    // Publish progress as a % in the UI thread.
                    publishProgress(percentage.intValue());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Runs in the UI thread after doInBackground() finishes running
     * successfully.
     */
    @Override
    public void onPostExecute(Void v) {
        // Indicate to the activity that we're done.
        mActivity.done();
    }

    /**
     * Runs in the UI thread after doInBackground() is cancelled. 
     */
    @Override
    public void onCancelled(Void v) {
        // Just forward to onPostExecute();
        onPostExecute(v);
    }
}

