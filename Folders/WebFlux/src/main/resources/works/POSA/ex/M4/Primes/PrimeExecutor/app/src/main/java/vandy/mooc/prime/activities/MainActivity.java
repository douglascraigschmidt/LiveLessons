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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import vandy.mooc.prime.R;
import vandy.mooc.prime.utils.TextViewKt;
import vandy.mooc.prime.utils.UiUtils;

public class MainActivity extends AppCompatActivity {
    /**
     * Debugging tag used by the Android logger.
     */
    private final static String TAG = "MainActivity";

    /**
     * Number of primes to evaluate if the user doesn't specify
     * otherwise.
     */
    private final static int DEFAULT_COUNT = 100;

    /**
     * Bundle key used during configuration changes.
     */
    private static final String KEY_RESTART = "RESTART";

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
     * Flag indicating that calculations were running at the
     * time of a configuration change and therefore should be
     * restarted when the activity is recreated.
     */
    private boolean mRestart = false;

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
     * Reference to the Executor that runs the prime computations.
     * Only allocate as many threads as there are processor cores
     * since determining primes is a CPU-bound computation.
     */
    private Executor mExecutor =
        Executors.newFixedThreadPool(Runtime
                                     .getRuntime()
                                     .availableProcessors());

    /**
     * Keeps track of the number of running tasks.
     */
    private AtomicInteger mRunningTasks =
        new AtomicInteger(0);

    /**
     * Keeps track of the number of primes found.
     */
    int mPrimeFactors = 0;

    /**
     * Keeps track of the number of prime candidates processed so far.
     */
    int mProcessed = 0;

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

        // Restore possible restart state in case calculation needs a
        // restart.
        mRestart = savedInstanceState != null 
            && savedInstanceState.getBoolean(KEY_RESTART);
    }

    /**
     * Called by framework when activity is activated.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Check if the activity requires restarting from a configuration
        // change.
        if (mRestart) {
            // NOTE: This strategy is NOT RECOMMENDED and is is highly inefficient.
            // It will result in the previously running threads to hold on to a
            // reference to the old activity (memory leak) and also to continue
            // to perform computations even though their results will never be
            // used or seen by the user.

            mLogTextView.setText(null);
            startComputations();
        }
    }

    /**
     * Called by framework when activity before activity about to be
     * recreated due to a configuration change.
     *
     * @param outState Bundle into which to save state information.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_RESTART, mRunningTasks.get() > 0);
        super.onSaveInstanceState(outState);
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


        // Get reference to start view and set it's click handler.
        findViewById(R.id.searchView).setOnClickListener(view -> startComputations());
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
     * Common helper method that handles both option and context menu commands.
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
        }

        return false;
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
    public void onCreateContextMenu(ContextMenu menu,
                                    View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_popup, menu);
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
            item.setEnabled(mRunningTasks.get() == 0);
        }

        item = menu.findItem(R.id.action_clear);
        if (item != null) {
            item.setEnabled(mLogTextView.getText().length() > 0);
        }

        return super.onPrepareOptionsMenu(menu);
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
                                  "Please specify a count in the range [1 .. " 
                                  + MAX_COUNT 
                                  + "]");
                return;
            }
        }

        if (count > MAX_COUNT) {
            count = MAX_COUNT;
            UiUtils.showToast(this,
                              "The maximum count value is " 
                              + MAX_COUNT 
                              + ".");
        }

        mCountEditText.setText(String.valueOf(count));
        startComputations(count);
    }

    /**
     * Start running the primality computations.
     *
     * @param count Number of prime computations to perform.
     */
    public void startComputations(int count) {
        // Since this implementation doesn't support cancelling the
        // calculation, warn the user and do not start a new computation
        // while and existing one is ongoing.
        if (mRunningTasks.get() > 0) {
            UiUtils.showToast(this,
                    "The current computations must complete " +
                            "before starting new computations.");
            return;
        }

        // Clear status bar stats counters.
        mPrimeFactors = 0;
        mProcessed = 0;

        if (count <= 0) {
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                              "Please specify a count value that's > 0");
        } else {
            // Set the number of running tasks to the count.
            mRunningTasks.set(count);

            // Show progress bar.
            mProgressBar.setVisibility(View.VISIBLE);

            // Create "count" random values and check to see if they
            // are prime.
            new Random()
                // Generate "count" random between sMAX_VALUE - count
                // and sMAX_VALUE.
                .longs(count, MAX_VALUE - count, MAX_VALUE)

                // Convert each random number into a PrimeRunnable and
                // execute it.
                .forEach(randomNumber ->
                         mExecutor.execute(new PrimeRunnable(this,
                                                             randomNumber)));
            if (mRestart) {
                println("Restarting primality computations");
                mRestart = false;
            } else {
                println("Starting computations (count " + count + ")");
            }
        }
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        Log.d(TAG,
              "Finished in thread "
              + Thread.currentThread());

        if (mRunningTasks.decrementAndGet() == 0) {
            // Create a command to reset the UI.
            Runnable command = () -> {
                println("Finished computations ("
                        + mPrimeFactors 
                        + " found)\n");

                // Show progress bar.
                mProgressBar.setVisibility(View.INVISIBLE);
            };

            // Run the command on the UI thread.  This all is
            // optimized for the case where println() is called from
            // the UI thread.
            runOnUiThread(command);
        }
    }

    /**
     * Called from each PrimeRunnable instance when their calculations
     * complete.
     *
     * @param primeCandidate the prime candidate being investigated
     * @param smallestFactor the smallest factor of the candidate or 0 if prime.
     */
    public void updateResults(long primeCandidate, long smallestFactor) {
        // Update status fields and status bar.
        mProcessed++;
        if (smallestFactor == 0) {
            mPrimeFactors++;
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
     * Updates the toolbar to display the current calculation state.
     */
    private void updateToolbar() {
        mPrimesTextView.setText(String.valueOf(mPrimeFactors));
        mCandidatesTextView.setText(String.valueOf(mProcessed));
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
}
