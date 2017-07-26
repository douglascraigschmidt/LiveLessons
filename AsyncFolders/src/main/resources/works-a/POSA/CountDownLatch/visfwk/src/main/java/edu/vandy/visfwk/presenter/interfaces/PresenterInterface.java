package edu.vandy.visfwk.presenter.interfaces;

/**
 * Single interface for referencing all Presenter interfaces.
 */
public interface PresenterInterface 
       extends NotifyOfGUIActionsInterface {

    /**
     * Notify the presenter layer of a state change.
     */
    void notifyOfStateChange();
}
