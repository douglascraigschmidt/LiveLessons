package edu.vandy.visfwk.view.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;


import edu.vandy.visfwk.R;
import edu.vandy.visfwk.model.TaskTuple;

import java.util.ArrayList;

/**
 * Custom adapter for list of TaskTuples.
 */
public class ListAdapter<TestFunc>
        extends ArrayAdapter<TaskTuple<TestFunc>> {
    /**
     * TAG used for logging.
     */
    private final static String TAG =
            ListAdapter.class.getCanonicalName();

    /**
     * Reference to the Layout File to be used for creating each row
     * of the ListView
     */
    private int mLayoutResourceID;

    /**
     * The underlying set of data being tracked for display.
     */
    private ArrayList<TaskTuple<TestFunc>> mTaskList;

    /**
     * A LayoutInflater used to 'inflate' (create) Views for display.
     */
    private LayoutInflater mInflater;

    /**
     * Constructor
     *
     * @param context          The {@link Context} used to create the UI components.
     * @param layoutResourceID The Layout Resource used to create each ListView's rows.
     * @param objects          The underlying set of data to be tracked and displayed dynamically.
     */
    public ListAdapter(Context context,
                       int layoutResourceID,
                       ArrayList<TaskTuple<TestFunc>> objects) {
        super(context,
                layoutResourceID,
                objects);
        Log.d(TAG,
                "constructor() : size: " + "" + objects.size());

        // Context to use for crating Views.
        mLayoutResourceID = layoutResourceID;
        mTaskList = objects;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * Called each time the ListAdapter needs to create a new view for
     * display on the ListView.
     */
    @NonNull
    @Override
    public View getView(int position,
                        View convertView,
                        @NonNull ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = mInflater.inflate(mLayoutResourceID,
                    parent,
                    false);
            viewHolder =
                    new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else
            //noinspection unchecked
            viewHolder = (ViewHolder) convertView.getTag();

        // get your data object
        final TaskTuple<TestFunc> task = mTaskList.get(position);

        // Assign values to your items of listView.
        viewHolder.setNewTask(task);
        return convertView;
    }

    /**
     * Notify the Adapter that the underlying dataset has changed.
     */
    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.v("List Adapter",
                "notify data set changed: " + mTaskList.size());
    }

    /**
     * ViewHolder is a POJO (Plain Old Java Object) that is used to
     * 'hold' the values of each row of UI Components in the
     * ListView. This simplifies the logic in the rest of the Adapter.
     */
    final class ViewHolder {
        /**
         * TextView for displaying Description of the Task.
         */
        TextView mTvTaskDesc;

        /**
         * TextView for displaying the % complete of the Task.
         */
        TextView mTvTaskCount;

        /**
         * Progress bar for this task.
         */
        ProgressBar mPbTask;

        /**
         * Completion Time TextView
         */
        TextView mCompletionTime;

        /**
         * Helper method to set the UI elements of this ListView row's
         * instance based on the {@link TaskTuple} being tested.
         */
        @SuppressLint("DefaultLocale")
        void setNewTask(TaskTuple<TestFunc> t) {
            mTvTaskDesc.setText(String.format("%s", t.getTestName()));
            mPbTask.setProgress(t.getProgressStatus());
            mTvTaskCount.setText(String.format("%d%%", t.getProgressStatus()));
            mCompletionTime.setText(String.format("%s", t.getTimeCompletedString()));
        }

        /**
         * Constructor
         *
         * @param parentView The Parent View of each Row's UI Elements.
         */
        ViewHolder(View parentView) {
            mTvTaskDesc = (TextView)
                    parentView.findViewById(R.id.TesterTaskTestName);
            mTvTaskCount = (TextView)
                    parentView.findViewById(R.id.TesterTaskProgressCount);
            mPbTask = (ProgressBar)
                    parentView.findViewById(R.id.TesterTaskProgressBar);
            mCompletionTime = (TextView)
                    parentView.findViewById(R.id.completionTime);
        }
    }

    /**
     * Get a count of the number of items being tracked by the Adapter.
     *
     * @return number of items tracked by the Adapter.
     */
    @Override
    public int getCount() {
        return mTaskList.size();
    }

    /**
     * Get the {@link TaskTuple} at the desired position.
     *
     * @param position Determines which {@link TaskTuple} to return.
     * @return The target {@link TaskTuple}.
     */
    @Override
    public TaskTuple<TestFunc> getItem(int position) {
        return mTaskList.get(position);
    }

    /**
     * Get the Item ID of a specific displayed row (Does NOT have to
     * be the same, depending on how different implementations of UI
     * and or underlying data-store work. In this Adapter they are 1
     * to 1 though.)
     *
     * @param position The position to get the Item ID of.
     * @return the Item ID of the value stored at 'position'.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Add a new {@link TaskTuple} to the adapter
     *
     * @param newTask new {@link TaskTuple} to track & display.
     * @return number of {@link TaskTuple} stored in adapter now.
     */
    public int addTask(TaskTuple<TestFunc> newTask) {
        mTaskList.add(newTask);
        notifyDataSetChanged();
        return mTaskList.size();
    }
}
