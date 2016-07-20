import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import streams.ImageStreamCompletableFuture1;
import streams.ImageStreamCompletableFuture2;
import streams.ImageStreamGang;
import streams.ImageStreamParallel;
import streams.ImageStreamSequential;
import utils.Options;
import filters.Filter;
import filters.GrayScaleFilter;
import filters.NullFilter;

/**
 * This class is the main entry point for a Java console version of
 * the ImageStreamGang application.
 */
public class ImageStreamGangTest {
    /**
     * Enumerate the tests to run.
     */
    enum TestsToRun {
        SEQUENTIAL_STREAM,
        PARALLEL_STREAM,
        COMPLETABLE_FUTURES_1,
        COMPLETABLE_FUTURES_2,
    }
    
    /**
     * Array of Filters to apply to the downloaded images.
     */
    private final static Filter[] mFilters = {
        new NullFilter(),
        new GrayScaleFilter()
    };

    /**
     * Keep track of which SearchStreamGang performed the best.
     */
    private static Map<String, List<Long>> mResultsMap = new HashMap<>();

    /**
     * The JVM requires the instantiation of a main() method to run
     * the console version of the ImageStreamGang application.
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
     * Create/run appropriate type of StreamGang to search for words.
     */
    private static void runTests() {
        // Iterate through all the tests and run them.
        for (TestsToRun test : TestsToRun.values()) {
            System.out.println("Starting " + test); 

            // Create an Iterator that contains all the image URLs to
            // download and process.
            Iterator<List<URL>> urlIterator = 
                Options.instance().getUrlIterator();

            // Create an exit barrier with a count of one to
            // synchronize with the completion of image downloading,
            // processing, and storing in the StreamGang.
            final CountDownLatch mExitBarrier = 
                new CountDownLatch(1);

            // Create a completion hook that decrements the exit
            // barrier by one so its count equals 0.
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

            // Clean any filter directories from the previous run.
            clearFilterDirectories();

            // Make the appropriate SearchStreamGang.
            ImageStreamGang streamGang =
                makeImageStreamGang(mFilters,
                                    urlIterator,
                                    completionHook,
                                    test);

            // Run the next test.
            streamGang.run();

            // Store the execution times.
            mResultsMap.put(test.toString(), streamGang.executionTimes());

            System.out.println("Ending " + test);

            // Try to run the garbage collector to avoid
            // perturbing the test itself.
            System.gc();
            try {
                // Exit barrier synchronizer waits for the ImageStreamGang
                // to finish all its processing.
                mExitBarrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Print out all the timing results.
        printTimingResults(mResultsMap);
    }

    /**
     * Factory method that creates the desired type of StreamGang
     * subclass implementation.
     */
    private static ImageStreamGang makeImageStreamGang(Filter[] filters,
                                                       Iterator<List<URL>> urlIterator,
                                                       Runnable completionHook,
                                                       TestsToRun choice) {
        switch (choice) {
        case SEQUENTIAL_STREAM:
            return new ImageStreamSequential(filters, 
                                             urlIterator,
                                             completionHook);
        case PARALLEL_STREAM:
            return new ImageStreamParallel(filters,
                                           urlIterator,
                                           completionHook);
        case COMPLETABLE_FUTURES_1:
            return new ImageStreamCompletableFuture1(filters,
                                                     urlIterator,
                                                     completionHook);
        case COMPLETABLE_FUTURES_2:
            return new ImageStreamCompletableFuture2(filters,
                                                     urlIterator,
                                                     completionHook);
        }
        return null;
    }

    /**
     * A helper method that recursively deletes files in a specified
     * directory.
     */
    private static int deleteSubFolders(String path) {
        int deletedFiles = 0;
        File currentFolder = new File(path);        
        File files[] = currentFolder.listFiles();

        if (files == null) 
            return 0;

        // Java does not allow you to delete a directory with child
        // files, so we need to write code that handles this
        // recursively.
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
     * Clears the filter directories..
     */
    public static void clearFilterDirectories() {
        int deletedFiles = 0;

        for (Filter filter : mFilters)
            deletedFiles += deleteSubFolders
                    (new File(Options.instance().getDirectoryPath(),
                            filter.getName()).getAbsolutePath());

        System.out.println(deletedFiles
                           + " previously downloaded file(s) deleted");
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
                     resultsMap
                     // Get the entry set from the map.
                     .entrySet()
                     
                     // Convert to a stream.
                     .stream()
                     
                     // Iterate through each entry in the map and
                     // store the results into the appropriate tree
                     // map, whose key is the time in msecs and whose
                     // value is the test that was run.
                     .forEach(entry ->
                              listOfMaps.get(treeIndex).put(entry
                                                            .getValue()
                                                            .get(treeIndex), 
                                                            entry.getKey())));

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
