package vandy.mooc.prime.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import vandy.mooc.prime.R;
import vandy.mooc.prime.utils.PrimeCheckers;
import vandy.mooc.prime.utils.TextViewKt;
import vandy.mooc.prime.utils.TimedMemoizerEx;
import vandy.mooc.prime.utils.UiUtils;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Main activity for an app that shows how to use the Java
 * ExecutorCompletionService interface and a fixed-size thread pool to
 * determine if n random numbers are prime or not.  The user can
 * interrupt the thread performing this computation at any point and
 * the thread will also be interrupted when the activity is destroyed.
 * In addition, runtime configuration changes are handled gracefully.
 */
public class MainActivity extends LifecycleLoggingActivity {
    /**
     * Number of primes to evaluate if the user doesn't specify
     * otherwise.
     */
    private final static int DEFAULT_COUNT = 100;

    /**
     * Maximum random number value.
     */
    private static long MAX_VALUE = 1000000000L;

    /**
     * Maximum range of random numbers where range is
     * [MAX_VALUE - MAX_COUNT .. MAX_VALUE].
     */
    private static int MAX_COUNT = 1000;
    /**
     * EditText field for entering the desired number of iterations.
     */
    private EditText mCountEditText;
    /**
     * A TextView used to display the output.
     */
    private TextView mLogTextView;
    /**
     * A ProgressBar to show when computation tasks are running.
     */
    private ProgressBar mProgressBar;
    /**
     * Displays number of primes found so far.
     */
    private TextView mPrimesTextView;
    /**
     * Displays number of prime candidates processed so far.
     */
    private TextView mCandidatesTextView;
    /**
     * Start stop image view in tool bar.
     */
    private ImageView mStartStopView;
    /**
     * Store all the state that must be preserved across runtime
     * configuration changes.
     */
    private RetainedState mRetainedState;

    /**
     * Hook method called when the activity is first launched.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file.
        setContentView(R.layout.activity_main);

        // Initialize the views.
        initializeViews();

        // Set mRetainedState to the object that was stored by
        // onRetainCustomNonConfigurationInstance().
        mRetainedState =
                (RetainedState) getLastCustomNonConfigurationInstance();

        if (mRetainedState != null) {
            // Activity is being restored so reset reference to this
            // class in future runnable and update UI to reflect
            // currently running state.
            if (mRetainedState.mCompletionRunnable != null) {
                mRetainedState.mCompletionRunnable.setActivity(this);
            }
            updateToolbar();
        } else {
            // Allocate the state that's retained across runtime
            // configuration changes.
            mRetainedState = new RetainedState();
        }
    }

    /**
     * This hook method is called by Android as part of destroying an
     * activity due to a configuration change, when it is known that a
     * new instance will immediately be created for the new
     * configuration.
     */
    @Override
    public Object onRetainCustomNonConfigurationInstance() {
        // Returns mRetainedState so that it will be saved across
        // runtime configuration changes.
        return mRetainedState;
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

    /**
     * Called by framework to create a previously registered
     * context menu.
     *
     * @param menu     menu in which to inflate the context menu
     * @param v        view registered for this context menu
     * @param menuInfo (unused)
     */
    @Override
    public void onCreateContextMenu(
            ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_popup, menu);
        onPrepareOptionsMenu(menu);
    }

    /**
     * Called by framework to create the app options menu.
     *
     * @param menu menu in which to inflate the options menu.
     * @return true if handled by this method, false if not.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * Called each time a menu is about to be displayed.  Here we show
     * and hide menu items based on the current app state.
     *
     * @param menu menu to be displayed
     * @return true if this menu has been modified
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item;

        item = menu.findItem(R.id.action_run);
        if (item != null) {
            item.setVisible(!isRunning());
        }

        item = menu.findItem(R.id.action_cancel);
        if (item != null) {
            item.setVisible(isRunning());
        }

        item = menu.findItem(R.id.action_clear);
        if (item != null) {
            item.setVisible(mLogTextView.getText().length() > 0);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    /**
     * Called by framework when an options menu item is selected.
     *
     * @param item the selected menu item
     * @return true if this method handles the selection false if not
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return onMenuItemSelected(item) || super.onOptionsItemSelected(item);
    }

    /**
     * Called by framework when a context menu item is selected.
     *
     * @param item the selected menu item
     * @return true if this method handles the selection false if not
     */
    @Override
    public boolean onContextItemSelected(@NotNull MenuItem item) {
        return onMenuItemSelected(item) || super.onContextItemSelected(item);
    }

    /**
     * Common helper method that handles both option and context menu
     * commands.
     *
     * @param item selected menu item
     * @return true if menu command is handled, false if not
     */
    private boolean onMenuItemSelected(@NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_clear:
                mLogTextView.setText(null);
                return true;
            case R.id.action_run:
                startComputations();
                return true;
            case R.id.action_cancel:
                interruptComputations();
                return true;
        }

        return false;
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        // Setup toolbar.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup user input EditText widget to support a clear icon on
        // the right.
        mCountEditText = findViewById(R.id.input_view);
        TextViewKt.makeClearEditText(mCountEditText, null, null);

        // Store references to layout views.
        mProgressBar = findViewById(R.id.progress);
        mLogTextView = findViewById(R.id.textview);
        mPrimesTextView = findViewById(R.id.primes);
        mCandidatesTextView = findViewById(R.id.candidates);

        // Add listener to automatically scroll appended text into view.
        TextViewKt.autoScroll(mLogTextView);

        // Register a context popup menu for the main activity logging window.
        registerForContextMenu(mLogTextView);

        // Register a listener to help display "start playing" FAB
        // when the user hits enter. This listener also sets a default
        // count value if the user enters no value.
        mCountEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                UiUtils.hideKeyboard(MainActivity.this, mCountEditText.getWindowToken());
                if (TextUtils.isEmpty(mCountEditText.getText().toString().trim())) {
                    mCountEditText.setText(String.valueOf(DEFAULT_COUNT));
                }

                startComputations();

                return true;
            } else {
                return false;
            }
        });

        // Get reference to start stop view and set it's click handler.
        mStartStopView = findViewById(R.id.searchStopView);
        mStartStopView.setOnClickListener(view -> startStopComputations());
    }

    public void startStopComputations() {
        if (isRunning()) {
            interruptComputations();
        } else {
            startComputations();
        }
    }

    /**
     * Helper that extracts the user entered count value from the edit
     * text widget and calls startComputations to find prime factors.
     */
    public void startComputations() {
        // Start running the primality computations.
        int count;
        String text = mCountEditText.getText().toString();
        if (text.length() == 0) {
            count = DEFAULT_COUNT;
        } else {
            try {
                count = Integer.valueOf(text);
            } catch (Exception e) {
                UiUtils.showToast(this,
                        "Please specify a count in the " +
                                "range [1 .. " + MAX_COUNT + "]");
                return;
            }
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
            UiUtils.showToast(this,
                    "The maximum count value is " + MAX_COUNT + ".");
        }

        mCountEditText.setText(String.valueOf(count));
        mStartStopView.setImageResource(R.drawable.ic_stop_white_24dp);
        startComputations(count);
    }

    /**
     * Start running the primality computations.
     *
     * @param count Number of prime computations to perform.
     */
    public void startComputations(int count) {
        // Make sure there's a non-0 count.
        mRetainedState.mPrimeFactors = 0;
        mRetainedState.mProcessed = 0;

        if (count <= 0) {
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                    "Please specify a count value that's > 0");
        } else {
            setRunning(true);

            // Shutdown the memoizer if it already exists.
            if (mRetainedState.mTimedMemoizer != null)
                mRetainedState.mTimedMemoizer.shutdown();

            // Create the cache used to generate, store, and retrieve
            // the results of prime checking computations.
            mRetainedState.mTimedMemoizer =
                    new TimedMemoizerEx<>(PrimeCheckers::bruteForceChecker,
                            // Timeout cache entries after
                            // count * 0.5 seconds.
                            count * 500);

            // Submit "count" PrimeCallable objects that concurrently
            // check the primality of "count" random numbers.
            new Random()
                    // Generate "count" random between the min and max values.
                    .longs(count, MAX_VALUE - count, MAX_VALUE)

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
            mRetainedState.mThread =
                    new Thread(mRetainedState.mCompletionRunnable);
            mRetainedState.mThread.start();

            // Update toolbar to reflect started state.
            updateToolbar();

            println("Starting computations (count " + count + ")");
        }
    }

    /**
     * Stop the prime computations.
     */
    private void interruptComputations() {
        // Inform user an interrupt occurred.
        UiUtils.showToast(this,
                "Interrupting the computations");

        // Shutdown all the retained state.
        mRetainedState.shutdown();

        // Trigger a reset of the retained state on cancellation.
        mRetainedState = new RetainedState();

        // Not necessary to call done() here;
        // it will get called when run completes.
    }

    /**
     * Called from each PrimeRunnable instance when their calculations complete.
     *
     * @param primeCandidate the prime candidate being investigated
     * @param smallestFactor the smallest factor of the candidate or 0 if prime.
     */
    public void updateResults(long primeCandidate, long smallestFactor) {
        // Update status fields and status bar.
        mRetainedState.mProcessed++;
        if (smallestFactor == 0) {
            mRetainedState.mPrimeFactors++;
        }
        runOnUiThread(this::updateToolbar);


        // Update the UI logging output.
        if (smallestFactor > 0) {
            println(""
                    + primeCandidate
                    + " is not prime with smallest factor "
                    + smallestFactor);
        } else if (smallestFactor == 0) {
            println(""
                    + primeCandidate
                    + " is prime");
        }
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        // Build command to run on the UI thread.
        Runnable command = () -> {
            // Indicate the app is not longer running.
            setRunning(false);

            // Append the stringToPrint and terminate it with a
            // newline.
            println("Finished computations ("
                    + mRetainedState.mPrimeFactors + " found)\n");

            // Update the toolbar widgets.
            updateToolbar();
        };

        // Run the command on the UI thread.  This all is optimized
        // for the case where println() is called from the UI thread.
        runOnUiThread(command);
    }

    /**
     * Updates the toolbar to display the current calculation state.
     */
    private void updateToolbar() {
        mPrimesTextView.setText(String.valueOf(mRetainedState.mPrimeFactors));
        mCandidatesTextView.setText(String.valueOf(mRetainedState.mProcessed));
        if (isRunning() != (mProgressBar.getVisibility() == VISIBLE)) {
            mProgressBar.setVisibility(isRunning() ? VISIBLE : INVISIBLE);
        }

        if (isRunning()) {
            mStartStopView.setImageResource(R.drawable.ic_stop_white_24dp);
        } else {
            mStartStopView.setImageResource(R.drawable.ic_search_white_24dp);
        }
    }

    /**
     * Output string to log view.
     */
    public void println(String string) {
        // In case this was originally called from an AsyncTask or
        // some other off-UI thread, make sure the update occurs
        // within the UI thread.
        runOnUiThread(new Thread(() -> mLogTextView.append(string + "\n")));
    }

    /**
     * @return flag indicating if calculations are currently running.
     */
    private boolean isRunning() {
        return mRetainedState.mIsRunning;
    }

    /**
     * Sets flag to indicate calculation running state.
     */
    private void setRunning(boolean running) {
        mRetainedState.mIsRunning = running;
    }

    /**
     * State that must be preserved across runtime configuration
     * changes.
     */
    private static class RetainedState {
        /**
         * This object runs the prime computations.
         */
        final ExecutorCompletionService<PrimeCallable.PrimeResult>
                mExecutorCompletionService;
        /**
         * Debugging tag used by the Android logger.
         */
        private final String TAG =
                getClass().getSimpleName();
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
        TimedMemoizerEx<Long, Long> mTimedMemoizer;

        /**
         * Keeps track of the number of primes found.
         */
        int mPrimeFactors;

        /**
         * Keeps track of the number of prime candidates processed so far.
         */
        int mProcessed;

        /**
         * Flag indicating if calculations are ongoing.
         */
        boolean mIsRunning;

        /**
         * Constructor initializes the ExecutorService thread pool.
         */
        RetainedState() {
            // Create a "work-stealing" fork-join thread pool that
            // internally matches the number of cores.
            mExecutorService =
                    Executors.newWorkStealingPool();

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
            try {
                // Iterate through all the futures to get the results.
                for (int i = 0; i < mCount; ++i) {
                    // This call blocks until future is triggered.
                    Future<PrimeCallable.PrimeResult> resultFuture = mActivity
                            .mRetainedState
                            .mExecutorCompletionService
                            .take();

                    // get() won't block since results must be done
                    // before they're added to completion queue.
                    PrimeCallable.PrimeResult result =
                            resultFuture.get();

                    // Check results and display appropriate message.
                    mActivity.updateResults(
                            result.mPrimeCandidate,
                            result.mSmallestFactor);
                }
            } catch (Exception e) {
                Log.d(TAG,
                        "Prime waiter thread interrupted "
                                + Thread.currentThread());
            }

            // Finish up and reset the UI.
            mActivity.done();
        }
    }
}
