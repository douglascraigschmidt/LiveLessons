package livelessons.streamgangs;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamGang;
import livelessons.utils.WordMatchSpliterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

/**
 * This class factors out the common code used by all instantiations
 * of the StreamGang framework in the SearchStreamGang program.  It
 * customizes the StreamGang framework to search a list of input
 * Strings for words provided in a list of words to find.
 */
public class SearchStreamGang
       extends StreamGang<String> {
    /**
     * The list of words to find.
     */
    protected final List<String> mWordsToFind;

    /**
     * An Iterator to the list of Strings to search.
     */
    private final Iterator<List<String>> mInputIterator;

    /**
     * Exit barrier that controls when the framework has completed its
     * processing.
     */
    protected CountDownLatch mExitBarrier = null;
        
    /**
     * Constructor initializes the fields.
     */
    public SearchStreamGang(List<String> wordsToFind,
                            List<List<String>> stringsToSearch) {
        // Store the words to search for.
        mWordsToFind = wordsToFind;

        // Create an Iterator for the array of Strings to search.
        mInputIterator = stringsToSearch.iterator();

        // Initialize the Executor with a ForkJoinPool.
        setExecutor(Executors.newWorkStealingPool());
    }

    /**
     * Factory method that returns the next List of Strings to be
     * searched by StreamGang implementation strategies.
     */
    @Override
    protected List<String> getNextInput() {
        if (!mInputIterator.hasNext())
        	return null;
        else {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the Strings to search.
            return mInputIterator.next();
        }
    }

    /**
     * This template method starts the Java 8 stream processing to
     * search the list of input strings for the given words to find.
     */
    @Override
    protected void initiateStream() {
        // Create a new barrier for this iteration cycle.
        mExitBarrier = new CountDownLatch(1);

        // Start timing the test run.
        startTiming();

        // Start the stream processing.
        List<List<SearchResults>> results = processStream();
        
        // Stop timing the test run.
        stopTiming();

        System.out.println(TAG + ": The search returned " 
                           + results.stream()
                                    .mapToInt(list 
                                              -> (Integer) list.stream().mapToInt(SearchResults::size).sum())
                                    .sum()
                           + " word matches for "
                           + getInput().size() 
                           + " input strings");

        // Indicate all computations in this iteration are done.
        try {
            mExitBarrier.countDown();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } 
    }

    /**
     * Hook method that uses the CountDownLatch as an exit barrier to
     * wait for the StreamGang iteration to finish processing.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Loop for each iteration cycle of input strings.
            for (;;) {
                // Barrier synchronizer that waits until all the
                // stream processing in this iteration cycle are done.
                mExitBarrier.await();

                // Check to see if there's another List of input
                // strings available to process.
                if (setInput(getNextInput()) != null)
                    // Invoke this hook method to initialize the gang
                    // of tasks for the next iteration cycle.
                    initiateStream();
                else
                    break; // No more input, so we're done.
            } 

            // Only call the shutdown() and awaitTermination() methods
            // if we've actually got an ExecutorService (as opposed to
            // just an Executor).
            if (getExecutor() instanceof ExecutorService) {
                ExecutorService executorService = 
                    (ExecutorService) getExecutor();

                // Tell the ExecutorService to initiate a graceful
                // shutdown.
                executorService.shutdown();

                // Wait for all the tasks in the Thread pool to
                // complete.
                executorService.awaitTermination(Long.MAX_VALUE,
                                                 TimeUnit.NANOSECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Looks for all instances of @code word in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    public SearchResults searchForWord(String word,
                                       String inputData,
                                       String title) {
    	// Create SearchResults to keep track of relevant info.
        SearchResults results =
            new SearchResults(Thread.currentThread().getId(),
                              currentCycle(),
                              word,
                              title);

        // Use a WordMatchSpliterator to add the indices of all places
        // in the inputData where word matches.
        StreamSupport
            // Create a sequential stream of Integers that records the
            // indices (if any) where the word matched the input data.
            .stream(new WordMatchSpliterator(inputData, word),
                    false)
                    
            // Add any matches to the results object.
            .forEach(results::add);

        return results;
    }
    
    /**
     * Return the title portion of the @a inputData.
     */
    public String getTitle(String inputData) {
        Pattern p =
            Pattern.compile("^Act [0-9]+, Scene [0-9]+");
        Matcher m = p.matcher(inputData);
        return m.find()
            ? m.group()
            : "";
    }

    /**
     * Hook method that must be overridden by subclasses to perform
     * the Stream processing.
     */
    protected List<List<SearchResults>> processStream() {
        // No-op by default.
        return null; 
    }

    /**
     * Keeps track of how long the test has run.
     */
    private long mStartTime;

    /**
     * Keeps track of all the execution times.
     */
    private List<Long> mExecutionTimes = new ArrayList<>();

    /**
     * Start timing the test run.
     */
    public void startTiming() {
        // Note the start time.
        mStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    public void stopTiming() {
        mExecutionTimes.add((System.nanoTime() - mStartTime) / 1_000_000);
    }

    /**
     * Return the time needed to execute the test.
     */
    public List<Long> executionTimes() {
        return mExecutionTimes;
    }
}

