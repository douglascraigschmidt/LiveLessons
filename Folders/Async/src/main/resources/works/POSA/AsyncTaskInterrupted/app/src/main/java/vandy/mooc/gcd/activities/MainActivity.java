package vandy.mooc.gcd.activities;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import vandy.mooc.gcd.R;
import vandy.mooc.gcd.utils.UiUtils;

/**
 * Main activity for an app that shows how to start and cancel a pool
 * of Android AsyncTasks that computes the greatest common divisor
 * (GCD) of two numbers, which is the largest positive integer that
 * divides two integers without a remainder.  The user can cancel the
 * AsyncTasks performing these computations at any point and they will
 * also be canceled when the activity is destroyed.  In addition,
 * runtime configuration changes are handled gracefully, without
 * restarting the computations from the beginning.
 */
public class MainActivity 
       extends LifecycleLoggingActivity {
   /**
     * EditText field for entering the desired number of iterations.
     */
    private EditText mCountEditText;

    /**
     * Number of times to iterate if the user doesn't specify
     * otherwise.
     */
    private final static int sDEFAULT_COUNT = 100000000;

    /**
     * Number of threads to put in the ThreadPoolExecutor.
     */
    private final static int sMAX_TASK_COUNT = 2;

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
     * State's related to an AsyncTask that must be preserved across
     * runtime configuration changes.
     */
    class AsyncTaskRelatedState {
        /**
         * Reference to the ExecutorService that runs the GCD
         * computations.
         */
        ExecutorService mExecutor;

        /**
         * Keep track of the number of AsyncTasks.
         */
        AtomicInteger mTaskCount;

        /**
         * A ThreadFactory that creates an appropriately named thread
         * to handle each request.
         */
        ThreadFactory mThreadFactory;

        /**
         * The list of GCDAsyncTasks to execute.
         */
        List<GCDAsyncTask> mTaskList;

        /**
         * Constructor initializes the fields.
         */
        AsyncTaskRelatedState() {
            // Create the GCDAsyncTask.
            mTaskList = new ArrayList<>();

            // Initialize the task count.
            mTaskCount = new AtomicInteger(0);

            // Initialize the ThreadFactory with a lambda expression.
            mThreadFactory =
                // Factory method that returns a new thread.
                (runnable) -> new Thread(runnable,
                                         // Uniquely name each AsyncTask.
                                         "AsyncTask #"
                                         + mTaskCount.incrementAndGet());
            /* Could also use this more verbose method:
            mThreadFactory = new ThreadFactory() {
                public Thread newThread(Runnable runnable) {
                    return new Thread(runnable, "AsyncTask #" + mTaskCount.incrementAndGet());
                }
            }
            */

            // Create a new "cached" ThreadPoolExecutor that's will
            // execute the AsyncTasks concurrently.
            mExecutor =
                new ThreadPoolExecutor(0,
                                       Integer.MAX_VALUE,
                                       60L,
                                       TimeUnit.SECONDS,
                                       new SynchronousQueue<Runnable>(),
                                       mThreadFactory);
        }
    }

    /**
     * Store all the state that's related to the AsyncTasks, which
     * will be preserved across runtime configuration changes.
     */
    AsyncTaskRelatedState mAsyncTaskRelatedState;

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

        // Set mAsyncTaskRelatedState to the object that was stored by
        // onRetainNonConfigurationInstance() (if any).
        mAsyncTaskRelatedState =
            (AsyncTaskRelatedState) getLastNonConfigurationInstance();

        // A value of null indicates this is the first time in.
        if (mAsyncTaskRelatedState == null) 
            // Allocate the state once the first time in.
            mAsyncTaskRelatedState = 
                new AsyncTaskRelatedState();

        // If this isn't the first time in then see if there are any
        // allocated GDCAsyncTasks that are running.
        else if (mAsyncTaskRelatedState.mTaskList.size() > 0) {
            // Set all the activities for all the GCDAsyncTasks.
            mAsyncTaskRelatedState.mTaskList.forEach
                (asyncTask 
                 -> asyncTask.setActivity(this));

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

        // Returns the AsyncTask-related state that must be saved
        // across runtime configuration changes.
        return mAsyncTaskRelatedState;
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        // Set the EditText that holds the count entered by the user
        // (if any).
        mCountEditText = (EditText) findViewById(R.id.count);

        // Store floating action button that sets the count.
        mSetFab = (FloatingActionButton) findViewById(R.id.set_fab);

        // Store floating action button that starts playing ping/pong.
        mStartOrStopFab = (FloatingActionButton) findViewById(R.id.play_fab);

        // Make the count button invisible for animation purposes.
        mStartOrStopFab.setVisibility(View.INVISIBLE);

        if (TextUtils.isEmpty(mCountEditText.getText().toString().trim()))
            // Make the EditText invisible for animation purposes.
            mCountEditText.setVisibility(View.INVISIBLE);

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

                        // Show the "startOrStop" FAB.
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
            // Hides the count FAB.
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
        // See if there are AsyncTasks present (they only exist while
        // GCD computations are in progress).
        if (mAsyncTaskRelatedState.mTaskList.size() > 0)
            // Cancel the computations.
            cancelComputations();
        else 
            // Start running the computations specified by the user.
            startComputations(mCountEditText.getText().toString());
    }

    /**
     * Start the GCD computations based on the @a userInput.
     */
    public void startComputations(String userInput) {
        // See if user specified the number of AsyncTasks after the
        // number of iterations.
        String[] splitInput =
            userInput.split(":");

        // Set the max iteration count.
        int count = Integer.valueOf(splitInput[0]);

        // Set the max AsyncTask count.
        int asyncTaskCount = splitInput.length > 1 
            ? Integer.valueOf(splitInput[1])
            : sMAX_TASK_COUNT;

        // Make sure there's a positive count.
        if (count <= 0) 
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                              "Please specify a count value that's > 0");
        else {
            // Create all the GCDAsyncTasks.
            for (int id = 1; id <= asyncTaskCount; ++id)
                mAsyncTaskRelatedState.mTaskList.add
                    (new GCDAsyncTask(this,
                                      id,
                                      new Random()));

            // Iterate through the list of GCDAsyncTasks and
            // start executing them.
            mAsyncTaskRelatedState.mTaskList.forEach
                (asyncTask -> 
                 // Execute the asyncTask on the ThreadPoolExecutor
                 // (note use of a black-box framework "strategy".
                 asyncTask.executeOnExecutor
                     (mAsyncTaskRelatedState.mExecutor,
                      count));

            // Update the start/stop FAB to display a stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
        }
    }

    /**
     * Cancel all the AsyncTasks running GCD computations.
     */
    private void cancelComputations() {
        // Cancel all the GCDAsyncTasks immediately.
        mAsyncTaskRelatedState.mTaskList.forEach(asyncTask-> 
                                                 asyncTask.cancel(true));

        UiUtils.showToast(this,
                          "Cancelling all "
                          + mAsyncTaskRelatedState.mTaskList.size()
                          + " async tasks ");
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        // Create a command to reset the UI.
        Runnable command = () -> {
            // Clear the task list.
            mAsyncTaskRelatedState.mTaskList.clear();

            // Reset the start/stop FAB to the play icon.
            mStartOrStopFab.setImageResource(android.R.drawable.ic_media_play);
        };

        // Run the command in the UI thread.
        command.run();
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

        // Run the command in the UI thread.
        command.run();
    }

    /**
     * Lifecycle hook method called when this activity is being
     * destroyed.
     */
    protected void onDestroy() {
        // Call the super class.
        super.onDestroy();

        // If the activity is going away then (i.e., not simply changing
        // the runtime configuration) then cancel all AsyncTasks.
        if (mAsyncTaskRelatedState.mTaskList.size() > 0
            && !isChangingConfigurations()) {
            Log.d(TAG,
                  "canceling all the async tasks ");

            // Cancel all the GCDAsyncTasks.
            mAsyncTaskRelatedState.mTaskList.forEach(asyncTask 
                                                     -> asyncTask.cancel(true));

            // Shutdown all the threads in the ThreadPoolExecutor.
            mAsyncTaskRelatedState.mExecutor.shutdown();
        }
    }
}
