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

import vandy.mooc.gcd.R;
import vandy.mooc.gcd.utils.UiUtils;

/**
 * Main activity for an app that shows how to start and interrupt a
 * Java thread that computes the greatest common divisor (GCD) of two
 * numbers, which is the largest positive integer that divides two
 * integers without a remainder.  The user can interrupt the thread
 * performing this computation at any point and the thread will also
 * be interrupted when the activity is destroyed.  In addition,
 * runtime configuration changes are handled gracefully, i.e., without
 * having to restart the computations from the beginning.
 */
public class MainActivity 
       extends LifecycleLoggingActivity {
    /**
     * Number of times to iterate if the user doesn't specify
     * otherwise.
     */
    private final static int sDEFAULT_COUNT = 100000000;

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
     * Keeps track of whether a button click from the user is
     * processed or not.  Only one click is processed until the GCD
     * computations are finished.
     */
    public static boolean mProcessButtonClick = true;

    /** 
     * A TextView used to display the output.
     */
    private TextView mTextViewLog;

    /** 
     * A ScrollView that contains the results of the TextView.
     */
    private ScrollView mScrollView;

    /**
     * Reference to the thread that runs the GCD computations.
     */
    private GCDThread mThread;

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

        // Set mThread to the object that was stored by
        // onRetainNonConfigurationInstance().
        mThread = (GCDThread) getLastNonConfigurationInstance();

        if (mThread != null) {
            // Set the activity.
            mThread.setActivity(this);

            // Update the start/stop FAB to display a stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);

            // Show the "startOrStop" FAB.
            UiUtils.showFab(mStartOrStopFab);
        }
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
        if (mThread != null)
            // The thread only exists while GCD computations are in
            // progress.
            interruptComputations();
        else {
            // Get the count from the edit view.
            startComputations(Integer.valueOf(mCountEditText.getText().toString()));
        }
    }

    /**
     * Start the GCD computations.
     */
    private void startComputations(int count) {
        // Make sure there's a non-0 count.
        if (count <= 0) 
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                              "Please specify a count value that's > 0");
        else if (!mProcessButtonClick)
            // Inform the user they can't play yet.
            UiUtils.showToast(this,
                              "GCD computations are in progress");
        else {
            // Create a new Thread that's will perform the GCD
            // computations concurrently.
            mThread = new GCDThread(this, count);

            // Start the thread.
            mThread.start();

            // Inform the user that we're starting the GCD
            // computations.
            println("starting thread with id " + mThread);

            mTextViewLog.setText(R.string.empty_string);
            mScrollView.fullScroll(ScrollView.FOCUS_UP);

            // Update the start/stop FAB to display a stop icon.
            mStartOrStopFab.setImageResource(R.drawable.ic_media_stop);
        }
    }

    /**
     * Stop the GCD computations.
     */
    private void interruptComputations() {
        // Interrupt the GCD thread.
        mThread.interrupt();

        UiUtils.showToast(this,
                          "Interrupting thread "
                          + mThread);

        // Finish up and reset the UI.
        done();
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        println("finishing thread " + mThread);

        // Create a command to reset the UI.
        Runnable command = () -> {
            // Allow user input again.
            mProcessButtonClick = true;

            // Null out the thread to avoid later problems.
            mThread = null;

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
     * This hook method is called by Android as part of destroying an
     * activity due to a configuration change, when it is known that a
     * new instance will immediately be created for the new
     * configuration.
     */
    @Override
    public Object onRetainNonConfigurationInstance() {
        // Call the super class.
        super.onRetainNonConfigurationInstance();

        // Returns mThread so that it will be saved across runtime
        // configuration changes.
        return mThread;
    }

    /**
     * Lifecycle hook method called when this activity is destroyed.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mThread != null
            && !isChangingConfigurations()) {
            // Interrupt the thread since the activity is being
            // destroyed.
            mThread.interrupt();

            Log.d(TAG,
                  "interrupting thread "
                  + mThread);
        }
    }
}
