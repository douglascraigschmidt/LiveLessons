package edu.vandy.visfwk.model.interfaces;

import edu.vandy.visfwk.model.ProgramState;
import edu.vandy.visfwk.model.TaskTuple;
import edu.vandy.visfwk.presenter.interfaces.PresenterInterface;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Interface for getting the program's runtime state and values.
 */
public interface ModelStateInterface<TestFunc> {
    /*
     * Local Storage to be inherited by class(s) implementing this Interface.
     */

    /**
     * Current State of the program.
     */
    ArrayList<ProgramState> mProgramState =
        new ArrayList<>(1);

    /**
     * Default number of runs to fill EditText when button pressed if
     * the user doesn't specify otherwise.
     */
    ArrayList<Long> mDefaultRuns = new ArrayList<>(1);

    /**
     * This mechanism exists to store the reference to the PresenterInterface
     * that is initialized after the fact by the App's setup process in the
     * TesterFragment's 'onCreate' method. This is done to use this Interface
     * with Defaults as a means to achieve multiple inheritance.
     */
    ArrayList<WeakReference<PresenterInterface>> mPresenterInterfaceRef =
        new ArrayList<>(1);

    /*
     * Default Methods inherited by class(s) implementing this Interface.
     */

    /**
     * Get the current State of the App.
     *
     * @return ProgramState Enum of app currently.
     */
    default ProgramState getCurrentState() {
        if (mProgramState.size() != 1)
            throw new RuntimeException("PresenterInterface Uninitialized");

        return mProgramState.get(0);
    }

    /**
     * Set the current State of the App.
     *
     * @param state ProgramState Enum to set to.
     */
    default void setState(ProgramState state) {
        // Store value after properly handling if value was existed
        // beforehand.
        if (mProgramState.size() != 0)
            mProgramState.remove(0);

        // Add the new state.
        mProgramState.add(0,
                          state);

        // notify the presenter layer that the state has changed.
        getPresenterInterface().notifyOfStateChange();
    }

    /**
     * Initialize the Counter instance from the UI to this Interface
     * so that this Interface's default methods can operate properly.
     */
    default void initializePresenterInterface(PresenterInterface presenterInterface) {
        if (mPresenterInterfaceRef.size() != 0)
            mPresenterInterfaceRef.remove(0);

        mPresenterInterfaceRef.add(0,
                                   new WeakReference<>(presenterInterface));
        mProgramState.add(ProgramState.NEW);
    }

    /**
     * Helper method to localize access of the EditText from its
     * storage and to give runtime exception if EditText was not
     * properly initialized.
     */
    default PresenterInterface getPresenterInterface() {
        PresenterInterface presenter =
            mPresenterInterfaceRef.get(0).get();

        if (presenter == null)
            throw new RuntimeException("PresenterInterface Uninitialized");

        return presenter;
    }

    /*
     * Methods that need to be implemented by class(s) implementing this Interface.
     */

    /**
     * Get the BaseTime to set the chronometer to.
     *
     * @param baseTime unix timestamp to set base time to.
     */
    void setBaseTime(long baseTime);

    /**
     * Get the base time (Unix Time) that the Chronometer should use.
     *
     * @return Long Base time Chronometer should use.
     */
    long getBaseTime();

    /**
     * Set the Time Elapsed on current test.
     *
     * @param timeElapsed long Time (Unix Time) Elapsed so far.
     */
    void setTimeElapsed(long timeElapsed);

    /**
     * Get Time (Unix Time) Elapsed in current test.
     *
     * @return long Time Elapsed.
     */
    long getTimeElapsed();

    /**
     * Get the number to use as a default number of test runs.
     *
     * @return long Default for the number of runs to test.
     */
    default long getDefaultRuns() {
        if (mDefaultRuns.size() == 0) {
            mDefaultRuns.add(0, (long) (1000));
        }
        return mDefaultRuns.get(0);
    }

    /**
     * Set value to be used when filling UI with default number of tests to run.
     *
     * @param defaultRuns long Value to use when requesting Default test number.
     */
    default void setDefaultRuns(long defaultRuns) {
        if (mDefaultRuns.size() != 0) {
            mDefaultRuns.remove(0);
        }
        mDefaultRuns.add(0, defaultRuns);
    }

    /**
     * Get the {@link TaskTuple} at the given position.
     *
     * @param position The position to get the AbstractTaskTuple of.
     * @return The TaskTuple at the given position.
     */
    TaskTuple<TestFunc> getTaskTuple(int position);

    /**
     * Set all the {@link TaskTuple} to be tested.
     *
     * @param tasks ArrayList of {@link TaskTuple} to be tested.
     */
    void setTaskTuples(List<TaskTuple<TestFunc>> tasks);

    /**
     * Get
     *
     * @return Count of {@link * TaskTuple} instances being tested.
     */
    int getTaskTuplesCount();

    /**
     * Get all of the Tasks being tested.
     *
     * @return All of the {@link TaskTuple}(s).
     */
    List<TaskTuple<TestFunc>> getTestTasks();
}

