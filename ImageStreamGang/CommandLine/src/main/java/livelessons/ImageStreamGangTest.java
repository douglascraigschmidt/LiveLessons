package livelessons;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import livelessons.streams.ImageStreamCompletableFuture1;
import livelessons.streams.ImageStreamCompletableFuture2;
import livelessons.streams.ImageStreamGang;
import livelessons.streams.ImageStreamParallel;
import livelessons.streams.ImageStreamSequential;
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
     * Enumerated type that lists all the implementation strategies to test.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        PARALLEL_STREAM,
        COMPLETABLE_FUTURES_1,
        COMPLETABLE_FUTURES_2,
    }
    
    /**
     * Array of Filters to apply to the images.
     */
    private final static Filter[] mFilters = {
        new NullFilter(),
        new GrayScaleFilter()
    };

    /**
     * Keep track of the timing results of the ImageStreamGang
     * implementation strategies so they can be sorted and displayed
     * when the program is finished.
     */
    private static Map<String, List<Long>> mResultsMap = new HashMap<>();

    /**
     * The JVM requires a static main() entry point to run the console version of
     * the ImageStreamGang app.
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
    private static void runTests() {
        // Iterate through all the implementation strategies and test
        // them.
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test); 

            // Create an Iterator that contains all the image URLs to
            // obtain and process.
            Iterator<List<URL>> urlIterator = 
                Options.instance().getUrlIterator();

            // Delete any the filtered images from the previous run.
            deleteFilteredImages();

            // Make an ImageStreamGang object via the factory method.
            ImageStreamGang streamGang =
                makeImageStreamGang(mFilters,
                                    urlIterator,
                                    test);

            // Start running the test.
            streamGang.run();

            // Store the execution times.
            mResultsMap.put(test.toString(), streamGang.executionTimes());

            // Run the garbage collector to avoid perturbing the test.
            System.gc();

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
        
        // This local class is needed to make the Java compiler happy.
        final class ResultMap extends TreeMap<Long, String> {}
        
        // Create a list of TreeMaps to hold the timing results in
        // sorted order.
        List<ResultMap> listOfMaps = 
            Stream.generate(ResultMap::new)
            .limit(numberOfRuns)
            .collect(toList());
        
        // Initialize the TreeMaps to contain the results from each
        // timing test.
        IntStream.range(0, numberOfRuns)
            // Iterate through each of the test runs.
            .forEach(treeIndex ->
                     // Get the entry set from the map.
                     resultsMap.entrySet()
                                                         
                     // Iterate through each entry in the map.
                     .forEach(entry -> {
                                   // Get the appropriate tree map.
                                   ResultMap map = listOfMaps.get(treeIndex);

                                   // Store results into the tree map,
                                   // whose key is time in msecs and
                                   // whose value is test that ran.
                                   map.put(entry.getValue()
                                           .get(treeIndex),
                                           entry.getKey());
                          }));

        // Print the results of the test runs from fastest to slowest.
        IntStream.range(0, numberOfRuns)
            // Iterate through each of the test runs.
            .forEach(treeIndex -> {
                    System.out.println("\nPrinting results for input " 
                                       + (treeIndex + 1)
                                       + " from fastest to slowest");
                    listOfMaps
                        // Get the appropriate TreeMap.
                        .get(treeIndex)
                        
                        // Get the entry set from the map.
                        .entrySet()

                        // Print results of test run with name of the
                        // test first followed by time in msecs.
                        .forEach (entry ->
                                  System.out.println("" 
                                                     + entry.getValue() 
                                                     + " executed in " 
                                                     + entry.getKey() 
                                                     + " msecs"));
                });
    }
}
