package edu.vandy.gcdtesttask;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import edu.vandy.gcdtesttask.presenter.GCDCountDownLatchWorker;
import edu.vandy.gcdtesttask.presenter.GCDImplementations;
import edu.vandy.gcdtesttask.presenter.GCDInterface;
import edu.vandy.visfwk.model.TaskTuple;
import edu.vandy.visfwk.utils.ProgressReporter;

/**
 * This JUnit test evaluates the GCDCountDownLatchWorker class.
 */
public class GCDCountDownLatchTest
       implements ProgressReporter {
    /**
     * Number of iterations to run the tests.
     */
    private static int sITERATIONS = 1000;

    /**
     * This factory method returns a list containing tuples, where
     * each tuple contains the GCD function to run and the name of the
     * GCD function as a string.
     */
    private static List<TaskTuple<GCDInterface>> makeGCDTuples() {
        // Automatically generates a unique id.
        AtomicInteger uniqueId = new AtomicInteger(0);

        // Return a new list of GCD tuples that are each initialized
        // using method references.
        return Arrays.asList(new TaskTuple<GCDInterface>
                             (GCDImplementations::computeGCDIterativeEuclid,
                              "GCDIterativeEuclid",
                              uniqueId.getAndIncrement()),
                             new TaskTuple<GCDInterface>
                             (GCDImplementations::computeGCDRecursiveEuclid,
                              "GCDRecursiveEuclid",
                              uniqueId.getAndIncrement()),
                             new TaskTuple<GCDInterface>
                             (GCDImplementations::computeGCDBigInteger,
                              "GCDBigInteger",
                              uniqueId.getAndIncrement()),
                             new TaskTuple<GCDInterface>
                             (GCDImplementations::computeGCDBinary,
                              "GCDBinary",
                              uniqueId.getAndIncrement()));
    }

    /**
     * Main entry point that tests the GCDCountDownLatchTester class.
     */
    @Test
    public void testGCDCountDownLatchTester()
        throws InterruptedException {
        // Initialize the input arrays.
        GCDCountDownLatchWorker.initializeInputs(sITERATIONS);

        // Make the list of GCD tuples.
        List<TaskTuple<GCDInterface>> gcdTests = makeGCDTuples();

        // Create an entry barrier that ensures all threads start at
        // the same time.
        CountDownLatch entryBarrier =
                new CountDownLatch(1);

        // Create an exit barrier that ensures all threads end at the
        // same time.
        CountDownLatch exitBarrier =
                new CountDownLatch(gcdTests.size());

        // Iterate through all the GCD tuples and start a new
        // thread to run GCDCountDownLatchTest for each one.
        gcdTests
                .forEach(gcdTuple ->
                        new Thread(new GCDCountDownLatchWorker
                                // All threads share the entry and
                                // exit barriers.
                                (entryBarrier,
                                        exitBarrier,
                                        gcdTuple,
                                        this)).start());

        System.out.println("Starting GCD tests for CountDownLatch");

        // Other threads wait until the main thread let's them run.
        entryBarrier.countDown();

        System.out.println("Waiting for results from CountDownLatch");

        // Wait until all threads are finished running.
        exitBarrier.await();

        System.out.println("All threads are done for CountDownLatch");
    }
}
