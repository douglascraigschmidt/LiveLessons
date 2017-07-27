package vandy.mooc.prime.activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Random;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import vandy.mooc.prime.R;
import vandy.mooc.prime.utils.PrimeCheckers;
import vandy.mooc.prime.utils.TimedMemoizer;
import vandy.mooc.prime.utils.UiUtils;

/**
 * Main activity for an app that shows how to use the Java
 * ExecutorCompletionService interface and a fixed-size thread pool to
 * determine if n random numbers are prime or not.  The user can
 * interrupt the thread performing this computation at any point and
 * the thread will also be interrupted when the activity is destroyed.
 * In addition, runtime configuration changes are handled gracefully.
 */
public class MainActivity 
       extends LifecycleLoggingActivity {
    /**
     * Number of times to iterate if the user doesn't specify
     * otherwise.
     */
    private final static int sDEFAULT_COUNT = 50;

    /**
     * An EditText field uesd to enter the desired number of iterations.
     */
    private EditText mCountEditText;

    /**
     * Keeps track of whether the edit text is visible for the user to
     * enter a count.
     */
    private boolean mIsEditTextVisible = false;

    /**
     * Reference to the "set" floating action button.
     */
    private FloatingActionButton mSetFab;

    /**
     * Reference to the "start or stop" floating action button.
     */
    private FloatingActionButton mStartOrStopFab;

    /** 
     * A TextView used to display the output.
     */
    private TextView mTextViewLog;

    /** 
     * A ScrollView that contains the results of the TextView.
     */
    private ScrollView mScrollView;

    /**
     * Keeps track of whether the app is running or not.
     */
    private boolean mIsRunning;

    /**
     * State that must be preserved across runtime configuration
     * changes.
     */
    private static class RetainedState {
        /**
         * Debugging tag used by the Android logger.
         */
        private final String TAG =
                getClass().getSimpleName();

        /**
         * This object runs the prime computations.
         */
        final ExecutorCompletionService<PrimeCallable.PrimeResult> mExecutorCompletionService;

        /**
         * This object manages a thread pool.
         */
        ExecutorService mExecutorService;

        /**
         * This runnable executes in a background thread to get the
         * results of the futures.
         */
        CompletionRunnable mCompletionRunnable;

        /**
         * Thread that waits for all the results to complete.
         */
        Thread mThread;
        
        /**
         * Cache used to generate, store, and retrieve the results of
         * prime checking computations.
         */
        TimedMemoizer<Long, Long> mTimedMemoizer;

        /**
         * Constructor initializes the state that's retained across
         * runtime configuration changes.
         */
        RetainedState() {
            // Create a thread pool that matches the number of cores.
            mExecutorService =
                Executors.newFixedThreadPool(Runtime.getRuntime()
                                             .availableProcessors());

            // Associate the ExecutorCompletionService with the
            // ExecutorService.
            mExecutorCompletionService =
                new ExecutorCompletionService<>
                (mExecutorService);
        }

        /**
         * Shutdown the retained state.
         */
        void shutdown() {
            Log.d(TAG,
                  "The retained state is being shutdown");

            // Shutdown the ExecutorService.
            if (mExecutorService != null) {
                mExecutorService.shutdownNow();
                mExecutorService = null;
            }

            // Shutdown the memoizer if it already exists.
            if (mTimedMemoizer != null) {
                mTimedMemoizer.shutdown();
                mTimedMemoizer = null;
            }

            // Interrupt the prime waiter thread.
            if (mThread != null) {
                mThread.interrupt();
                mThread = null;
            }

            // Help the GC.
            mCompletionRunnable = null;
        }
    }

    /**
     * Store all the state that must be preserved across runtime
     * configuration changes.
     */
    private RetainedState mRetainedState;

    /**
     * Hook method called when the activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file.
        setContentView(R.layout.main_activity);

        // Initialize the views.
        initializeViews();

        // Set mRetainedState to the object that was stored by
        // onRetainNonConfigurationInstance().
        mRetainedState =
            (RetainedState) getLastNonConfigurationInstance();

        if (mRetainedState != null) {
            mRetainedState.mCompletionRunnable.setActivity(this);

            // Update the start/stop FAB to display a stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);

            // Show the "startOrStop" FAB.
            UiUtils.showFab(mStartOrStopFab);
        } else 
            // Allocate the state that's retained across runtime
            // configuration changes.
            mRetainedState = new RetainedState();
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

        // Returns mRetainedState so that it will be saved across
        // runtime configuration changes.
        return mRetainedState;
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        // Set the EditText that holds the count entered by the user
        // (if any).
        mCountEditText = (EditText) findViewById(R.id.count);

        // Cache floating action button that sets the count.
        mSetFab = (FloatingActionButton) findViewById(R.id.set_fab);

        // Cache floating action button that starts playing ping/pong.
        mStartOrStopFab = (FloatingActionButton) findViewById(R.id.play_fab);

        // Make the EditText invisible for animation purposes.
        mCountEditText.setVisibility(View.INVISIBLE);

        // Make the count button invisible for animation purposes.
        mStartOrStopFab.setVisibility(View.INVISIBLE);

        // Store and initialize the TextView and ScrollView.
        mTextViewLog =
            (TextView) findViewById(R.id.text_output);
        mScrollView =
            (ScrollView) findViewById(R.id.scrollview_text_output);

        // Register a listener to help display "start playing" FAB
        // when the user hits enter.  This listener also sets a
        // default count value if the user enters no value.
        mCountEditText.setOnEditorActionListener
            ((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    UiUtils.hideKeyboard(MainActivity.this,
                                         mCountEditText.getWindowToken());
                    if (TextUtils.isEmpty
                        (mCountEditText.getText().toString().trim())) 
                        mCountEditText.setText(String.valueOf(sDEFAULT_COUNT));

                    UiUtils.showFab(mStartOrStopFab);
                    return true;
                } else {
                    return false;
                }
            });
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the '+' floating action button.
     *
     * @param view The view
     */
    public void setCount(View view) {
        // Check whether the EditText is visible to determine
        // the kind of animations to use.
        if (mIsEditTextVisible) {
            // Hide the EditText using circular reveal animation
            // and set boolean to false.
            UiUtils.hideEditText(mCountEditText);
            mIsEditTextVisible = false;

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mSetFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Hides the FAB.
            UiUtils.hideFab(mStartOrStopFab);
        } else {
            // Reveal the EditText using circular reveal animation and
            // set boolean to true.
            UiUtils.revealEditText(mCountEditText);
            mIsEditTextVisible = true;
            mCountEditText.requestFocus();

            // Rotate the FAB from '+' to 'X'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mSetFab.startAnimation(AnimationUtils.loadAnimation(this,
                                                                animRedId));
        }
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "startOrStartComputations" button.
     *
     * @param view
     *            The view.
     */
    public void startOrStopComputations(View view) {
        if (mIsRunning)
            // The ExecutorService only exists while prime
            // computations are in progress.
            interruptComputations();
        else 
            // Get the count from the edit view.
            startComputations(Integer.valueOf(mCountEditText.getText().toString()));
    }

    /**
     * Start the prime computations.
     */
    private void startComputations(int count) {
        // Make sure there's a non-0 count.
        if (count <= 0) 
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                              "Please specify a count value that's > 0");
        else {
            mIsRunning = true;

            // Shutdown the memoizer if it already exists.
            if (mRetainedState.mTimedMemoizer != null) 
                mRetainedState.mTimedMemoizer.shutdown();

            // Create the cache used to generate, store, and retrieve
            // the results of prime checking computations.
            mRetainedState.mTimedMemoizer =
                new TimedMemoizer<>(PrimeCheckers::bruteForceChecker,
                                    // Timeout cache entries after count *
                                    // 0.5 seconds.
                                    count * 500);

            // Submit "count" PrimeCallable objects that concurrently check
            // the primality of "count" random numbers.
            new Random()
                // Generate "count" random between (MAX_VALUE - count)
                // and MAX_VALUE.
                .longs(count, Integer.MAX_VALUE - count, Integer.MAX_VALUE)

                // Convert each random number into a PrimeCallable.
                .mapToObj(randomNumber 
                          -> new PrimeCallable(randomNumber,
                                               mRetainedState.mTimedMemoizer))

                // Submit each PrimeCallable to the ExecutorService.
                .forEach(mRetainedState.mExecutorCompletionService::submit);

            // Store the CompletionRunnable in a field so it can be
            // updated during a runtime configuration change.
            mRetainedState.mCompletionRunnable = new CompletionRunnable(this,
                                                                        count);

            // Create/start a thread that waits for all the results in
            // the background so it doesn't block the UI thread.
            mRetainedState.mThread = new Thread(mRetainedState.mCompletionRunnable);
            mRetainedState.mThread.start();
        }

        println("Starting primality computations");

        // Update the start/stop FAB to display a stop icon.
        mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
    }

    /**
     * The class runs in a background thread in the ExecutorService
     * and gets the results of all the completed futures.
     */
    static private class CompletionRunnable 
                   implements Runnable {
        /**
         * Debugging tag used by the Android logger.
         */
        final String TAG =
                getClass().getSimpleName();

        /**
         * Count of the number of prime checker computations.
         */
        final int mCount;

        /**
         * Reference back to the enclosing activity.
         */
        MainActivity mActivity;

        /**
         * Constructor initializes the field.
         */
        CompletionRunnable(MainActivity activity,
                                  int count) {
            mActivity = activity;
            mCount = count;
        }

        /**
         * Reset the activity after a runtime configuration change.
         */
        void setActivity(MainActivity activity) {
            mActivity = activity;
        }

        /**
         * Run in a background thread to get the results of all the
         * completed futures.
         */
        @Override
        public void run() {
            // Iterate through all the futures to get the results.
            for (int i = 0; i < mCount; ++i) {
                try {
                    // This call will block until the future is triggered.
                    Future<PrimeCallable.PrimeResult> resultFuture = 
                        mActivity.mRetainedState.mExecutorCompletionService.take();

                    // The get() call will not block since the results
                    // should be ready before they are added to the
                    // completion queue.
                    PrimeCallable.PrimeResult result = resultFuture.get();

                    // Check the results and display the appropriate message.
                    if (result.mSmallestFactor != 0)
                        mActivity.println(""
                                          + result.mPrimeCandidate
                                          + " is not prime with smallest factor "
                                          + result.mSmallestFactor);
                    else
                        mActivity.println(""
                                          + result.mPrimeCandidate
                                          + " is prime");
                } catch (InterruptedException e) {
                    Log.d(TAG,
                          "Prime waiter thread interrupted "
                          + Thread.currentThread());
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // Finish up and reset the UI.
            mActivity.done();
        }
    }

    /**
     * Stop the prime computations.
     */
    private void interruptComputations() {
        // Inform user an interrupt occurred.
        UiUtils.showToast(this,
                          "Interrupting the computations");

        // Shutdown the retained state.
        mRetainedState.shutdown();

        // Trigger a reset of the retained state on cancellation.
        mRetainedState = new RetainedState();

        // Finish up and reset the UI.
        done();
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        // Create a command to reset the UI.
        Runnable command = () -> {
            // Indicate the app is not longer running.
            mIsRunning = false;

            // Append the stringToPrint and terminate it with a
            // newline.
            mTextViewLog.append("Finished primality computations\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            // Reset the start/stop FAB to the play icon.
            mStartOrStopFab.setImageResource(android.R.drawable.ic_media_play);
        };

        // Run the command on the UI thread, which optimizes for the
        // case where println() is called from the UI thread.
        runOnUiThread(command);
    }

    /**
     * Append @a stringToPrint to the scrolling text view.
     */
    public void println(String stringToPrint) {
        // Create a command to print the results.
        Runnable command = () -> {
            // Append the stringToPrint and terminate it with a
            // newline.
            mTextViewLog.append(stringToPrint + "\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        };

        // Run the command on the UI thread, which internally
        // optimizes for the case where println() is called from the
        // UI thread.
        runOnUiThread(command);
    }

    /**
     * Lifecycle hook method called when this activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mRetainedState != null
            && !isChangingConfigurations()) {
            // Shutdown the retained state since the activity is being
            // destroyed.
            mRetainedState.shutdown();
        }
    }
}
