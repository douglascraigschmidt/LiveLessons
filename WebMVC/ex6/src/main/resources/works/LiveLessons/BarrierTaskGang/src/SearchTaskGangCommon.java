import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @class SearchTaskGangCommon
 * 
 * @brief This helper class factors out the common code used by all
 *        instantiations of the TaskGang framework in the BarrierTaskGang 
 *        project.  It customizes the TaskGang framework to concurrently 
 *        search one or more arrays of input Strings for words provided 
 *        in an array of words to find.
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
     * Exit barrier that controls when the framework and test object 
     * complete their concurrent processing.
     */
    protected CountDownLatch mExitBarrier = null;
        
    /**
     * Constructor initializes the data members.
     */
    protected SearchTaskGangCommon(String[] wordsToFind,
                                   String[][] stringsToSearch) {
        // Store the words to search for.
        mWordsToFind = wordsToFind;

        // Create an Iterator for the array of Strings to search.
        mInputIterator = Arrays.asList(stringsToSearch).iterator();
    }

    /**
     * Factory method that returns the next List of Strings to be
     * searched concurrently by the TaskGang.
     */
    @Override
    protected List<String> getNextInput() {
        if (!mInputIterator.hasNext())
        	return null;
        else {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the Strings to search
            // concurrently.
            return Arrays.asList(mInputIterator.next());
        }
    }

    /**
     * Hook method that initiates the gang of Tasks.
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
     * Factory method that creates a Runnable task that will process
     * one data element of the input List (at location @code index) in
     * a background task run by a Java Thread.
     */
    protected Runnable makeTask(final int index) {
        return new Runnable() {

            // This method runs in background task provided by a Java Thread.
            public void run() {
            	// Since this task runs in a distinct Thread it can
            	// block in a loop.
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
                            // A problem occurred, so return.
                            return;

                    } catch (IndexOutOfBoundsException e) {
                        // If an exception occurred then return from
                        // this Thread.
                        return;
                    }
                // Keep looping until all the iteration cycles are
                // done.
                } while (advanceTaskToNextCycle());
            }
        };
    }

    /**
     * Runs in a background Thread and searches the inputData for all
     * occurrences of the words to find.
     */
    @Override
    protected boolean processInput (String inputData) {
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        for (String word : mWordsToFind) {
            SearchResults results = searchForWord(word, 
                                                  inputData);

            // Each time a match is found the SearchResult.print()
            // method is called to print the output.  We put this call
            // in a synchronized statement so the output isn't all
            // scrambled up in different Threads.
            synchronized(System.out) {
                results.print();
            }
        }
        return true;
    }

    /**
     * Search for all instances of @code word in @code inputData and
     * return a list of all the @code SearchData results (if any).
     */
    protected SearchResults searchForWord(String word, 
                                          String inputData) {
        SearchResults results =
            new SearchResults(Thread.currentThread().getId(),
                              currentCycle(),
                              word,
                              inputData);

        // Check to see how many times (if any) the word appears
        // in the input data.
        for (int i = inputData.indexOf(word, 0);
             i != -1;
             i = inputData.indexOf(word, i + word.length())) {
            // Each time a match is found it's added to the list
            // of search results.
            results.add(i);
        }
        return results;
    }
    
    /**
     * Hook method that uses the CountDownLatch as an exit barrier to
     * wait for the gang of Threads to exit.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Blocks until all the tasks are done.
            mExitBarrier.await();
        } catch (InterruptedException e) {
        }
    }

}

