package edu.vandy.visfwk.presenter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import edu.vandy.visfwk.R;
import edu.vandy.visfwk.model.ProgramState;
import edu.vandy.visfwk.model.abstracts.AbstractTestTask;
import edu.vandy.visfwk.model.interfaces.ModelStateInterface;
import edu.vandy.visfwk.presenter.interfaces.PresenterInterface;
import edu.vandy.visfwk.presenter.interfaces.PresenterLogicUtils;
import edu.vandy.visfwk.utils.UiUtils;
import edu.vandy.visfwk.view.interfaces.ViewInterface;

/**
 * This is the 'logic' of the app, which defines the presentation
 * layer 'core'.
 */
@SuppressWarnings("unused")
public abstract class PresenterLogic<TestFunc>
        implements PresenterInterface {
    /**
     * TAG for logging.
     */
    private final static String TAG =
            PresenterLogic.class.getCanonicalName();

    /**
     * Interface for interacting with View layer.
     */
    private ViewInterface<TestFunc> mViewInterface;

    /**
     * Number of cycles to run with the CyclicBarrier.
     */
    private static final int sCYCLES = 1;

    /**
     * Interface for interacting with Model layer.
     */
    private ModelStateInterface<TestFunc> mModelStateInterface;

    /**
     * AsyncTask for testing of the tasks.
     */
    private AbstractTestTask<TestFunc> mTestTask;

    /**
     * Constructor.
     *
     * @param viewInterface       Interface for the View Layer
     * @param modelStateInterface Interface for the Model Layer
     */
    protected PresenterLogic(ViewInterface<TestFunc> viewInterface,
                             ModelStateInterface<TestFunc> modelStateInterface) {
        mViewInterface = viewInterface;
        mModelStateInterface = modelStateInterface;
    }

    /**
     * A factory method that returns a AbstractTestTask to perform the tests.
     *
     * @param viewInterface       Reference to the View layer.
     * @param modelStateInterface Reference to the Model layer.
     * @param presenterLogic      Reference to the Presenter layer.
     * @param numberOfTests       Number of tests to run.
     * @return A TaskTaskBase to perform the tests.
     */
    protected abstract AbstractTestTask<TestFunc> makeTestTask(ViewInterface<TestFunc> viewInterface,
                                                               ModelStateInterface<TestFunc> modelStateInterface,
                                                               PresenterLogic<TestFunc> presenterLogic,
                                                               int numberOfTests);

    /**
     * Handle the situation where there fabSet button was pressed.
     *
     * @param view The View that was pressed.
     */
    @Override
    public void fabSetPressed(View view) {
        Log.d(TAG,
                "fabSetPressed(...)" + mModelStateInterface.getCurrentState());

        switch (mModelStateInterface.getCurrentState()) {
            case ENABLED:
                break;
            case RUNNING:
            case CANCELLED:
            case FINISHED:
            case NEW:
            default:
                startConfig(view);
                break;
        }
    }

    /**
     * Logic of what to do when on screen button is pressed.
     *
     * @param view The View pressed.
     */
    @Override
    public void fabStartStopPressed(View view) {
        Log.d(TAG,
                "fabStartStopPressed(...)" + mModelStateInterface.getCurrentState());

        switch (mModelStateInterface.getCurrentState()) {
            case ENABLED:
                startTests(view);
                break;
            case RUNNING:
                cancelTests(view);
                break;
            case FINISHED:
            case CANCELLED:
                resetUIAfterCancelOrFinish(view);
                break;
            case NEW:
            default:
                break;
        }
    }

    /**
     * Enable Configuration of how many tests to run and play button.
     *
     * @param view View that was pressed that started this method.
     */
    private void startConfig(View view) {
        Log.d(TAG,
                "startConfig(...)");
        mModelStateInterface.setState(ProgramState.ENABLED);
    }

    /**
     * Helper method to contain code to run when starting Tests.
     *
     * @param view View Pressed.
     */
    private void startTests(View view) {
        Log.d(TAG,
              "startTests() Started");

        int numberOfTests;

        // Try to get # from the EditText and handle any exceptions.
        try {
            numberOfTests =
                Integer.valueOf(mViewInterface
                                .getCountEditText()
                                .getText()
                                .toString()
                                .trim());
        } catch (Exception ex) {
            if (ex instanceof NullPointerException) {
                Log.d(TAG,
                      "The text input for numbers was null");
                mViewInterface.showToast("Set number of runs");
            } else if (ex instanceof NumberFormatException) {
                Log.d(TAG,
                      "The text input was not a number");
                mViewInterface.showToast("Value entered for number of runs is empty"
                                         + " or is not a number.");
            }
            Log.d(TAG,
                  "unknown Exception occurred" + ex.getMessage());
            return;
        }

        // Do actual tests if valid testing #
        if (numberOfTests > 0) {
            // Set state to running.
            mModelStateInterface.setState(ProgramState.RUNNING);

            // Create the test task.
            mTestTask = makeTestTask(mViewInterface,
                                     mModelStateInterface,
                                     this,
                                     numberOfTests);

            // Execute the test task.
            mTestTask.execute(sCYCLES);
        }
    }

    /**
     * Reset the UI After Tests Cancelled.
     *
     * @param view View pressed that initiated this method being called.
     */
    private void resetUIAfterCancelOrFinish(View view) {
        Log.d(TAG,
                "resetUIAfterCancelOrFinish(...)");

        // Reset the backend data for tracking of the Progress bars,
        // and then update the UI.
        PresenterLogicUtils.resetProgressBars(mViewInterface,
                mModelStateInterface);

        // Set the application state to NEW.
        mModelStateInterface.setState(ProgramState.NEW);
        mViewInterface.notifyDataSetChanged();
    }

    /**
     * Cancel the current tests being ran.
     *
     * @param view View that was pressed that initiated this method.
     */
    private void cancelTests(View view) {
        Log.d(TAG,
                "cancelTests(...)");
        // notify the test task that it can cancel.
        mTestTask.cancel(true);

        // Set the state to CANCELLED
        mModelStateInterface.setState(ProgramState.CANCELLED);
    }

    /**
     * Notify the presenter layer of a state change.
     */
    @Override
    public void notifyOfStateChange() {
        ProgramState state =
            mModelStateInterface.getCurrentState();
        Log.d(TAG,
              "notifyOfStateChange(...)" + state);

        switch (state) {
        case NEW:
            processingNew();
            break;
        case ENABLED:
            processingEnabled();
            break;
        case RUNNING:
            processingRunning();
            break;
        case CANCELLED:
            processingCancelled();
            break;
        case FINISHED:
            processingFinished();
            break;
        default:
            break;
        }
    }

    /**
     * Update the UI to represent being in the New State.
     */
    private void processingNew() {
        // Stop and set the Chronometer to be invisible.
        mViewInterface.getChronometer()
                      .stop();
        mViewInterface.getChronometer()
                      .setVisibility(View.INVISIBLE);

        // Reset the backend data for tracking of the Progress bars,
        // and then update the UI.
        PresenterLogicUtils.resetProgressBars(mViewInterface,
                                              mModelStateInterface);
        // Notify the View Layer that the data(Model) has changed.
        mViewInterface.notifyDataSetChanged();

        // reset FABs, hide play/stop and show set.
        UiUtils.hideFab(mViewInterface.getFABStartOrStop());
        mViewInterface.getFABStartOrStop()
                      .setImageResource(android.R.drawable.ic_media_play);
        mViewInterface.getFABStartOrStop()
                      .setVisibility(View.INVISIBLE);
        UiUtils.showFab(mViewInterface.getFABSet());

        // clear and make invisible the EditText for run count.
        mViewInterface.getCountEditText()
                      .getText()
                      .clear();
        mViewInterface.getCountEditText()
                      .setVisibility(View.INVISIBLE);
        mViewInterface.getCountEditText()
                      .setEnabled(true);

    }

    /**
     * Update the UI to represent being in the Enabled State.
     */
    private void processingEnabled() {

        // Enable the EditText, clear the number of runs, and fill default.
        mViewInterface.getCountEditText()
                      .setEnabled(true);
        mViewInterface.getCountEditText()
                      .getText()
                      .clear();
        mViewInterface.getCountEditText()
                     .setVisibility(View.VISIBLE);

        // Request focus to EditText and open onscreen keypad
        mViewInterface.getCountEditText().requestFocus();
        InputMethodManager imm = (InputMethodManager)
            mViewInterface.getCountEditText()
                          .getContext()
                          .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mViewInterface.getCountEditText(),
                          InputMethodManager.SHOW_IMPLICIT);

        mViewInterface.getFABStartOrStop()
                      .setImageResource(android.R.drawable.ic_media_play);
    }

    /**
     * Update the UI to represent being in the Running State.
     */
    private void processingRunning() {
        // Disable edit text.
        mViewInterface.getCountEditText()
                      .setEnabled(false);

        // Hide set FAB.
        UiUtils.hideFab(mViewInterface.getFABSet());

        // Change play fab to stop.
        mViewInterface.getFABStartOrStop()
                      .setImageResource(android.R.drawable.ic_delete);

        // Start running the chronometer.
        PresenterLogicUtils.resetAndStartChronometer(mViewInterface);
    }

    /**
     * Update the UI to represent being in the Finished State.
     */
    private void processingFinished() {
        mViewInterface.getFABStartOrStop()
                      .setImageResource(R.drawable.ic_autorenew_white_24dp);
        mViewInterface.getChronometer()
                      .stop();
    }

    /**
     * Update the UI to represent being in the Cancelled State.
     */
    private void processingCancelled() {
        // Set the start/stop FAB to have 'refresh' image.
        mViewInterface.getFABStartOrStop()
                      .setImageResource(R.drawable.ic_autorenew_white_24dp);

        // Stop the Chronometer from continuing to count.
        mViewInterface.getChronometer()
                      .stop();
    }
}
