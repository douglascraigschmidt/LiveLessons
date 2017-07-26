package edu.vandy.visfwk.view.interfaces;

import android.support.v4.app.FragmentActivity;

import java.util.List;

import edu.vandy.visfwk.model.TaskTuple;

/**
 * This Interface combines all the separate Interfaces for the View
 * layer.
 */
public interface ViewInterface<TestFunc>
       extends ChronometerUpdateInterface,
               CountUpdateInterface<TestFunc>,
               FabUpdateInterface,
               ProgressBarInterface {
    /**
     * Set the data from the model to the ArrayAdapter. (used to init
     * the system).
     *
     * @param data
     * 	List of {@link TaskTuple} to use.
     */
    public void setData(List<TaskTuple<TestFunc>> data);

    /**
     * Get the data from the 'model' (stored in View Layer b/c of how
     * Android's Adapters work).
     *
     * @return List<TaskTuple> Tasks to test.
     */
    public List<TaskTuple<TestFunc>> getData();

    /**
     * Make a Toast on the UI Thread.
     *
     * @param stringValue
     * 	String value to toast to user.
     */
    public void showToast(String stringValue);

    /**
     * Get a reference to the underlying FragmentActivity that
     * contains the View Fragment.
     *
     * @return FragmentActivity The FragmentActivity that contains the
     * View Layer Fragment.
     */
    public FragmentActivity getFragmentActivity();

    /**
     * Notify the fragment (View layer) that the dataset (Model layer)
     * has change.
     */
    public void notifyDataSetChanged();
}
