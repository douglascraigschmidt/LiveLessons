package vandy.mooc.pingpong.view;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Locale;

import vandy.mooc.pingpong.R;
import vandy.mooc.pingpong.presenter.PingPongPresenter;
import vandy.mooc.pingpong.utils.Options;
import vandy.mooc.pingpong.utils.UiUtils;

import static vandy.mooc.pingpong.presenter.PingPongPresenter.Gamestate.PLAY;
import static vandy.mooc.pingpong.presenter.PingPongPresenter.Gamestate.RESET;

/**
 * This is the main activity for the PingPong app, which plays the
 * "View" role in the Model-View-Presenter (MVP) pattern.  It handles
 * runtime configuration changes gracefully (i.e., continues running
 * in a background thread) via the onRetainNonConfigurationInstance()
 * and getLastNonConfigurationInstance() methods.  These methods
 * return a PingPongPresenter object, which plays the "Presenter" role
 * in the MVP pattern.
 */
public class MainActivity
       extends LifecycleLoggingActivity {
   /**
     * An EditText field uesd to enter the desired number of iterations.
     */
    private EditText mCountEditText;

    /** 
     * A plain TextView that PingPong will be "played" upon. 
     */
    private TextView mPingPongTextViewLog;

    /** 
     * A ScrollView that contains the PingPong results.
     */
    private ScrollView mPingPongScrollView;

    /** 
     * A more colorful TextView that prints "Ping" or "Pong" to the
     * display.
     */
    private TextView mPingPongColorOutput;

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
     * Floating Action Button that allows playing and resetting the
     * concurrent ping/pong algorithm.
     */
    private FloatingActionButton mPlayOrResetFab;

    /**
     * Reference to the PingPongPresenter that implements the entry
     * point into the Presenter layer in the MVP pattern.
     */
    private PingPongPresenter mPresenter;
    
    /**
     * This lifecycle hook method is automatically called to perform
     * initialization operations when the activity is created.
     */
    protected void onCreate(Bundle savedInstanceState) {
        // Call up to the super class to perform platform
        // initializations.
        super.onCreate(savedInstanceState);

        // Sets the content view to the xml file, activity_ping_pong.
        setContentView(R.layout.main_activity);

        // Initialize the Views.
        initializeViews();

        // Initialize the Options singleton.
        Options.instance().parseArgs(null);

        // Set mPresenter to the object that was stored by
        // onRetainNonConfigurationInstance().
        setPresenter((PingPongPresenter) getLastNonConfigurationInstance());

        // Check to see if this is the first time in.
        if (getPresenter() == null) {
            // Create a new Presenter.
            setPresenter(new PingPongPresenter(this));
        } else {
            // Reinitialize the Presenter with the new instance of
            // this activity.
            getPresenter().onConfigurationChange(this);

            // Update the FAB based on the state of the game.
            updateFab();
        }
    }

    /**
     * Updates the FAB based on the game state.
     */
    private void updateFab() {
        mPlayOrResetFab.setImageResource
                (getPresenter().getGamestate() == PLAY
                        ? android.R.drawable.ic_media_play
                        : R.drawable.ic_media_stop);
        // Make the count button visible for animation purposes.
        mPlayOrResetFab.setVisibility(View.VISIBLE);
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

        // Returns mPresenter so that it will be saved across runtime
        // configuration changes.
        return getPresenter();
    }

    /**
     * Initialize the views and store them for later use.
     */
    private void initializeViews() {
        // Set the EditText that holds the count entered by the user
        // (if any).
        mCountEditText = (EditText) findViewById(R.id.count);

        // Cache floating action button that sets the count.
        mSetFab = (FloatingActionButton) findViewById(R.id.set_fab);

        // Cache floating action button that starts playing ping/pong.
        mPlayOrResetFab = (FloatingActionButton) findViewById(R.id.play_fab);

        // Make the EditText invisible for animation purposes.
        mCountEditText.setVisibility(View.INVISIBLE);

        // Make the count button invisible for animation purposes.
        mPlayOrResetFab.setVisibility(View.INVISIBLE);

        // Cache various views used to interact with the user.
        mPingPongTextViewLog =
            (TextView) findViewById(R.id.pingpong_text_output);
        mPingPongScrollView =
            (ScrollView) findViewById(R.id.scrollview_text_output);
        mPingPongColorOutput =
            (TextView) findViewById(R.id.pingpong_color_output);

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
                            mCountEditText.setText
                                (String.valueOf
                                 (Options.instance().maxIterations()));

                        UiUtils.showFab(mPlayOrResetFab);
                        return true;
                    } else {
                        return false;
                    }
                });
    }

    /** 
     * Sets the action of the button on click state. 
     */
    public void playOrResetGame(View view) {
        switch(getPresenter().getGamestate()) {
        case PLAY:
            // Empty TextView and prepare the UI to start another run
            // of the concurrent ping/pong algorithm.
            mPingPongScrollView.fullScroll(ScrollView.FOCUS_UP);
            mPingPongTextViewLog.setText(R.string.empty_string);

            // See if user provided the synchronization mechanism name
            // after the number of iterations.
            String[] splitInput =
                mCountEditText.getText().toString().split(":");
            
            // Start playing the ping/pong game.
            getPresenter().play
                (Integer.valueOf(splitInput[0]),
                 splitInput.length > 1 
                 ? splitInput[1] 
                 : Options.instance().syncMechanism());

            // Change the FAB and the next gamestate to RESET.
            mPlayOrResetFab.setImageResource(R.drawable.ic_media_stop);
            getPresenter().setGamestate(RESET);

            // Hide the EditText using circular reveal animation
            // and set boolean to false.
            UiUtils.hideEditText(mCountEditText, false);
            mIsEditTextVisible = false;

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mSetFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            break;
        case RESET:
            // Shutdown any resources allocated in the Presenter layer
            // during the previous run.
            getPresenter().reset();

            // Reset the color output.
            mPingPongColorOutput.setText("");
            mPingPongColorOutput.setBackgroundColor(Color.TRANSPARENT);

            // Change the FAB and the next gamestate to PLAY.
            mPlayOrResetFab.setImageResource(android.R.drawable.ic_media_play);
            getPresenter().setGamestate(PLAY);
            break;
        }
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
            UiUtils.hideEditText(mCountEditText, true);
            mIsEditTextVisible = false;

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mSetFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Hides the FAB.
            UiUtils.hideFab(mPlayOrResetFab);
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
     * Prints the @a output on the UI thread.
     */
    public void printOnUiThread(String output) {
        // Create a lambda expression that runs in the context of the
        // UI thread.
        runOnUiThread(() -> {
            // Append the output to the end of the view log.
            mPingPongTextViewLog.append(output);
            mPingPongScrollView.fullScroll(ScrollView.FOCUS_DOWN);

            // If we encounter a "ping", throw it up on the screen in
            // color.
            if (output.toLowerCase(Locale.US).contains("ping")) {
                mPingPongColorOutput.setBackgroundColor(Color.WHITE);
                mPingPongColorOutput.setTextColor(Color.BLACK);
                mPingPongColorOutput.setText("PING");
            } 
            // Likewise, do a similar thing for a "pong".
            else if (output.toLowerCase(Locale.US).contains("pong")) {
                mPingPongColorOutput.setBackgroundColor(Color.BLACK);
                mPingPongColorOutput.setTextColor(Color.WHITE);
                mPingPongColorOutput.setText("PONG");
            }
            else if (output.toLowerCase(Locale.US).contains("done")) {
                // Update the FAB when we're done.
                // Change the FAB and the next gamestate to PLAY.
                mPlayOrResetFab.setImageResource(android.R.drawable.ic_media_play);
                getPresenter().setGamestate(PLAY);
            }});
    }

    /**
     * Get the reference to the Presenter layer.
     */
    private PingPongPresenter getPresenter() {
        return mPresenter;
    }

    /**
     * Set the reference to the Presenter layer.
     */
    private void setPresenter(PingPongPresenter presenter) {
        mPresenter = presenter;
    }
}

