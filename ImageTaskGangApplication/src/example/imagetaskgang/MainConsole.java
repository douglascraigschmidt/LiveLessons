package example.imagetaskgang;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import example.imagetaskgang.filters.Filter;
import example.imagetaskgang.filters.GrayScaleFilter;
import example.imagetaskgang.filters.NullFilter;

/**
 * @class MainConsole
 *
 * @brief This class is the main entry point for a Java console
 *        version of the ImageTaskGang application.
 */
public class MainConsole {
    /**
     * The JVM requires the instantiation of a main() method to run
     * the console version of the ImageTaskGang application.
     */
    public static void main(String[] args) {
        /**
         * Array of Filters to apply to the downloaded images.
         */
        final Filter[] filters = {
            new NullFilter(),
            new GrayScaleFilter()
        };

        // Initializes the Platform singleton with the appropriate
        // PlatformStrategy, which in this case will be the
        // ConsolePlatform.
        PlatformStrategy.instance
            (new PlatformStrategyFactory
             (System.out).makePlatformStrategy());

        PlatformStrategy.instance().errorLog("MainConsole", 
                                             "Starting ImageTaskGangTest");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Create an exit barrier with a count of one to synchronize
        // with the completion of image downloading, processing, and
        // storing in the TaskGang.
        final CountDownLatch mExitBarrier = 
            new CountDownLatch(1);

        // Create a completion hook that decrements the exit barrier
        // by one so its count equals 0.
        final Runnable completionHook = 
            new Runnable() {
                @Override
                public void run() {
                    // Cause the main Thread to return from the
                    // blocking await() call on the exit barrier
                    // below.
                    mExitBarrier.countDown();
                }
            };

        // Create an Iterator that contains all the image URLs to
        // download and process.
        Iterator<List<URL>> urlIterator = 
            PlatformStrategy.instance().getUrlIterator
            (PlatformStrategy.instance().getInputSource
             (Options.instance().getInputSource()));

        // Create an anonymous Thread to run a new instance of
        // ImageTaskGang.
        new Thread(new ImageTaskGang(filters,
                                     urlIterator,
                                     completionHook)).start();

        try {
            // Exit barrier synchronizer waits for the ImageTaskGang
            // to finish all its processing.
            mExitBarrier.await();
        } catch (InterruptedException e) {
            PlatformStrategy.instance().errorLog("MainConsole", 
                                                 "await interrupted");
        }

        PlatformStrategy.instance().errorLog("MainConsole", 
                                             "Ending ImageTaskGangTest");
    }
}
