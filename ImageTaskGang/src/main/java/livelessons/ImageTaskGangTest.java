package livelessons;

import livelessons.filters.Filter;
import livelessons.filters.GrayScaleFilter;
import livelessons.filters.NullFilter;
import livelessons.tasks.ImageTaskGang;
import livelessons.utils.Options;
import livelessons.utils.RunTimer;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Executors;

import static livelessons.utils.FileAndNetUtils.deleteAllFiles;

/**
 * This class is the main entry point for the Java console version of
 * the ImageTaskGang app.
 */
@SuppressWarnings("SameParameterValue")
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
        warmUpThreadPool();

        // Iterate thru the implementation strategies and test them.
        for (var test : TestsToRun.values()) {
            System.out.println("Starting " + test);

            // Delete any the filtered images from the previous run.
            deleteAllFiles(mFilters);

            // Create a list of lists that contains all the image URLs
            // to obtain and process.
            var urlLists = Options.instance()
                .getUrlLists();

            // Make an ImageTaskGang object via the factory method.
            ImageTaskGang taskGang = makeImageTaskGang(mFilters,
                                                       urlLists,
                                                       test);

            // Run the garbage collector here to avoid perturbing the
            // test.
            System.gc();

            // Start running the test.
            RunTimer
                .timeRun(taskGang,
                         test.toString());

            System.out.println("Ending " + test);
        }

        // Print out the timing results.
        System.out.println(RunTimer.getTimingResults());
    }

    /**
     * Factory method that creates the designated type of
     * ImageTaskGang subclass implementation.
     */
    private static ImageTaskGang makeImageTaskGang
        (Filter[] filters,
         List<List<URL>> urlLists,
         TestsToRun choice) {
        return switch (choice) {
        case EXECUTOR_COMPLETION_SERVICE_CACHED ->
            new ImageTaskGang
            (filters,
             urlLists,
             choice.toString(),
             // Create an Executor with a cached pool of Thread
             // objects, which grow and shrink dynamically as new
             // tasks are executed.
             Executors.newCachedThreadPool());
        case EXECUTOR_COMPLETION_SERVICE_FIXED ->
            new ImageTaskGang
            (filters,
             urlLists,
             choice.toString(),
             Executors
             // Create an Executor with a fixed pool of threads
             .newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
        };
    }

    /**
     * Warm up the threads in the thread pool so the timing results
     * will be more accurate.
     */
    private static void warmUpThreadPool() {
        // Delete any the filtered images from the previous run.
        deleteAllFiles(mFilters);

        // Create and run the ImageTaskGang test with a fixed-sized
        // thread pool to warm up the threads in pool.
        ImageTaskGang taskGang =
            new ImageTaskGang
            (mFilters,
             Options.instance().getUrlLists(),
                "",
                Executors
                    .newFixedThreadPool(Runtime.getRuntime().availableProcessors()));

        taskGang.run();
    }
}
