package streamgangs;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import utils.SearchResults;

/**
 * This helper class factors out the common code used by all
 * instantiations of the StreamGang framework in the BarrierStreamGang
 * project.  It customizes the StreamGang framework to concurrently
 * search one or more arrays of input Strings for words provided in an
 * array of words to find.
 */
public class SearchStreamGangAsync
       extends SearchStreamGang {
    /**
     * Constructor initializes the data members.
     */
    protected SearchStreamGangAsync(List<String> wordsToFind,
                                    String[][] stringsToSearch) {
        super (wordsToFind, stringsToSearch);
    }

    /**
     * Hook method that must be overridden by subclasses to perform
     * the Stream processing.
     */
    protected List<List<CompletableFuture<SearchResults>>> processStream() {
        // No-op by default.
        return null; 
    }

    /**
     * Initiate the Stream processing, which uses a Java 8 stream to
     * download, process, and store images sequentially.
     */
    @Override
    protected void initiateStream() {
        // Create a new barrier for this iteration cycle.
        mIterationBarrier = new CountDownLatch(1);

        // Note the start time.
        long start = System.nanoTime();

        // Start the Stream processing.
        List<List<CompletableFuture<SearchResults>>> results = processStream();
        
        // Print the processing time.
        System.out.println(TAG + 
                           ": Done in " 
                           + (System.nanoTime() - start) / 1_000_000
                           + " msecs");

        // Print the results.
        // searchResults.stream().forEach(SearchResults::print);

        if (results != null)
        System.out.println(TAG + ": The search returned " 
                           + results.stream().mapToInt(list ->
                                                       list.stream().mapToInt(future ->
                                                                              future.join().size()).sum()).sum()
                           + " word matches for "
                           + getInput().size() 
                           + " input strings");

        // Indicate all computations in this iteration are done.
        try {
            mIterationBarrier.countDown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }

}

