package edu.vandy.gcdtesttask;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import edu.vandy.visfwk.model.TaskTuple;
import edu.vandy.visfwk.utils.ProgressReporter;
import edu.vandy.gcdtesttask.presenter.GCDCyclicBarrierTester;
import edu.vandy.gcdtesttask.presenter.GCDImplementations;
import edu.vandy.gcdtesttask.presenter.GCDInterface;

/**
 * This JUnit test evaluates the GCDCyclicBarrierTest class.
 */
public class GCDCyclicBarrierTest 
       implements ProgressReporter {
    /**
     * Number of times to iterate, which is 100 million to ensure the
     * program runs for a while.
     */
    private static final int sITERATIONS = 100000000;

    /**
     * Number of cycles to run with the CyclicBarrier.
     */
    private static final int sCYCLES = 2;

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
     * Main entry point that tests the GCDCyclicBarrierTester class.
     */
    @Test
    public void testGCDCyclicBarrierTester()
        throws BrokenBarrierException, InterruptedException {
        // Make the list of GCD tuples.
        List<TaskTuple<GCDInterface>> gcdTests = makeGCDTuples();

        // Create an entry barrier that ensures all threads start at
        // the same time.  We add a "+ 1" for the thread that
        // initializes the tests.
        CyclicBarrier entryBarrier =
            new CyclicBarrier(gcdTests.size() + 1,
                              // Barrier action (re)initializes the test data.
                              () -> GCDCyclicBarrierTester.initializeInputs(sITERATIONS));

        // Create an exit barrier that ensures all threads end at the
        // same time.  We add a "+ 1" for the thread that waits for
        // the tests to complete.
        CyclicBarrier exitBarrier =
            new CyclicBarrier(gcdTests.size() + 1);

        // Iterate for each cycle.
        for (int cycle = 1; cycle <= sCYCLES; cycle++) {

            // Iterate through all the GCD tuples and start a new
            // thread to run GCDCyclicBarrierTest for each one.
            gcdTests.forEach(gcdTuple
                             -> new Thread(new GCDCyclicBarrierTester
                                           // All threads share the
                                           // entry and exit barriers.
                                           (entryBarrier,
                                            exitBarrier,
                                            gcdTuple,
                                            this)).start());

            System.out.println("Starting GCD tests for cycle "
                               + cycle);

            // Wait until all threads are ready to run.
            entryBarrier.await();     
            System.out.println("Waiting for results from cycle "
                               + cycle);

            // Wait until all threads are finished running.
            exitBarrier.await();
            System.out.println("All threads are done for cycle "
                               + cycle);
        }
    }
}
