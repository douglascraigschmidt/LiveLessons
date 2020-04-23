package edu.vandy.gcdtesttask.view;

import edu.vandy.gcdtesttask.presenter.GCDTestTaskFactory;
import edu.vandy.visfwk.view.abstracts.AbstractTestTaskFragment;
import edu.vandy.visfwk.model.abstracts.AbstractTestTaskFactory;
import edu.vandy.gcdtesttask.presenter.GCDInterface;

/**
 * Fragment that acts as the View in the MVP pattern.
 */
public class TestTaskFragment
       extends AbstractTestTaskFragment<GCDInterface> {
    /**
     * Get the task factory used to create TaskTuple(s).
     *
     * @return The concrete instance of AbstractTestTaskFactory to use
     * for this app, which creates and tests various GCD
     * implementations using the CyclicBarrier barrier synchronizer.
     */
    public AbstractTestTaskFactory<GCDInterface> makeTaskFactory() {
        return new GCDTestTaskFactory();
    }
}
