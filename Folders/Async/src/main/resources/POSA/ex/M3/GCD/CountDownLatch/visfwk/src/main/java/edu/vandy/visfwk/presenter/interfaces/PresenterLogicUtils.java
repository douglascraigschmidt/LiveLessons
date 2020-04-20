package edu.vandy.visfwk.presenter.interfaces;

import android.os.SystemClock;
import android.util.Log;

import edu.vandy.visfwk.model.interfaces.ModelStateInterface;
import edu.vandy.visfwk.view.interfaces.ViewInterface;

/**
 * A utility class that defines static helper methods used by the
 * PresenterLogic class.
 */
@SuppressWarnings("UnusedParameters")
public class PresenterLogicUtils {
    /**
     * TAG for logging.
     */
    private final static String TAG =
            PresenterLogicUtils.class.getCanonicalName();

    /**
     * Ensure this class is only used as a utility.
     */
    private PresenterLogicUtils() {
        throw new AssertionError();
    }

    /**
     * Reset and Start the Chronometer.
     *
     * @param viewInterface the interface instance for interacting with the View Layer.
     */
    public static <TestFunc> void resetAndStartChronometer(ViewInterface<TestFunc> viewInterface) {
        Log.d(TAG,
                "resetAndStartChronometer(....)");

        // Run a new Runnable on the UI thread to reset and start the
        // Chronometer.
        viewInterface
                .getFragmentActivity()
                .runOnUiThread(() -> {
                    viewInterface.chronometerStop();
                    viewInterface.chronometerSetBase(SystemClock.elapsedRealtime());
                    viewInterface.chronometerSetVisibility(true);
                    viewInterface.chronometerStart();
                });
    }

    /**
     * Reset the state and then display of the progress bars for each
     * test.
     *
     * @param viewInterface       the interface instance for interacting with the View Layer.
     * @param modelStateInterface the interface instance for interacting with the Model Layer.
     */
    public static <TestFunc> void resetProgressBars(ViewInterface<TestFunc> viewInterface,
                                                    ModelStateInterface<TestFunc> modelStateInterface) {
        Log.d(TAG,
                "resetProgressBars(....)");

        // For each of the underlying TaskTuple(s) do the following:
        for (int counter = 0;
             counter < modelStateInterface.getTaskTuplesCount();
             counter++) {
            // Set the progress to 0.
            modelStateInterface.getTaskTuple(counter).setProgressStatus(0);
            modelStateInterface.getTaskTuple(counter).setTimeCompletedString("00:00:00");
        }

    }
}
