package livelessons.streamgangs;

import livelessons.utils.Options;
import livelessons.utils.PhraseMatchSpliterator;
import livelessons.utils.RunTimer;
import livelessons.utils.SearchResults;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
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
       extends StreamGang<CharSequence> {
    /**
     * The list of phrases to find.
     */
    protected final List<String> mPhrasesToFind;

    /**
     * Iterator to the list of Strings to search.
     */
    private final Iterator<List<CharSequence>> mInputIterator;

    /**
     * Exit barrier that controls when the framework has completed its
     * processing.
     */
    private CountDownLatch mExitBarrier = null;
        
    /**
     * Constructor initializes the fields.
     */
    public SearchStreamGang(List<String> phrasesToFind,
                            List<List<CharSequence>> listOfListOfInputToSearch) {
        // Store the phrases to search for.
        mPhrasesToFind = phrasesToFind;

        // Create an Iterator for the array of Strings to search.
        mInputIterator = listOfListOfInputToSearch.iterator();

        // Initialize the Executor with a ForkJoinPool.
        setExecutor(Executors.newWorkStealingPool());
    }

    /**
     * Factory method that returns the next List of Strings to be
     * searched by StreamGang implementation strategies.
     */
    @Override
    protected List<CharSequence> getNextInput() {
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

        // Execute the test and time how long it runs.
        List<List<SearchResults>> results =
            RunTimer.timeRun(() -> processStream(),
                             TAG);

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
        if (Options.getInstance().isVerbose())
            printPhrases(listOfListOfSearchResults);
    }

    /**
     * Displays the phrases associated with each play.
     */
    private void printPhrases(List<List<SearchResults>> listOfListOfSearchResults) {
        // Create a map that associates phrases found in the input
        // with the titles where they were found.
        Map<String, List<SearchResults>> resultsMap = listOfListOfSearchResults
            // Convert the list of lists into a stream of lists.
            .stream()

            // Flatten the lists into a stream of SearchResults.
            .flatMap(List::stream)

            // Collect the SearchResults into a Map by their titles.
            .collect(groupingBy(SearchResults::getTitle));

        // Print out the results in the map, where each title is
        // first printed followed by a list of the indices where the
        // phrase appeared in the input.
        resultsMap.forEach((key, value)
                     -> {
                         System.out.println("Title \""
                                            + key
                                            + "\" contained");
                         // Print out the indicates for this key.
                         value.forEach(SearchResults::print);
                     });
    }

    /**
     * Looks for all instances of @code phrase in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    public SearchResults searchForPhrase(String phrase,
                                         CharSequence input,
                                         String title,
                                         boolean parallel) {
        List<SearchResults.Result> resultList =
            // Use a PhraseMatchSpliterator to add the indices of all
            // places in the inputData where phrase matches.
            StreamSupport
                // Create a stream of Results to record the indices
                // (if any) where the phrase matched the input data.
                .stream(new PhraseMatchSpliterator(input, phrase),
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
     * Return the title portion of the @a input.
     */
    public String getTitle(CharSequence input) {
        // Create a Matcher.
        Matcher m = Pattern
                // Compile a regex that matches only the first line in the input.
                .compile("(?m)^.*$")

                // Create a matcher for this pattern.
                .matcher(input);

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

}

