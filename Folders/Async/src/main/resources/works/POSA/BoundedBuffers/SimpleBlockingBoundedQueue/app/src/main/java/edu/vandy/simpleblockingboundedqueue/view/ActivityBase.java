package edu.vandy.simpleblockingboundedqueue.view;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import edu.vandy.simpleblockingboundedqueue.R;
import edu.vandy.simpleblockingboundedqueue.utils.Chronometer;
import edu.vandy.simpleblockingboundedqueue.utils.UiUtils;

/**
 * This super class factors out the material design-based GUI fields
 * and operations that are used in many example apps.
 */
public class ActivityBase
       extends LifecycleLoggingActivity {
    /**
     * EditText field for entering the desired number of iterations.
     */
    protected EditText mCountEditText;

    /**
     * Number of times to iterate if the user doesn't specify
     * otherwise.
     */
    protected final static int sDEFAULT_COUNT = 100000;

    /**
     * Keeps track of whether the edit text is visible for the user to
     * enter a count.
     */
    protected boolean mIsEditTextVisible = false;

    /**
     * Reference to the "set" floating action button.
     */
    protected FloatingActionButton mSetFab;

    /**
     * Reference to the "start or stop" floating action button.
     */
    protected FloatingActionButton mStartOrStopFab;

    /**
     * Keeps track of whether the orientation of the phone has been
     * changed.
     */
    protected boolean mOrientationChange = false;

    /**
     * Keeps track of how long the ProducerTask and ConsumerTask take
     * to perform their computations.
     */
    Chronometer mChronometer;

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

        // Store the Chronometer.
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        // Register a listener to help display "start playing" FAB
        // when the user hits enter.  This listener also sets a
        // default count value if the user enters no value.
        mCountEditText.setOnEditorActionListener
            ((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    UiUtils.hideKeyboard(this,
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
     * Called to retrieve per-instance state from an activity before
     * being killed so that the state can be restored in
     * onRestoreInstanceState().
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the current value of the Chronometer so it will be
        // available after a runtime configuration change.
        savedInstanceState.putLong("ChronoTime",
                                   mChronometer.getBase());
        
        // Call up to the super class.
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * This method is called after onStart() when the activity is
     * being re-initialized from a previously saved state, given here
     * in savedInstanceState.
     */
    public void onRestoreInstanceState(Bundle savedInstanceState){
        if (savedInstanceState != null 
            && savedInstanceState.containsKey("ChronoTime"))
            // Restore the current value of the Chronometer after a
            // runtime configuration change.
            mChronometer.setBase(savedInstanceState.getLong("ChronoTime"));

        // Call up to the super class.
        super.onRestoreInstanceState(savedInstanceState);
    }
}
