package example;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import filters.Filter;
import filters.GrayScaleFilter;
import filters.NullFilter;

/**
 * @class MainConsole
 *
 * @brief This class is the main entry point for a Java console
 *        version of the ImageStream application.
 */
public class MainConsole {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM, // Uses a Java 8 sequential stream.
        PARALLEL_STREAM,   // Uses a Java 8 parallel stream.
        COMPLETABLE_FUTURE // Uses Java 8 CompletableFutures.
    }

    /**
     * Array of Filters to apply to the downloaded images.
     */
    private static Filter[] FILTERS = {
        new NullFilter(),
        new GrayScaleFilter()
    };

    /**
     * The JVM requires the instantiation of a main() method to run
     * the console version of the ImageTaskGang application.
     */
    public static void main(String[] args) {
    	
        // Initializes the Platform singleton with the appropriate
        // PlatformStrategy, which in this case will be the
        // ConsolePlatform.
        PlatformStrategy.instance
            (new PlatformStrategyFactory
             (System.out).makePlatformStrategy());

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        PlatformStrategy.instance().errorLog("MainConsole", 
                                             "Starting all the tests");

        // Run all the tests.
        for (TestsToRun test : TestsToRun.values()) {
            PlatformStrategy.instance().errorLog("MainConsole", 
                                                 "Starting "
                                                 + test);

            // Create an Iterator that contains all the image URLs to
            // download and process.
            Iterator<List<URL>> urlIterator = 
                PlatformStrategy.instance().getUrlIterator
                (PlatformStrategy.instance().getInputSource
                 (Options.instance().getInputSource()));

            // Create an exit barrier with a count of one to
            // synchronize with the completion of the image
            // downloading and processing in the TaskGang.
            final CountDownLatch exitBarrier = 
                new CountDownLatch(1);

            // Create a completion hook that decrements the exit barrier
            // by one so its count equals 0.
            final Runnable completionHook = () -> exitBarrier.countDown();

            long start = System.nanoTime();

            // Call the makeImageStream() factory method to create the
            // designated ImageStream and then run it in a separate
            // Thread.
            new Thread(makeImageStream(FILTERS, 
                                       urlIterator,
                                       completionHook,
                                       test)).start();

            try {
                // Barrier synchronizer that wait for the ImageStream
                // to finish all its processing.
                exitBarrier.await();
            } catch (InterruptedException e) {
                PlatformStrategy.instance().errorLog("MainConsole", 
                                                     "await interrupted");
            }

            long duration = (System.nanoTime() - start) / 1_000_000;

            PlatformStrategy.instance().errorLog("MainConsole", 
                                                 "Ending "
                                                 + test
                                                 + " in "
                                                 + duration 
                                                 + " msecs");
        }

        PlatformStrategy.instance().errorLog("MainConsole", 
                                             "Ending all the tests");
    }

    /**
     * Factory method that creates the designated ImageStream.
     */
    private static ImageStream makeImageStream(Filter[] filters,
                                               Iterator<List<URL>> urlIterator,
                                               Runnable completionHook,
                                               TestsToRun choice) {
        switch(choice) {
        case SEQUENTIAL_STREAM:
            return new ImageStreamSequential(filters,
                                             urlIterator,
                                             completionHook);
        case PARALLEL_STREAM:
            return new ImageStreamParallel(filters,
                                           urlIterator,
                                           completionHook);
        case COMPLETABLE_FUTURE:
            return new ImageStreamCompletableFuture(filters,
                                                    urlIterator,
                                                    completionHook);
        }
        return null;
    }
}
