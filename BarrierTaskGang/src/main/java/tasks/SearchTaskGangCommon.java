package tasks;

import utils.SearchResults;
import utils.TaskGang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static utils.Options.print;
import static utils.Options.printDebugging;

/**
 * This helper class factors out the common code used by all
 * instantiations of the {@link TaskGang} framework in the
 * BarrierTaskGang project.  It customizes the {@link TaskGang}
 * framework to concurrently search one or more arrays of input
 * Strings for words provided in an array of words to find.
 */
public abstract class SearchTaskGangCommon
                extends TaskGang<String> {
    /**
     * The array of words to find.
     */
    protected final String[] mWordsToFind;

    /**
     * An Iterator for the array of Strings to search.
     */
    private final Iterator<String[]> mInputIterator;

    /**
     * Exit barrier that controls when the framework and test program
     * complete all concurrent processing.
     */
    protected CountDownLatch mExitBarrier;
        
    /**
     * Constructor initializes the fields.
     */
    protected SearchTaskGangCommon(String[] wordsToFind,
                                   String[][] stringsToSearch) {
        // Store the words to search for.
        mWordsToFind = wordsToFind;

        // Create an Iterator for the List of Strings to search.
        mInputIterator = Arrays.asList(stringsToSearch).iterator();
    }

    /**
     * @return The next {@link List} of {@link String} objects for the
     *         task gang to search concurrently.
     */
    @Override
    protected List<String> getNextInput() {
        // Return null if all input has been processed.
        if (!mInputIterator.hasNext())
            return null;
        else {
            // We're starting a new cycle.
            incrementCycle();

            // Return a List containing the String objects to search
            // concurrently.
            return Arrays.asList(mInputIterator.next());
        }
    }

    /**
     * Hook method that initiates the gang of tasks.
     */
    @Override
    protected void initiateTaskGang(int size) {
    	// Hook method called back to perform custom initializations
    	// before the Threads in the gang are spawned.
        initiateHook(size);

        // Create and start a Thread for each element in the input
        // List.  Each Thread performs the processing designated by
        // the processInput() hook method defined above.
        for (int i = 0; i < size; ++i) 
            new Thread(makeTask(i)).start();
    }

    /**
     * Run in a background {@link Thread} and search {@code inputData}
     * for all occurrences of the words to find.
     */
    @Override
    protected boolean processInput (String inputData) {
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        for (String word : mWordsToFind) {
            SearchResults results = searchForWord(word,
                                                  inputData);
            // Each time a match is found, the SearchResult.print()
            // method is called to print the output.  We put this call
            // in a synchronized statement, so the output isn't all
            // scrambled up in different Threads.
            synchronized(System.out) {
                results.print();
            }
        }

        return true;
    }

    /**
     * @return A {@link Runnable} task that processes one data element
     * at location {@code index} of the underlying input {@link List}
     * in a background task run by a Java {@link Thread}.
     */
    protected Runnable makeTask(final int index) {
        // This method runs in a background task provided by a Java
        // Thread.
        return () -> {
            // Since this task runs in a distinct Thread, it can block
            // in a loop until all iteration cycles are done.
            do {
                try {
                    // Get the input data element associated with
                    // this index.
                    String element = getInput().get(index);

                    // Process the input data element.
                    if (processInput(element))
                        // Success indicates the Thread is
                        // done with this iteration cycle.
                        taskDone(index);
                    else
                        // A problem occurred, so exit the loop and
                        // return.
                        return;
                } catch (IndexOutOfBoundsException e) {
                    printDebugging("Thread " 
                                   + e.getMessage()
                                   + " is shutdown");

                    // If an exception occurred, then return from this
                    // Thread.
                    return;
                }

                // Keep looping until all iteration cycles are done.
            } while (advanceTaskToNextCycle());
        };
    }

    /**
     * Hook method that uses the {@link CountDownLatch} as an exit
     * barrier to wait for the gang of tasks to exit.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Block the calling task until all tasks are done.
            mExitBarrier.await();
        } catch (InterruptedException ignored) {
        }
    }

    /**
     * Search for all instances of {@code word} in {@code inputData}
     * and return a {@link List} of all {@link SearchResults} (if
     * any).
     */
    protected SearchResults searchForWord(String word,
                                          String inputData) {
        // Return the SearchResults.
        return new SearchResults
            (Thread.currentThread().threadId(),
             currentCycle(),
             word,
             inputData,
             makeResultsRegex(word, inputData));
    }

    /**
     * Use Java regular expressions to make a {@link List} of {@link
     * SearchResults.Result} objects for the {@code word} to search
     * for in the {@code inputData}.
     * 
     * @param word The word to search for
     * @param inputData The input to search for {@code word}
     * @return A {@link List} of {@link SearchResults.Result} 
     *         objects
     */
    private List<SearchResults.Result> makeResultsRegex
        (String word,
         String inputData) {
        // Create an ArrayList to store Result objects.
        var results = new ArrayList<SearchResults.Result>();

        // Compile the word into a regular expression pattern.
        Pattern pattern = Pattern.compile(word);

        // Create a matcher object to perform matching operations on
        // the input data.
        Matcher matcher = pattern.matcher(inputData);

        // Initialize the starting index for searching.
        int index = 0;

        // Continue searching for matches starting from the index.
        while (matcher.find(index)) {
            // Add the start index of the match to the results.
            results.add(new SearchResults.Result(matcher.start()));

            // Update the index to search for the next match.
            index = matcher.start() + 1;
        }

        // Return the list of results.
        return results;
    }

    /**
     * Use Java Streams to make a {@link List} of {@link
     * SearchResults.Result} objects for the {@code word} to search
     * for in the {@code inputData}.
     * 
     * @param word The word to search for
     * @param inputData The input to search for {@code word}
     * @return A {@link List} of {@link SearchResults.Result} 
     *         objects
     */
    private List<SearchResults.Result> makeResultsStream
        (String word,
         String inputData) {

        // Return a List of SearchResults.Result objects.
        return IntStream
            // Create a stream of indices from 0 to inputData.length().
            .range(0, inputData.length())

            // Filter the indices to keep only the ones where the word
            // appears at that position.
            .filter(i -> inputData
                    .indexOf(word, i) == i)

            // Map each index to a new Result object.
            .mapToObj(SearchResults.Result::new)

            // Collect the Result objects into a List.
            .toList();
    }

    /**
     * Use the Java {@code indexOf()} method to make a {@link List} of
     * {@link SearchResults.Result} objects for the {@code word} to
     * search for in the {@code inputData}.
     * 
     * @param word The word to search for
     * @param inputData The input to search for {@code word}
     * @return A {@link List} of {@link SearchResults.Result} 
     *         objects
     */
    private List<SearchResults.Result> makeResultsIndexOf
        (String word,
         String inputData) {
        // Create an ArrayList to store Result objects.
        var results = new ArrayList<SearchResults.Result>();

        // Check to see how many times (if any) the word appears in
        // the input data.
        for (int i = inputData.indexOf(word);
             i != -1;
             i = inputData.indexOf(word, i + 1)) {
            // Each time a match is found, it's added to the list of
            // search results.
            results.add(new SearchResults.Result(i));
        } 

        // Return the list of results.
        return results;
    }
}

