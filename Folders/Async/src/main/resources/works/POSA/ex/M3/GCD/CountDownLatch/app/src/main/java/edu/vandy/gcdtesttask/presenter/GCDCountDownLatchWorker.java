package edu.vandy.gcdtesttask.presenter;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import edu.vandy.visfwk.model.TaskTuple;
import edu.vandy.visfwk.utils.ProgressReporter;

/**
 * Each instance of this class runs in a thread and tests various GCDInterface
 * implementations using CountDownLatch objects.
 */
public class GCDCountDownLatchWorker
       implements Runnable {
    /**
     * String Tag for logging.
     */
    private final static String TAG =
        GCDCountDownLatchTestTask.class.getCanonicalName();

    /**
     * This entry barrier ensures the threads don't start until the
     * coordinator thread lets them begin.
     */
    private final CountDownLatch mEntryBarrier;

    /**
     * This exit barrier ensures the coordinator thread doesn't
     * complete until all the test threads complete.
     */
    private final CountDownLatch mExitBarrier;

    /**
     * This lambda contains the GCDInterface function to test.
     */
    private final GCDInterface mGcdFunction;

    /**
     * Contains the name of the GCDInterface function being tested.
     */
    final String mTestName;

    /**
     * An array of randomly generated input to use as the first
     * parameter to the GCDInterface function.
     */
    private static int[] mInputA;

    /**
     * An array of randomly generated input to use as the second
     * parameter to the GCDInterface function.
     */
    private static int[] mInputB;

    /**
     * A reference to the ProgressReporter.
     */
    private ProgressReporter mProgressReporter;

    /**
     * Constructor initializes the fields.
     */
    public GCDCountDownLatchWorker(CountDownLatch entryBarrier,
                                   CountDownLatch exitBarrier,
                                   TaskTuple<GCDInterface> gcdTuple,
                                   ProgressReporter progressReporter) {
        mEntryBarrier = entryBarrier;
        mExitBarrier = exitBarrier;
        mGcdFunction = gcdTuple.getTestFunc();
        mTestName = gcdTuple.getTestName();
        mProgressReporter = progressReporter;
    }

    /**
     * Initialize the input arrays so that all the GCDInterface functions
     * operate on the same randomly generated data.
     */
    public static void initializeInputs(int iterations) {
        System.out.println(TAG 
                           + ", calling initializeInputs() for "
                           + iterations 
                           + " iterations");

        // Create a new Random number generator.
        Random random = new Random();

        // Generate "iterations" random ints between 0 and MAX_VALUE.
        mInputA =
            random.ints(iterations,
                        0,
                        Integer.MAX_VALUE)
            .toArray();

        // Generate "iterations" random ints between 0 and MAX_VALUE.
        mInputB =
            random.ints(iterations,
                        0,
                        Integer.MAX_VALUE)
            .toArray();
    }

    /**
     * Run the GCDInterface test.
     */
    private void runTest() {
        System.out.println(TAG
                           + ", Starting test of "
                           + mTestName
                           + " in thread "
                           + Thread.currentThread());

        // Size of the array(s) of random numbers indicates how many
        // iterations to perform.
        int iterations = mInputA.length;

        // Note the start time.
        long startTime = System.nanoTime();

        // Iterate for the given # of iterations.
        for (int i = 0; i < iterations; ++i) {
            if (Thread.interrupted()) {
                System.out.println(TAG
                                   + ", Interrupt request received in runTest() for "
                                   + mTestName
                                   + " in thread "
                                   + Thread.currentThread());
                return;
            }

            // Get the next two random numbers.
            int number1 = mInputA[i];
            int number2 = mInputB[i];

            // Compute the GCDInterface of these two numbers.
            int result = mGcdFunction.compute(number1,
                                      number2);

            // Publish the mProgressStatus every 10%.
            if (((i + 1) % (iterations / 10)) == 0) {
                /*
                  System.out.println("In runTest() on iteration "
                  + i
                  + " the GCDInterface of "
                  + number1
                  + " and "
                  + number2
                  + " is "
                  + result);
                */
                // Convert to a percentage of 100.
                Double percentage =
                    ((double) (i + 1) / (double) iterations) * 100.00;

                // Publish mProgressStatus as a percentage of total
                // completion.
                mProgressReporter.updateProgress(makeReport(percentage.intValue()));
            }
        }

        // Stop timing the tests.
        long stopTime = System.nanoTime();

        // Print the results.
        System.out.println(TAG
                           + ", "
                           + (double) (stopTime - startTime) / 1000000.0
                           + " millisecond run time for "
                           + mTestName
                           + " in thread "
                           + Thread.currentThread());
    }

    /**
     * This factory method returns a runnable that will be logged.
     */
    protected Runnable makeReport(Integer percentageComplete) {
        return () -> System.out.println(TAG
                                        + ", "
                                        + percentageComplete
                                        + "% complete for "
                                        + mTestName);
    }

    /**
     * Main entry point into the GCDInterface test.
     */
    public void run() {
        try {
            // Wait for the coordinator thread to start the tests.
            mEntryBarrier.await();

            // Run the test.
            runTest();

            // Inform the coordinator thread that this test is
            // finished.
            mExitBarrier.countDown();
        } catch (Exception ex) {
            System.out.println(TAG
                               + ", exception "
                               + ex
                               + " received in run() for thread "
                               + Thread.currentThread());
        }
    }
}

