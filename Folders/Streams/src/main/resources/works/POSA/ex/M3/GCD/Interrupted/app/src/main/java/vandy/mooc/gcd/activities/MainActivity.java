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
 * runtime configuration changes are handled relatively gracefully,
 * though the computations are restarted from the beginning.
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
     * Reference to the thread that runs the GCD computations.
     */
    private Thread mThread;

    /**
     * Keeps track of whether the orientation of the phone has been
     * changed.
     */
    private boolean mOrientationChange = false;

    /**
     * Hook method called when the activity is first launched.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file.
        setContentView(R.layout.main_activity);

        // Initialize the views.
        initializeViews(savedInstanceState);
    }

    /**
     * Initialize the views.
     */
    private void initializeViews(Bundle savedInstanceState) {
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

        // The activity is being restarted after an orientation
        // change.
        if (savedInstanceState != null) 
            mOrientationChange = true;

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
        if (mThread != null)
            // The thread only exists while GCD computations are in
            // progress.
            interruptComputations();
        else 
            // Start running the computations.
            startComputations(Integer.valueOf(mCountEditText.getText().toString()));
    }

    /**
     * Start the GCD computations.
     */
    public void startComputations(int count) {
        // Make sure there's a non-0 count.
        if (count <= 0) 
            // Inform the user there's a problem with the input.
            UiUtils.showToast(this,
                              "Please specify a count value that's > 0");
        else {
            // Create the GCD Runnable.
            GCDRunnable runnableCommand =
                new GCDRunnable(this,
                                count);

            // Create a new Thread that's will execute the runnableCommand
            // concurrently.
            mThread = new Thread(runnableCommand);

            // Start the thread.
            mThread.start();

            // Only print this message the first time the activity
            // runs.
            if (!mOrientationChange)
                println("Starting thread with name "
                        + mThread);

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
    }

    /**
     * Finish up and reset the UI.
     */
    public void done() {
        println("Finishing thread with name "
                + mThread);

        // Create a command to reset the UI.
        Runnable command = () -> {
            // Null out the thread to avoid later problems.
            mThread = null;

            // Reset the start/stop FAB to the play icon.
            mStartOrStopFab.setImageResource(android.R.drawable.ic_media_play);
        };

        // Run the command on the UI thread.  This all is optimized
        // for the case where println() is called from the UI thread.
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
     * Lifecycle hook method called when the activity is about gain
     * focus.
     */
    @Override
    protected void onResume() {
        // Call to the super class.
        super.onResume();

        // Check to see if an orientation change occurred.  This
        // implementation is simple and doesn't take into account how
        // much progress was made before the orientation occurred.
        if (mOrientationChange) {
            // Show the "startOrStop" FAB.
            UiUtils.showFab(mStartOrStopFab);

            // Start running the computations using the value
            // originally entered by the user.
            startComputations(Integer.valueOf(mCountEditText.getText().toString()));
        }
    }

    /**
     * Lifecycle hook method called when this activity is being
     * destroyed.
     */
    protected void onDestroy() {
        // Call the super class.
        super.onDestroy();

        if (mThread != null) {
            Log.d(TAG,
                  "interrupting thread "
                  + mThread);

            // Interrupt the thread since the activity is being
            // destroyed.
            mThread.interrupt();
        }
    }
}
