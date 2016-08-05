import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import leasepool.LeasePool;
import utils.Options;
import utils.StreamsUtils;

/**
 * This program demonstrates the use of Java 8 CompletableFutures,
 * Streams, and a LeasePool resource manager that limits the number of
 * Beings from Middle-Earth who can concurrently gaze into Palantiri
 * (see http://en.wikipedia.org/wiki/Palantir for more information on
 * Palantiri if you're not a Lord of the Ring's fan yet ;-)).
 */
public class PalantirManagerTest {
    /**
     * Defines a pool of Threads that allow Beings to gaze at a fixed
     * number of Palantiri.
     */
    private static ExecutorService mExecutor;
    
    /**
     * Give each Being a uniquely numbered name.
     */
    private final static AtomicInteger mCount = 
    	new AtomicInteger(1);

    /**
     * A ThreadFactory object that spawns an appropriately named
     * Thread for each Being.
     */
    private static ThreadFactory mThreadFactory 
    	= (runnable) -> 
    	  // Create a new Thread whose name uniquely identifies
          // each Being.
          new Thread(runnable, 
                     "Being-" 
                     + mCount.getAndIncrement());

    /**
     * LeasePool that controls the access of multiple Middle-Earth
     * Beings to a fixed number of available Palantiri.
     */
    private static LeasePool<Palantir> mPalantiriManager;

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
     * Main entry point into the PalantiriManagerTest.
     */
    static public void main(String[] args) {
        printDebugging("Starting PalantiriManagerTest");

        // Initialize the Options singleton.
        Options.instance().parseArgs(args);

        // Create a ThreadPoolExecutor that runs the Being tasks.
        mExecutor =
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

        printDebugging("Finishing PalantiriManagerTest");
    }

    /**
     * Run a test where a given number of Beings concurrently gaze
     * into a smaller number of Palantiri that are configured with a
     * designated synchronization strategy.
     */
    private static void runTest(List<Palantir> palantiri,
                                int numberOfBeings,
                                LeasePool.SyncStrategy syncStrategy) {
        // Create a LeasePool that is used to manage concurrent access
        // to the List of Palantiri.
        mPalantiriManager = new LeasePool<Palantir>(palantiri,
                                                    syncStrategy);

        // Enable all Beings to start running the gazing logic, which
        // attempts to acquire a lease on a Palantir and gaze into it.
        CompletableFuture<List<Void>> future =
            beginBeingsGazing(numberOfBeings);

        // Wait for all Beings to stop gazing.
        future.join();
    }

    /**
     * Asynchronously run all the Beings, each of which attempts to
     * acquire a lease on a Palantir and gaze into it.
     *
     * @param beingCount Total number of Beings that want to gaze into Palantiri.
     * @return A CompletableFuture that can be used to wait for all
     *         the asynchronous operations to complete.  
     */
    private static CompletableFuture<List<Void>> beginBeingsGazing(int beingCount) {
        // Store a list of futures to asynchronous gazing operations.
    	List<CompletableFuture<Void>> listOfFutures = Stream
            // Generate beingCount asynchronous calls to
            // gazeIntoPalantir().
            .generate(() ->
                      CompletableFuture.runAsync(PalantirManagerTest::gazeIntoPalantir, 
                                                 mExecutor))
            .limit(beingCount)

            // Return a list of CompletableFutures.
            .collect(toList());

        // Return a CompletableFuture that can be used to wait for all
        // the asynchronous operations to complete.
        return StreamsUtils.joinAll(listOfFutures);
    }
                    
    /**
     * This method performs the algorithm used by each Being who wants
     * to gaze into a Palantir.
     */
    private static void gazeIntoPalantir() {
        final threadName = Thread.currentThread().getName();

        // Iterate for the designated number of times each Being can
        // gaze into a Palantir.
        IntStream.range(0, Options.instance().gazingIterations())
            .forEach(ignore -> {
                    // Get access to a Palantir for the given
                    // number of milliseconds. This call will
                    // block until a Palantir is available.
                    Palantir palantir =
                        mPalantiriManager.acquire
                        (Options.instance().leaseDuration());

                    printDebugging(threadName
                                   + " is starting to gaze at palantir " 
                                   + palantir.getId()
                                   + " for "
                                   + palantir.gazeDuration()
                                   + " milliseconds");

                    try {
                        // Gaze at Palantir for alloted time.
                        palantir.gaze();
                    } catch (InterruptedException e) {
                        printDebugging(threadName,
                                       + " gazing interrupted since lease expired"
                                       + palantir.getId());
                    }
                        
                    // Check to see if the lease has expired.
                    long remainingDuration =
                        mPalantiriManager.remainingTime(palantir);
                    if (remainingDuration <= 0)
                        printDebugging(threadName
                                       + " lease has expired for palantir "
                                       + palantir.getId());
                    else
                        printDebugging(threadName
                                       + " lease is "
                                       + remainingDuration
                                       + " milliseconds for palantir "
                                       + palantir.getId());

                    // Return Palantir back to the LeasePool
                    // so other Beings can gaze at it.
                    mPalantiriManager.release(palantir);
                });
    }
}
