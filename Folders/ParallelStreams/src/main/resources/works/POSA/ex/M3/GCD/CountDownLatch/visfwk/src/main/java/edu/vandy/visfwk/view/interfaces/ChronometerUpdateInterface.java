package edu.vandy.visfwk.view.interfaces;

import android.view.View;

import edu.vandy.visfwk.utils.Chronometer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for Presenter to interact with Custom Chronometer.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface ChronometerUpdateInterface {
    /**
     * Keeps track of how long the ProducerTask and ConsumerTask take
     * to perform their computations.
     */
    List<WeakReference<Chronometer>> mChronometerRef =
        new ArrayList<>(1);

    /**
     * Helper method that localizes access to the Chronometer
     * reference and gives an appropriate error if the Chronometer
     * instance was not initialized.
     */
    default Chronometer getChronometer() {
        if (mChronometerRef.get(0) == null
            || mChronometerRef.get(0).get() == null) 
            throw new RuntimeException("Un-Initialized Chronometer accessed.");

        return mChronometerRef.get(0).get();
    }

    /**
     * Initialize the Chronometer instance from the UI to this
     * Interface so that this Interface's default methods can operate
     * properly.
     */
    default void initializeChronometer(Chronometer chronometer) {
        if (mChronometerRef.size() != 0) 
            mChronometerRef.remove(0);

        mChronometerRef.add(0,
                            new WeakReference<>(chronometer));
    }

    /**
     * Set the 'Base' time to start counting 'from' (In UnixTime)
     */
    default void chronometerSetBase(final long chronometerBase) {
        getChronometer().setBase(chronometerBase);
    }

    /**
     * Set the visibility of the Chronometer.
     */
    default void chronometerSetVisibility(final boolean value) {
        // If (value' == true) then set to VISIBLE, else set to INVISIBLE.
        getChronometer().setVisibility(value ? View.VISIBLE : View.INVISIBLE);
    }

    /**
     * Reset the Text of the Chronometer.
     */
    default public void resetText() {
        getChronometer().setText("0:00:00");
    }

    /**
     * Start the Chronometer counting.
     */
    default public void chronometerStart() {
        getChronometer().start();
    }

    /**
     * Stop the Chronometer counting.
     */
    default public void chronometerStop() {
        getChronometer().stop();
    }
}
