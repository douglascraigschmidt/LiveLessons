import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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
     * Defines a pool of Threads that allow Beings to gaze at a fixed
     * number of Palantiri.
     */
    private static ExecutorService mExecutorService;

    /**
     * A ThreadFactory object that spawns an appropriately named
     * Thread for each Being.
     */
    private static ThreadFactory mThreadFactory = 
        new ThreadFactory() {
            /**
             * Give each Being a uniquely numbered name.
             */
            private final AtomicInteger mCount = new AtomicInteger(1);

            /**
             * Construct a new Thread.
             */
            public Thread newThread(Runnable r) {
                // Create a new Thread whose name uniquely identifies
                // each Being.
                return new Thread(r,
                                  "Being-" + mCount.getAndIncrement());
            }
        };

    /**
     * LeasePool that controls the access of multiple Middle-Earth
     * Beings to a fixed number of available Palantiri.
     */
    private static LeasePool<Palantir> mPalantirManager;

    /**
     * Factory method that generates a List of Palantiri with random
     * gaze times between 1 and 5 seconds.  Each Palantir's id is its
     * position in the List.
     */
    private static List<Palantir> makePalantiri(int numberOfPalantiri) {
        // Create a list to hold the generated Palantiri.
        List<Palantir> palantiri = new ArrayList<Palantir>();

        // Create a new Random number generator.
        final Random rand = new Random();

        // Create and add each new Palantir into the list.
        for (int i = 0; i < numberOfPalantiri; ++i) {
            // Create the id, which is its position in the List.
            final int id = i;

            // Create gaze duration here so each test run by this
            // driver program have the same random sleep times.
            final int gazeDuration = rand.nextInt(4000) + 1000;

            // Create a new Palantir that can be gazed at for a random
            // amount of time between 1 and 5 seconds.
            palantiri.add(new Palantir() {
                    // Gaze into the Palantir for the given gaze
                    // duration (and go into a trance ;-)).
                    @Override
                    public void gaze() throws InterruptedException {
                        Thread.sleep(gazeDuration);
                    }

                    // The amount of time the gazing will occur.
                    @Override
                    public long gazeDuration() {
                        return gazeDuration;
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
     * Print debugging output if @code diagnosticsEnabled is true.
     */
    private static void printDebugging(String output) {
        if (Options.instance().diagnosticsEnabled())
            System.out.println(output);
    }

    /**
     * Main entry point into the PalantirManagerTest.
     */
    static public void main(String[] args) {
        printDebugging("Starting PalantirManagerTest");

        // Initialize the Options singleton.
        Options.instance().parseArgs(args);

        // Create a ThreadPoolExecutor that runs the Being tasks.
        mExecutorService =
            Executors.newFixedThreadPool
                (Options.instance().numberOfBeings(),
                 mThreadFactory);

        // Create a List with the designated number of Palantiri.
        List<Palantir> palantiri =
            makePalantiri(Options.instance().numberOfPalantiri());

        // Run the test for each of the synchronization strategy.
        for (LeasePool.SyncStrategy syncStrategy : LeasePool.SyncStrategy.values()) {
            System.out.println("Starting "
                               + syncStrategy
                               + " test");
            // Run a test where a given number of Beings concurrently
            // gaze into a smaller number of Palantiri that are
            // configured with a designated synchronization strategy.
            runTest(palantiri,
                    Options.instance().numberOfBeings(),
                    syncStrategy);
            System.out.println("Ending "
                               + syncStrategy
                               + " test");
        }

        printDebugging("Finishing PalantirManagerTest");
    }

    /**
     * Run a test where a given number of Beings concurrently gaze
     * into a smaller number of Palantiri that are configured with a
     * designated synchronization strategy.
     */
    private static void runTest (List<Palantir> palantiri,
                                 int numberOfBeings,
                                 LeasePool.SyncStrategy syncStrategy) {
        // Create a LeasePool that is used to control concurrent
        // access to the List of Palantiri.
        mPalantirManager = new LeasePool<Palantir>(palantiri,
                                                   syncStrategy);

        // A CyclicBarrier ensures all Threads start as a group.
        CyclicBarrier entryBarrier =
            new CyclicBarrier(numberOfBeings + 1);

        // A CountDownLatch ensures all Threads exit as a group.
        CountDownLatch exitBarrier =
            new CountDownLatch(numberOfBeings);

        // Create and start Threads for all the Beings so they can run
        // the gazing logic, which attempts to acquire a lease on a
        // Palantir and gaze into it.
        beginBeingGazing(numberOfBeings,
                         entryBarrier,
                         exitBarrier);

        try {
            // Allow all the Being Threads to run the gazing logic.
            entryBarrier.await();

            // Wait for all Being Threads to stop gazing.
            exitBarrier.await();
        } catch (Exception e) {
            printDebugging("runTest received exception");
        }
    }

    /**
     * Create and start Threads for all the Beings so they can attempt
     * to acquire a lease on a Palantir and gaze into it.
     */
    private static void beginBeingGazing(int beingCount,
                                         CyclicBarrier entryBarrier,
                                         CountDownLatch exitBarrier) {
        // Start all Beings tasks that gaze into the Palantiri.
        for (int i = 0; i < beingCount; ++i) {
            mExecutorService.execute(makeBeingRunnable(i, 
                                                       entryBarrier,
                                                       exitBarrier));
        }
    }

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
             * This hook method performs the algorithm used by each
             * Being who wants to gaze into a Palantir.
             */
            @Override
            public void run() {
                // Wait for all Being Threads to reach this point
                // before letting any of them proceed.
                try {
                    entryBarrier.await();
                } catch (Exception e) {
                    printDebugging("run() received exception");
                }

                // Iterate for the designated number of times each
                // Being can gaze into a Palantir.
                for (int i = 0; i < Options.instance().gazingIterations(); ++i) {

                    // Get access to a Palantir for the given number
                    // of milliseconds. This call will block until a
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
                    long remainingDuration =
                        mPalantirManager.remainingLeaseDuration(palantir);
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

                // Indicate to the main Thread that this Being Thread
                // has finished.
                exitBarrier.countDown();
            }
        };
    }
}
