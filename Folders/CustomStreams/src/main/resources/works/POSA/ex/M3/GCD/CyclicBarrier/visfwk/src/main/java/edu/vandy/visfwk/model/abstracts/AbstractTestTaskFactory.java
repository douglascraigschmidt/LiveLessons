package edu.vandy.visfwk.model.abstracts;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.AbstractMap.SimpleImmutableEntry;
import edu.vandy.visfwk.model.TaskTuple;
import edu.vandy.visfwk.model.interfaces.ModelStateInterface;
import edu.vandy.visfwk.presenter.PresenterLogic;
import edu.vandy.visfwk.view.interfaces.ViewInterface;

import static java.util.stream.Collectors.toList;

/**
 * Super class for factory classes that create the list of tasks to
 * test and the actual AsyncTask to test them on Android.
 */
public abstract class AbstractTestTaskFactory<TestFunc> {
    /**
     * Return the list of TaskTuples to test.
     */
    public final List<TaskTuple<TestFunc>> getTasksToTest() {
        // Get the functions and their names to test.
        List<SimpleImmutableEntry<String, TestFunc>> funcsToRun =
            getFuncsAndNames();

        // Automatically generates a unique id.
        AtomicInteger uniqueId = new AtomicInteger(0);

        // Return a List of TaskTuples containing the test tasks to run.
        return funcsToRun
            // Convert the EntrySet into a stream.
            .stream()

            // Create a new TaskTuple for each element in the
            // EntrySet.
            .map(entry
                 -> new TaskTuple<>(entry.getValue(),
                                    entry.getKey(),
                                    uniqueId.getAndIncrement()))

            // Limit the number of TaskTuples to the number of
            // functions.
            .limit(funcsToRun.size())

            // Convert the stream to a list.
            .collect(toList());
    }

    /**
     * The abstract method must be overridden by a subclass to returns
     * a List containing the functions to test (as the values) and the
     * names of each function ( as the keys).
     */
    protected abstract List<SimpleImmutableEntry<String, TestFunc>> getFuncsAndNames();

    /**
     * A factory method that returns an AbstractTestTask to perform
     * the tests.
     *
     * @param viewInterface       Reference to the View layer.
     * @param modelStateInterface Reference to the Model layer.
     * @param presenterLogic      Reference to the Presenter layer.
     * @param numberOfTests       Number of tests to run.
     * @return An AbstractTestTask to perform the tests.
     */
    public abstract AbstractTestTask<TestFunc> makeTestTask(ViewInterface<TestFunc> viewInterface,
                                                            ModelStateInterface<TestFunc> modelStateInterface,
                                                            PresenterLogic<TestFunc> presenterLogic,
                                                            int numberOfTests);

    /**
     * Set the default number to fill the on-screen prompt with.
     *
     * @return long Default Number of Runs.
     */
    public abstract long setDefaultRuns();
}
