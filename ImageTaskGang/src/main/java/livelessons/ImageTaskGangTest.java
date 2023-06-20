package livelessons;

import livelessons.filters.Filter;
import livelessons.filters.GrayScaleFilter;
import livelessons.filters.NullFilter;
import livelessons.tasks.ImageTaskCompletionServiceCached;
import livelessons.tasks.ImageTaskCompletionServiceFixed;
import livelessons.tasks.ImageTaskGang;
import livelessons.utils.Options;
import livelessons.utils.RunTimer;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static livelessons.utils.FileAndNetUtils.deleteAllFiles;

/**
 * This class is the main entry point for the Java console version of
 * the ImageTaskGang app.
 */
public class ImageTaskGangTest {
    /**
     * Enumerated type listing all implementation strategies to test.
     */
    enum TestsToRun {
        EXECUTOR_COMPLETION_SERVICE_CACHED,
        EXECUTOR_COMPLETION_SERVICE_FIXED,
    }
    
    /**
     * Array of Filters to apply to the images.
     */
    private final static Filter[] mFilters = {
        new NullFilter(),
        new GrayScaleFilter()
    };

    /**
     * Java requires a static main() entry point to run the console
     * version of the ImageTaskGang app.
     */
    public static void main(String[] args) {
        System.out.println("Starting ImageTaskGangTest");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Run all the tests.
        runTests();

        System.out.println("Ending ImageTaskGangTest");
    }

    /**
     * Iterates through all the implementation strategies to test how
     * they perform.
     */
    private static void runTests() {
        // Warm up the thread pool.
        // warmUpThreadPool();

        // Iterate thru the implementation strategies and test them.
        for (var test : TestsToRun.values()) {
            // Run the garbage collector first to avoid perturbing the
            // test.
            System.gc();

            // Delete any the filtered images from the previous run.
            deleteAllFiles(mFilters);

            System.out.println("Starting " + test); 

            // Create a list of lists that contains all the image URLs
            // to obtain and process.
            var urlLists = Options.instance().getUrlLists();

            // Make an ImageTaskGang object via the factory method.
            ImageTaskGang taskGang = RunTimer
                .timeRun(() -> makeImageTaskGang(mFilters,
                                                 urlLists,
                                                 test),
                         test.toString());

            // Start running the test.
            taskGang.run();

            System.out.println("Ending " + test);
        }

        // Print out the timing results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Factory method that creates the designated type of
     * ImageTaskGang subclass implementation.
     */
    private static ImageTaskGang makeImageTaskGang(Filter[] filters,
                                                   List<List<URL>> urlLists,
                                                   TestsToRun choice) {
        return switch (choice) {
        case EXECUTOR_COMPLETION_SERVICE_CACHED ->
            new ImageTaskCompletionServiceCached(filters,
                                                 urlLists);
        case EXECUTOR_COMPLETION_SERVICE_FIXED ->
            new ImageTaskCompletionServiceFixed(filters,
                                                urlLists);
        };

    }

    /**
     * Warm up the threads in the thread pool so the timing results
     * will be more accurate.
     */
    private static void warmUpThreadPool() {
        System.out.println("Warming up the thread pool");

        // Delete any the filtered images from the previous run.
        deleteAllFiles(mFilters);

        // Create and run the ImageTaskCompletionServiceFixed test to
        // warm up threads in the thread pool.
        ImageTaskGang taskGang =
            new ImageTaskCompletionServiceFixed
            (mFilters,
             Options.instance().getUrlLists());

        taskGang.run();

        // Run the garbage collector to avoid perturbing the test.
        System.gc();

        System.out.println("End warming up the thread pool");
    }
}
