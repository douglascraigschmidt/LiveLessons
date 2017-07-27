package edu.vandy.simpleblockingboundedqueue.presenter;

import android.widget.ProgressBar;
import android.widget.TextView;

import edu.vandy.simpleblockingboundedqueue.R;
import edu.vandy.simpleblockingboundedqueue.model.SimpleBlockingBoundedQueue;
import edu.vandy.simpleblockingboundedqueue.view.MainActivity;

/**
 * The producer runs in a background thread and passes integers to the
 * ConsumerTask via a shared BoundedQueue.
 */
public class ProducerTask
       extends ProducerConsumerTaskBase {
    /**
     * Constructor initializes the superclass.
     */
    public ProducerTask(SimpleBlockingBoundedQueue<Integer> blockingQueue,
                        int maxIterations,
                        MainActivity activity) {
        super("producer percentage = ",
              maxIterations,
              blockingQueue,
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
     * the ConsumerTask via a shared BoundedQueue.
     */
    @Override
    protected Void doInBackground(Void... v) {
        try {
            for (int i = 1; i <= mMaxIterations; ++i) {
                // Break out of the loop if we're cancelled.
                if (isCancelled())
                    break;

                // Call the put() method.
                mQueue.put(i);

                // Published the progress every 10%.
                if ((i % (mMaxIterations / 10)) == 0) {
                    /*
                    Log.d("Producer",
      s                    "doInBackground() on iteration "
                          + i);
                    */

                    // Convert to a percentage of 100.
                    Double percentage =
                        ((double) i / (double) mMaxIterations) * 100.00;

                    // Publish progress as a % in the UI thread.
                    publishProgress(percentage.intValue());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}

