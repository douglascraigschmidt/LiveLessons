import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * @class PalantirManagerTest
 *
 * @brief This program demonstrates the use of the LeasePool resource
 *        manager that limits the number of Beings from Middle-Earth
 *        who can concurrently gaze into a Palantir (see
 *        http://en.wikipedia.org/wiki/Palantir for more information
 *        on Palantirs if you're not a Lord of the Ring's fan yet
 *        ;-)).
 */
public class PalantirManagerTest {
    /**
     * If this is set to true then lots of debugging output will be
     * generated.
     */
    public static boolean diagnosticsEnabled = false;

    /**
     * Keep track of whether a runtime exception occurs.
     */
    static volatile boolean mFailed = false;

    /**
     * LeasePool that controls access to the available Palantiri.
     */
    static LeasePool<Palantir> mPalantirManager;

    /**
     * This factory helper method makes a Runnable that does
     * everything a Being Thread should do. A Being is identified by
     * it index in the List of Beings.
     *
     * @param index The index of the Being in the List.
     * @return A Runnable for a Being.
     */
    private static Runnable makeBeingRunnable
                                (final int beingIndex,
                                 final CyclicBarrier entryBarrier,
                                 final CountDownLatch exitBarrier) {
        return new Runnable() {
            /**
             * This is the main loop run by each Being who wants to
             * gaze into a Palantir.
             */
            @Override
            public void run() {
                // Wait for all Threads to reach this point before
                // letting any of them run.
                try {
					entryBarrier.await();
				} catch (Exception e) {
					printDebugging("run() received exception");
				}

                // Bound the total number of iterations that each
                // Being can gaze into a Palantir.
                for (int i = 0; i < Options.instance().gazingIterations(); ++i) {

                    // Get access to a Palantir for LEASE_DURATION
                    // milliseconds. This call blocks until a
                    // Palantir is available.
                    Palantir palantir =
                        mPalantirManager.acquire
                        (Options.instance().leaseDuration());

                    printDebugging(Thread.currentThread().getName()
                                   + " is starting to gaze at palantir " 
                                   + palantir.getId()
                                   + " for "
                                   + palantir.gazeDuration()
                                   + " milliseconds");

                    try {
                        // Gaze at the Palantir for the alloted
                        // time.
                        palantir.gaze();
                    } catch (InterruptedException e) {
                        printDebugging(Thread.currentThread().getName()
                                       + " gazing interrupted since lease expired"
                                       + palantir.getId());
                    }
                        
                    // Check to see if the lease has expired.
                    long remainingDuration = mPalantirManager.remainingLeaseDuration(palantir);
                    if (remainingDuration <= 0)
                        printDebugging(Thread.currentThread().getName()
                                       + " lease has expired for palantir "
                                       + palantir.getId());
                    else
                        printDebugging(Thread.currentThread().getName()
                                       + " lease is "
                                       + remainingDuration
                                       + " milliseconds for palantir "
                                       + palantir.getId());

                    // Return the Palantir back to the LeasePool
                    // so other Beings can gaze at it.
                    mPalantirManager.release(palantir);
                }
                // Indicate this Thread is done.
                exitBarrier.countDown();
            }
        };
    }

    /**
     * Generate a List of Palantiri with random gaze times between 1
     * and 5 seconds.  Each Palantir's id is its position in the List.
     */
    static List<Palantir> makePalantiri(int numberOfPalantiri) {
        // Create a list to hold the generated Palantiri.
        List<Palantir> palantiri = new ArrayList<Palantir>();

        // Create a new Random number generator.
        final Random rand = new Random();

        // Create and add each new Palantir into the list.
        for (int i = 0; i < numberOfPalantiri; ++i) {
            // Create the id, which is its position in the List.
            final int id = i;

            // Create sleep duration here so each test run by this
            // driver program have the same random sleep times.
            final int sleepDuration = rand.nextInt(4000) + 1000;

            // Create a new Palantir that can be gazed at for a random
            // amount of time between 1 and 5 seconds.
            palantiri.add(new Palantir() {
                    // Gaze into the Palantir (and go into a trance ;-)).
                    @Override
                    public void gaze() throws InterruptedException {
                        Thread.sleep(sleepDuration);
                    }

                    // The amount of time the gazing will occur.
                    @Override
                    public long gazeDuration() {
                        return sleepDuration;
                    }

                    // Return the Palantir's id.
                    @Override
                    public int getId() {
                        return id + 1;
                    }
                });
        }
        
        return palantiri;
    }

    /**
     * This method starts all Threads in the List of Being Threads.
     */
    private static void startBeingThreads(int beingCount,
                                          CyclicBarrier entryBarrier,
                                          CountDownLatch exitBarrier) {
        // Start all Beings Threads that gaze into the Palantiri.
        for (int i = 0; i < beingCount; ++i) {
            Thread thread = new Thread(makeBeingRunnable(i, 
                                                         entryBarrier,
                                                         exitBarrier),
                                       "Being-" + i);

            // Catch runtime exceptions and induce a test failure.
            thread.setUncaughtExceptionHandler
                (new Thread.UncaughtExceptionHandler() {
                        public void uncaughtException(Thread thr,
                                                      Throwable e) {
                            printDebugging(thr 
                                           + " throws exception "
                                           + e);
                            // Indicate a runtime exception occurred.
                            mFailed = true;
                        }
                    });

            // Start the Thread.
            thread.start();
        }
    }

    /**
     * Run a test for a particular synchronization strategy.
     */
    private static long runTest (List<Palantir> palantiri,
                                 int numberOfBeings,
                                 LeasePool.SyncStrategy syncStrategy) {
        // Create a LeasePool that controls access to the available
        // Palantiri.
        mPalantirManager = new LeasePool<Palantir>(palantiri,
                                                   syncStrategy);

        // A CyclicBarrier ensures all Threads start at once.
        CyclicBarrier entryBarrier =
            new CyclicBarrier(numberOfBeings + 1);

        // A CountDownLatch ensures all Threads exit at once.
        CountDownLatch exitBarrier =
            new CountDownLatch(numberOfBeings);

        // Create and start all Being Threads.
        startBeingThreads(numberOfBeings,
                          entryBarrier,
                          exitBarrier);

        long startTime = System.currentTimeMillis();

        try {
        	// Let all Threads start.
        	entryBarrier.await();

        	// Wait for all Threads to exit.
        	exitBarrier.await();
        } catch (Exception e) {
            printDebugging("runTest received exception");
        }
        long endTime = System.currentTimeMillis();

        // Make sure the test didn't fail via a runtime exception.
        if (mFailed)
            printDebugging("PalantirManagerTest FAILED for the"
                           + syncStrategy
                           + " strategy");
        return endTime - startTime;
    }

    /**
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (diagnosticsEnabled)
            System.out.println(output);
    }

    /**
     * Main entry point into the PalantirManagerTest.
     */
    static public void main(String[] args) {
        printDebugging("Starting PalantirManagerTest");

        // Initialize the Options singleton.
        Options.instance().parseArgs(args);

        // Get the List of available Palantiri.
        List<Palantir> palantiri =
            PalantirManagerTest.makePalantiri(Options.instance().numberOfPalantiri());

        // Run the test for each of the synchronization strategy.
        for (LeasePool.SyncStrategy syncStrategy : LeasePool.SyncStrategy.values()) {
            System.out.println("Starting "
                               + syncStrategy
                               + " test");

            // Run the test.
            long testDuration = runTest(palantiri,
                                        Options.instance().numberOfBeings(),
                                        syncStrategy);

            System.out.println("Ending "
                               + syncStrategy
                               + " test, which ran in "
                               + testDuration
                               + " milliseconds");
        }

        printDebugging("Finishing PalantirManagerTest");
    }
}
