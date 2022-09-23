package edu.vandy.gcdtesttask.presenter;

import android.os.SystemClock;
import android.util.Log;

import edu.vandy.visfwk.model.TaskTuple;
import edu.vandy.visfwk.model.abstracts.AbstractTestTask;
import edu.vandy.visfwk.model.interfaces.ModelStateInterface;
import edu.vandy.visfwk.presenter.interfaces.PresenterInterface;
import edu.vandy.visfwk.utils.ProgressReporter;
import edu.vandy.visfwk.view.interfaces.ViewInterface;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.stream.Collectors.toList;

/**
 * This class tests various GCDInterface implementations using CountDownLatchs
 * on Android.
 */
public class GCDCountDownLatchTestTask
	extends AbstractTestTask<GCDInterface>
	implements ProgressReporter {
    /**
     * String Tag for logging.
     */
    private final static String TAG =
        GCDCountDownLatchTestTask.class.getCanonicalName();

    /**
     * The executor to run this AsyncTask and Runnable(s) on.
     */
    private ExecutorService mExecutor;

    /**
     * This list of AndroidGCDCountDownLatchTesters keeps track of the
     * objects to update after a runtime configuration change.
     */
    private List<GCDCountDownLatchTesterAndroidAdapter> mGcdTesters;

    /**
     * Number of iterations to run the GCDInterface tests.
     */
    private int mIterations;

    /**
     * This entry barrier ensures the threads don't start until the
     * coordinator thread lets them begin.
     */
    private CountDownLatch mEntryBarrier;

    /**
     * This exit barrier ensures the coordinator thread doesn't
     * complete until all the test threads complete.
     */
    private CountDownLatch mExitBarrier;

    /**
     * Constructor initializes the fields.
     */
    GCDCountDownLatchTestTask(ViewInterface<GCDInterface> viewInterface,
                              ModelStateInterface<GCDInterface> modelStateInterface,
                              PresenterInterface presenterInterface,
                              int iterations) {
        super(viewInterface,
              modelStateInterface,
              presenterInterface);

        // Set the number of times to run the tests.
        mIterations = iterations;

        // Create a new cached thread pool executor.
        mExecutor = Executors.newCachedThreadPool();
    }

    /**
     * Hook method called in the UI thread prior to execution of the
     * asynctask.
     */
    protected void onPreExecute() {
        // This list of GCDTuples keeps track of the data needed to
        // run each GCDInterface implementation.
        List<TaskTuple<GCDInterface>> gcdTaskTuples =
            mModelStateInterface.getTestTasks();

        Log.d(TAG,
              "onPreExecute()");

        // Create an entry barrier that ensures the threads don't
        // start until this thread lets them begin.
        mEntryBarrier = new CountDownLatch(1);

        // Create an exit barrier that ensures this thread doesn't
        // complete until all the test threads complete.
        mExitBarrier = new CountDownLatch(gcdTaskTuples.size());

        // Create a list of GCDInterface testers.
        mGcdTesters = gcdTaskTuples
            // Covert the GCDInterface tuples into a stream.
            .stream()

            // Map each GCDInterface tuple into a GCDInterface tester.
            .map(gcdTaskTuple ->
                 new GCDCountDownLatchTesterAndroidAdapter
                 // All test tasks share the entry and exit barriers.
                 (mViewInterface,
                  gcdTaskTuple.getTaskUniqueId(),
                  mEntryBarrier,
                  mExitBarrier,
                  gcdTaskTuple,
                  this))

            // Collect into a list.
            .collect(toList());
    }

    /**
     * Runs in a background thread to initiate all the GCDInterface tests and
     * wait for them to complete.
     */
    @Override
    protected Void doInBackground(Integer... cycles) {
        Log.d(TAG,
              "doInBackground()" + cycles[0]);

        // Iterate for each cycle.
        for (int cycle = 1;
             cycle <= cycles[0];
             cycle++) {
            try {
                // Initialize the input arrays.
                GCDCountDownLatchWorker.initializeInputs(mIterations);

                // Execute each GCDInterface tester in the ExecutorService.
                mGcdTesters.forEach(mExecutor::execute);
                
                try {
                    // Create a runnable on the UI thread to
                    // initialize the chronometer.
                    updateProgress(() -> {
                            // Log.d(TAG,
                            //       "publish progress from inside doInBackground.");

                            // Initialize and start the Chronometer.
                            mViewInterface.chronometerStop();
                            mViewInterface.chronometerSetBase(SystemClock.elapsedRealtime());
                            mViewInterface.chronometerSetVisibility(true);
                            mViewInterface.chronometerStart();
                        });

                    System.out.println("Starting GCDInterface tests for cycle "
                                       + cycle);

                    // Wait until all the worker threads are ready to run.
                    mEntryBarrier.countDown();
                    System.out.println("Waiting for results from cycle "
                                       + cycle);

                    // Wait until all the worker threads are finished
                    // running.
                    mExitBarrier.await();
                    System.out.println("All threads are done for cycle "
                                       + cycle);
                } catch (Exception ex) {
                    System.out.println("cancelling doInBackground() due to exception"
                                       + ex);

                    // Cancel ourselves so the onCancelled() hook
                    // method gets called.
                    cancel(true);
                    return null;
                }
            } catch (Exception ex) {
                Log.d(TAG,
                      "Exception: " + ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Runs in the UI thread after doInBackground() finishes running
     * successfully.
     */
    @Override
    public void onPostExecute(Void v) {
        Runnable command = () -> {
            // Stop the chronometer.
            mViewInterface.getChronometer()
            .stop();

            Log.d(TAG,
                  "onPostExecute()");
        };

        // Run the command on the UI thread.  This call is optimized
        // for the case where println() is called from the UI thread.
        mViewInterface.getFragmentActivity()
            .runOnUiThread(command);

        // Call to the super class.
        super.onPostExecute(v);
    }

    /**
     * Runs in the UI thread after doInBackground() is cancelled.
     */
    @Override
    public void onCancelled(Void v) {
        System.out.println("in onCancelled()");

        // Shutdown all the threads in the polls.
        mExecutor.shutdownNow();

        // Call to the super class.
        super.onCancelled(v);
    }

    /**
     * Report progress to the UI thread.
     */
    public void updateProgress(Runnable runnable) {
        // Publish the runnable on the UI thread.
        publishProgress(runnable);
    }

    /**
     * Return the ExecutorService.
     */
    public Executor getExecutor() {
        return mExecutor;
    }
}
