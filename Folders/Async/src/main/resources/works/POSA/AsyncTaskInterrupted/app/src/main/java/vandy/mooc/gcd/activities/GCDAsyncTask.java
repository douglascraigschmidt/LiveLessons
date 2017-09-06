package vandy.mooc.gcd.activities;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Random;

/**
 * Computes the greatest common divisor (GCD) of two numbers, which is
 * the largest positive integer that divides two integers without a
 * remainder.  This implementation extends AsyncTask and implements
 * its hook methods, as per a white-box framework.  It also checks to
 * see if the task has been cancelled and exits gracefully if so.
 */
public class GCDAsyncTask
       extends AsyncTask<Integer, // Passed to doInBackground()
                         String,  // Passed to onProgressUpdate()
                         Boolean> { // Returned from doInBackground() and passed to onPostExecute()
    /**
     * Debugging tag used by the Android logger.
     */
    protected final String TAG =
        getClass().getSimpleName();

    /**
     * A reference to the MainActivity. 
     */
    private MainActivity mActivity;

    /**
     * Random number generator.
     */
    private final Random mRandom;

    /**
     * Keeps track of the AsyncTask number.
     */
    private int mAsyncTaskNumber;

    /**
     * Constructor initializes the fields.
     */
    GCDAsyncTask(MainActivity activity,
                 int asyncTaskNumber,
                 Random random) {
        mActivity = activity;
        mAsyncTaskNumber = asyncTaskNumber;
        mRandom = random;
    }
    
    /**
     * Sets the activity (used after a runtime configuration change).
     */
    void setActivity(MainActivity activity) {
        mActivity = activity;
    }

    /**
     * Provides a recursive implementation of Euclid's algorithm to
     * compute the "greatest common divisor" (GCD).
     */
    private int computeGCD(int number1,
                           int number2) {
        // Basis case.
        if (number2 == 0)
            return number1;
        // Recursive call.
        return computeGCD(number2,
                          number1 % number2);
    }

    /**
     * Hook method called in the UI thread before doInBackground()
     * starts.
     */
    @Override
    protected void onPreExecute() {
        // Print the message in the UI thread.
        mActivity.println("Starting new AsyncTask #"
                          + mAsyncTaskNumber);
    }

    /**
     * Hook method that runs in a background thread for MAX_ITERATIONs
     * computing the GCD of randomly generated numbers.
     */
    @Override
    public Boolean doInBackground(Integer... maxIterations) {
        // Number of times to print the results.
        int maxPrintIterations = maxIterations[0] / 10;

        // Generate random numbers and compute their GCDs.

        for (int i = 0; i < maxIterations[0]; ++i) {
            // Generate two random numbers.
            int number1 = mRandom.nextInt();
            int number2 = mRandom.nextInt();

            // Check to see if this thread has been interrupted and
            // exit gracefully if so.
            if (isCancelled()) {
                Log.d(TAG,
                      "thread "
                      + Thread.currentThread()
                      + " interrupted");
                break;
            }
            // Print results periodically.
            else if ((i % maxPrintIterations) == 0)
                // Publish this string in the UI thread.
                publishProgress("In run() with thread id "
                                + Thread.currentThread()
                                + " the GCD of "
                                + number1
                                + " and "
                                + number2
                                + " is "
                                + computeGCD(number1,
                                             number2));
        }

        // Returns true if this is the last task.
        return mActivity
            .mAsyncTaskRelatedState
            .mTaskCount
            .decrementAndGet() <= 0;
    }

    /**
     * Hook method called in the UI thread based on strings published
     * by doInBackground().
     */
    @Override
    protected void onProgressUpdate(String... message) {
        // Print the message in the UI thread.
        mActivity.println(message[0]);
    }

    /**
     * Hook method called in the UI thread after doInBackground()
     * returns successfully.
     */
    @Override
    protected void onPostExecute(Boolean lastTask) {
        // Print the message in the UI thread.
        mActivity.println("Finishing AsyncTask #"
                          + mAsyncTaskNumber
                          + " successfully");

        // Tell the activity to finish up if we're the last task.
        if (lastTask)
            mActivity.done();
    }

    /**
     * Called in the UI thread after doInBackground() returns after
     * being cancelled.
     */
    @Override
    protected void onCancelled(Boolean lastTask) {
        // Print the message in the UI thread.
        mActivity.println("Finishing AsyncTask #"
                          + mAsyncTaskNumber
                          + " after being cancelled");

        // Tell the activity to finish up if we're the last task.
        if (lastTask)
            mActivity.done();
    }
}
