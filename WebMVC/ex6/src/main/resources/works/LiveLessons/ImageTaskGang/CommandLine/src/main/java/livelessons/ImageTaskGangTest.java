package livelessons;

import java.io.File;
import java.net.URL;
import java.util.*;

import livelessons.tasks.ImageTaskCompletionServiceCached;
import livelessons.tasks.ImageTaskCompletionServiceFixed;
import livelessons.tasks.ImageTaskGang;
import livelessons.utils.Options;
import livelessons.filters.Filter;
import livelessons.filters.GrayScaleFilter;
import livelessons.filters.NullFilter;

/**
 * This class is the main entry point for the Java console version of
 * the ImageTaskGang app.
 */
public class ImageTaskGangTest {
    /**
     * Enumerated type that lists all the implementation strategies to test.
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
     * Keep track of the timing results of the ImageTaskGang
     * implementation strategies so they can be sorted and displayed
     * when the program is finished.
     */
    private static Map<String, List<Long>> mResultsMap = new HashMap<>();

    /**
     * The JVM requires a static main() entry point to run the console version of
     * the ImageTaskGang app.
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
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test); 

            // Create a list of lists that contains all the image URLs
            // to obtain and process.
            List<List<URL>> urlLists =
                Options.instance().getUrlLists();

            // Delete any the filtered images from the previous run.
            deleteFilteredImages();

            // Make an ImageTaskGang object via the factory method.
            ImageTaskGang taskGang =
                makeImageTaskGang(mFilters,
                                  urlLists,
                                  test);

            // Start running the test.
            taskGang.run();

            // Store the execution times.
            mResultsMap.put(test.toString(), 
                            taskGang.executionTimes());

            // Run the garbage collector to avoid perturbing the test.
            System.gc();

            System.out.println("Ending " + test);
        }

        // Print out all the timing results.
        printTimingResults(mResultsMap);
    }

    /**
     * Warm up the threads in the thread pool so the timing results
     * will be more accurate.
     */
    private static void warmUpThreadPool() {
        System.out.println("Warming up the thread pool");

        // Delete any the filtered images from the previous run.
        deleteFilteredImages();

        // Create and run the ImageTaskCompletionServiceFixed test to
        // warm up threads in the thread pool.
        ImageTaskGang taskGang =
            new ImageTaskCompletionServiceFixed(mFilters,
                                                Options.instance().getUrlLists());

        taskGang.run();

        // Run the garbage collector to avoid perturbing the test.
        System.gc();

        System.out.println("End warming up the thread pool");
    }

    /**
     * Factory method that creates the designated type of
     * ImageTaskGang subclass implementation.
     */
    private static ImageTaskGang makeImageTaskGang(Filter[] filters,
                                                   List<List<URL>> urlLists,
                                                   TestsToRun choice) {
        switch (choice) {
        case EXECUTOR_COMPLETION_SERVICE_CACHED:
            return new ImageTaskCompletionServiceCached(filters,
                                                        urlLists);
        case EXECUTOR_COMPLETION_SERVICE_FIXED:
            return new ImageTaskCompletionServiceFixed(filters,
                                                       urlLists);
        }

        return null;
    }

    /**
     * Clears the filter directories.
     */
    private static void deleteFilteredImages() {
        int deletedFiles = 0;

        // Delete all the filter directories.
        for (Filter filter : mFilters)
            deletedFiles += deleteSubFolders
                (new File(Options.instance().getDirectoryPath(),
                          filter.getName()).getAbsolutePath());

        System.out.println(deletedFiles
                           + " previously downloaded file(s) deleted");
    }

    /**
     * Recursively delete files in a specified directory.
     */
    private static int deleteSubFolders(String path) {
        int deletedFiles = 0;
        File currentFolder = new File(path);        
        File files[] = currentFolder.listFiles();

        if (files == null) 
            return 0;

        // Java doesn't delete a directory with child files, so we
        // need to write code that handles this recursively.
        for (File f : files) {          
            if (f.isDirectory()) 
                deletedFiles += deleteSubFolders(f.toString());
            f.delete();
            deletedFiles++;
        }

        currentFolder.delete();
        return deletedFiles;
    }

    /**
     * Print out all the timing results for all the test runs in order
     * from fastest to slowest.
     */
    private static void printTimingResults(Map<String, List<Long>> resultsMap) {
        // Determine how many runs of the tests took place.
        int numberOfRuns =
            resultsMap.entrySet().iterator().next().getValue().size();

        // Iterate through the results of each of the test runs.
        for (int i = 0;
             i < numberOfRuns;
             i++) {
            final int runNumber = i;
            System.out.println("\nPrinting "
                               + resultsMap.entrySet().size()
                               + " results for input file "
                               + (runNumber + 1)
                               + " from fastest to slowest");

            // Print out the contents of the resultsMap in sorted
            // order.
            resultsMap
                // Get the entrySet for the resultsMap.
                .entrySet()

                // Convert the entrySet into a stream.
                .stream()

                // Create a SimpleImmutableEntry containing the timing
                // results (value) followed by the test name (key).
                .map(entry
                     -> new AbstractMap.SimpleImmutableEntry<>
                        (entry.getValue().get(runNumber),
                         entry.getKey()))

                // Sort the stream by the timing results (key).
                .sorted(Comparator.comparing(AbstractMap.SimpleImmutableEntry::getKey))

                // Print all the entries in the sorted stream.
                .forEach(entry
                         -> System.out.println(""
                                               + entry.getValue()
                                               + " executed in "
                                               + entry.getKey()
                                               + " msecs"));
        }
    }
}
