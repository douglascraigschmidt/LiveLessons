package edu.vandy.visfwk.view.interfaces;

/**
 * Interface for Presenter Layer to interact with Progress bars of
 * each Task.
 */
@SuppressWarnings("unused")
public interface ProgressBarInterface {
    /**
     * Update the progress for a specific Test's uniqueID and the
     * progress it should have.
     */
    public void setProgress(int uniqueID,
                            int progress);
}
