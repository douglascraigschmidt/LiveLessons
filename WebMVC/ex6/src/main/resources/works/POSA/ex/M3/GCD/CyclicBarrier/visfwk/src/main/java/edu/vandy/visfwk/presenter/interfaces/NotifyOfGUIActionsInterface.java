package edu.vandy.visfwk.presenter.interfaces;

import android.view.View;

/**
 * Interface that allows the View layer to interact with the Presenter
 * layer.
 */
public interface NotifyOfGUIActionsInterface {
    /**
     * Indicate that the Set FAB was pressed.
     *
     * @param view
     * 	The view pressed.
     */
    void fabSetPressed(View view);

    /**
     * Indicate that the StartStop FAB was pressed.
     *
     * @param view
     * 	The view pressed.
     */
    void fabStartStopPressed(View view);
}
