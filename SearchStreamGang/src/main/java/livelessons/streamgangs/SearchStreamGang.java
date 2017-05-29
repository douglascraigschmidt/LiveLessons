package livelessons.streamgangs;

import livelessons.utils.PhraseMatchSpliterator;
import livelessons.utils.SearchResults;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * This class factors out the common code used by all instantiations
 * of the StreamGang framework in the SearchStreamGang program.  It
 * customizes the StreamGang framework to search a list of input
 * Strings for phrases provided in a list of phrases to find.
 */
public class SearchStreamGang
       extends StreamGang<String> {
    /**
     * The list of phrases to find.
     */
    protected final List<String> mPhrasesToFind;

    /**
     * An Iterator to the list of Strings to search.
     */
    private final Iterator<List<String>> mInputIterator;

    /**
     * Exit barrier that controls when the framework has completed its
     * processing.
     */
    private CountDownLatch mExitBarrier = null;
        
    /**
     * Constructor initializes the fields.
     */
    public SearchStreamGang(List<String> phrasesToFind,
                            List<List<String>> stringsToSearch) {
        // Store the phrases to search for.
        mPhrasesToFind = phrasesToFind;

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
     * search the list of input strings for the given phrases to find.
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

        // Print the results.
        printResults(TAG, results);

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
     * Print out the search results.
     */
    private void printResults(String test,
                              List<List<SearchResults>> listOfListOfSearchResults) {
        System.out.println(test +
                           ": The search returned "
                           + listOfListOfSearchResults.stream()
                           .mapToInt(list 
                                     -> list.stream().mapToInt(SearchResults::size).sum())
                           .sum()
                           + " phrase matches for "
                           + getInput().size() 
                           + " input strings");

        // Print out the titles.
        // printTitles(listOfListOfSearchResults);
    }

    /**
     * Print out the quotes associated with each play title.
     */
    private void printTitles(List<List<SearchResults>> listOfListOfSearchResults) {
        // Create a map that associates phrases found in the input with
        // the indices where they were found.
        Map<String, List<SearchResults>> resultsMap = listOfListOfSearchResults
            // Convert the list of lists into a stream of lists.
            .stream()

            // Flatten the lists into a stream of SearchResults.
            .flatMap(List::stream)

            // Collect the SearchResults into a Map.
            .collect(groupingBy(SearchResults::getTitle));

        // Print out the results in the map, where each phrase is
        // first printed followed by a list of the indices where the
        // phrase appeared in the input.
        resultsMap
            // Get the EntrySet for the map.
            .entrySet()

            // Convert the EntrySet into a stream.
            .stream()

            // Filter out any titles that have no phrases.
            .filter(entrySet -> entrySet.getValue().size() > 0)

            // Print out the titles and their associated phrases.
            .forEach(entrySet
                     -> { System.out.println("Title \""
                                             + entrySet.getKey()
                                             + "\" contained");
                         entrySet.getValue().forEach((SearchResults sr) -> sr.print());
                     });
    }

    /**
     * Looks for all instances of @code phrase in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    public SearchResults searchForPhrase(String phrase,
                                         String inputData,
                                         String title,
                                         boolean parallel) {
        List<SearchResults.Result> resultList =
            // Use a PhraseMatchSpliterator to add the indices of all
            // places in the inputData where phrase matches.
            StreamSupport
                // Create a stream of Results to record the indices
                // (if any) where the phrase matched the input data.
                .stream(new PhraseMatchSpliterator(inputData, phrase),
                        parallel)
                    
                // This terminal operation triggers aggregate
                // operation processing and returns a list of Results.
                .collect(toList());

    	// Create/return SearchResults to keep track of relevant info.
        return new SearchResults(Thread.currentThread().getId(),
                                 currentCycle(),
                                 phrase,
                                 title,
                                 resultList);
    }
    
    /**
     * Return the title portion of the @a inputData.
     */
    public String getTitle(String inputData) {
        // This regex matches only the first line in the inputData.
        Pattern p = Pattern.compile("(?m)^.*$");

        // Create a matcher for this pattern.
        Matcher m = p.matcher(inputData);

        // Find/return the first line in the string.
        return m.find()
            ? m.group()
            : "";
        /* Could also use
          
        int index = inputData.indexOf('\n');
        return inputData.substring(0,
                                   index);
        */
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
    private void startTiming() {
        // Note the start time.
        mStartTime = System.nanoTime();
    }

    /**
     * Stop timing the test run.
     */
    private void stopTiming() {
        mExecutionTimes.add((System.nanoTime() - mStartTime) / 1_000_000);
    }

    /**
     * Return the time needed to execute the test.
     */
    public List<Long> executionTimes() {
        return mExecutionTimes;
    }
}

