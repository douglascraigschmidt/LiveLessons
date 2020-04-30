package livelessons;

import java.net.URL;
import java.util.*;

import livelessons.streams.*;
import livelessons.utils.FileUtils;
import livelessons.utils.Options;
import livelessons.filters.Filter;
import livelessons.filters.GrayScaleFilter;
import livelessons.filters.NullFilter;

/**
 * This class is the main entry point for the Java console version of
 * the ImageStreamGang app.
 */
public class ImageStreamGangTest {
    /**
     * Enumerated type that lists all the implementation strategies to
     * test.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        PARALLEL_STREAM,
        COMPLETABLE_FUTURES_1,
        COMPLETABLE_FUTURES_2,
        RXJAVA1, 
        RXJAVA2
    }
    
    /**
     * Array of Filters to apply to the images.
     */
    private final static Filter[] mFilters = {
        new NullFilter(),
        new GrayScaleFilter()
        // Other filters can go here..
    };

    /**
     * Keep track of the timing results of the ImageStreamGang
     * implementation strategies so they can be sorted and displayed
     * when the program is finished.
     */
    private static Map<String, List<Long>> mResultsMap = new HashMap<>();

    /**
     * The JVM requires a static main() entry point to run the console
     * version of the ImageStreamGang app.
     */
    public static void main(String[] args) {
        System.out.println("Starting ImageStreamGangTest");

        // Initializes the Options singleton.
        Options.instance().parseArgs(args);

        // Run all the tests.
        runTests();

        System.out.println("Ending ImageStreamGangTest");
    }

    /**
     * Iterates through all the implementation strategies to test how
     * they perform.
     */
    public static void runTests() {
        // Warm up the fork-join pool.
        warmUpForkJoinPool();

        // Iterate thru the implementation strategies and test them.
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test);

            // Delete any the filtered images from the previous run.
            FileUtils.deleteAllFiles(mFilters);

            // Make an ImageStreamGang object via the factory method.
            ImageStreamGang streamGang =
                makeImageStreamGang(mFilters,
                                    Options.instance().getUrlIterator(),
                                    test);

            // Run garbage collector first to avoid perturbing test timing.
            System.gc();

            // Start running the test (which initiates the timer).
            streamGang.run();

            // Store the execution times for this test run.
            mResultsMap.put(test.toString(),
                            streamGang.executionTimes());

            System.out.println("Ending " + test);
        }

        // Print out all the timing results.
        printTimingResults(mResultsMap);
    }

    /**
     * Factory method that creates the designated type of
     * ImageStreamGang subclass implementation.
     */
    private static ImageStreamGang makeImageStreamGang(Filter[] filters,
                                                       Iterator<List<URL>> urlIterator,
                                                       TestsToRun choice) {
        switch (choice) {
        case SEQUENTIAL_STREAM:
            return new ImageStreamSequential(filters, 
                                             urlIterator);
        case PARALLEL_STREAM:
            return new ImageStreamParallel(filters,
                                           urlIterator);
        case COMPLETABLE_FUTURES_1:
            return new ImageStreamCompletableFuture1(filters,
                                                     urlIterator);
        case COMPLETABLE_FUTURES_2:
            return new ImageStreamCompletableFuture2(filters,
                                                     urlIterator);
        case RXJAVA1:
            return new ImageStreamRxJava1(filters,
                                         urlIterator);
        case RXJAVA2:
                return new ImageStreamRxJava2(filters,
                           urlIterator);
        }
        return null;
    }
    
    public static Map<String, List<Long>>  getTimingResults(){
    	return mResultsMap;
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
                .sorted(Map.Entry.comparingByKey())

                // Print all the entries in the sorted stream.
                .forEach(entry
                         -> System.out.println(""
                                               + entry.getValue()
                                               + " executed in "
                                               + entry.getKey()
                                               + " msecs"));
        }
    }

    /**
     * Warm up the threads in the common fork-join pool so the timing
     * results will be more accurate.
     */
    private static void warmUpForkJoinPool() {
        System.out.println("Warming up the fork-join pool");

        // Delete any the filtered images from the previous run.
        FileUtils.deleteAllFiles(mFilters);

        // Create and run the ImageStreamParallel test to warm up
        // threads in the common fork-join pool.
        ImageStreamGang streamGang =
                new ImageStreamParallel(mFilters,
                                        Options.instance().getUrlIterator());

        streamGang.run();

        System.out.println("End warming up the fork-join pool");
    }
}
