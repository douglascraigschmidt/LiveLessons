import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterators;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This helper class factors out the common code used by all
 * instantiations of the StreamGang framework in the BarrierStreamGang
 * project.  It customizes the StreamGang framework to concurrently
 * search one or more arrays of input Strings for words provided in an
 * array of words to find.
 */
public abstract class SearchStreamGangCommon
                extends StreamGang<String> {
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
    protected CountDownLatch mIterationBarrier = null;
        
    /**
     * Constructor initializes the data members.
     */
    protected SearchStreamGangCommon(String[] wordsToFind,
                                     String[][] stringsToSearch) {
        // Store the words to search for.
        mWordsToFind = wordsToFind;

        // Create an Iterator for the array of Strings to search.
        mInputIterator = Arrays.asList(stringsToSearch).iterator();

        // Initialize the Executor with a fixed-sized pool of Threads.
        // @@ setExecutor(Executors.newFixedThreadPool(MAX_THREADS));
    }

    /**
     * Hook method that must be overridden by subclasses to perform
     * the Stream processing.
     */
    protected abstract List<SearchResults> processStream();

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
        List<SearchResults> searchResults = processStream();

        // Note the end time.
        long duration = (System.nanoTime() - start) / 1_000_000;
        System.out.println(TAG + 
                           ": Done in " 
                           + duration
                           + " msecs");

        // Print the results.
        searchResults.stream().forEach(SearchResults::print);
  
        System.out.println(TAG 
                           + ": The search returned "
                           + searchResults.stream().mapToInt(SearchResults::size).sum()
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

    /**
     * Factory method that returns the next List of Strings to be
     * searched concurrently by the StreamGang.
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
     * Search the inputData for all occurrences of the words to find.
     */
    protected Stream<SearchResults> processInput (String inputData) {
        // Iterate through each word we're searching for and try to
        // find it in the inputData.
        return Stream
            // Convert the array of words into a Stream.
            .of(mWordsToFind)

            // Search for all places where the word matches the input data.
            .map(word -> searchForWord(word, inputData))
            
            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0);
    }

    /**
     * This class is used in conjunction with WordMatchItr to identify
     * all indices in the input data that match a word.
     */
    private static class WordMatcher {
        /**
         * The word to match.
         */
        String mWord;
        
        /**
         * The input data to do the matching.
         */
        String mInputData;
        
        /**
         * The current position in the input data.
         */
        int mCurrentPosition;

        /**
         * Constructor initializes the object.
         */
        public WordMatcher(String word) {
            mWord = word;
        }

        /**
         * Associate @a inputData with the WordMatcher.
         */
        public WordMatcher with(String inputData) {
            mInputData = inputData;

            // Try to find the match (if any) of the word in the input
            // data.
            mCurrentPosition = mInputData.indexOf(mWord, 0);
            return this;
        }

        /**
         * @return true if a match was found.
         */
        public boolean find() {
            return mCurrentPosition != -1;
        }

        /**
         * Return the index in the input data of the word that
         * matched.
         */
        public Integer next() {
            Integer index = Integer.valueOf(mCurrentPosition);
            mCurrentPosition =
                mInputData.indexOf(mWord, 
                                   mCurrentPosition + mWord.length());

            return index;
        }
    }

    /**
     * This Spliterator is used to create a Stream of matches to
     * a word in the input data.
     */
    private static class WordMatcherItr
                   extends Spliterators.AbstractSpliterator<Integer> {
        /**
         * Matches a word in the input data.
         */
        private final WordMatcher mMatcher;

        /**
         * Constructor initializes the field and super class.
         */
        WordMatcherItr(WordMatcher matcher) {
            super(Long.MAX_VALUE, ORDERED | NONNULL);
            mMatcher = matcher;
        }

        /**
         * Attempt to advance the spliterator by one position.
         */
        public boolean tryAdvance(Consumer<? super Integer> action) {
            // If there's no match then we're done with the iteration.
            if (!mMatcher.find())
                return false;

            // Store the index of the match in the action and keep the
            // iteration going.
            action.accept(mMatcher.next());
            return true;
        }
    }

    /**
     * Search for all instances of @code word in @code inputData and
     * return a list of all the @code SearchData results (if any).
     */
    protected SearchResults searchForWord(String word, 
                                          String inputData) {
    	// Create a SearchResults object that keeps track of relevant info.
        SearchResults results =
            new SearchResults(Thread.currentThread().getId(),
                              currentCycle(),
                              word,
                              inputData);
        // Use a WordMatchItr to add the indices of all places in the
        // inputData where word matches.
        StreamSupport
            // Create a stream of Integers indicating the indices of
            // all places (if any) where the word matched the input data.
            .stream(new WordMatcherItr(new WordMatcher(word).with(inputData)), 
                    false)
                    
            // Add any matches to the results object.
            .forEach(results::add);

        return results;
    }
    
    /**
     * Hook method that uses the CountDownLatch as an exit barrier to
     * wait for the gang of Threads to exit.
     */
    @Override
    protected void awaitTasksDone() {
        try {
            // Loop for each iteration cycle of input URLs.
            for (;;) {
                // Barrier synchronizer that waits until all the
                // stream processing in this iteration cycle are done.
                mIterationBarrier.await();

                // Check to see if there's another List of URLs
                // available to process.
                if (setInput(getNextInput()) == null)
                    break; // No more input, so we're done.
                else
                    // Invoke this hook method to initialize the gang
                    // of tasks for the next iteration cycle.
                    initiateStream();
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

        // Run the completion hook now that all the image downloading,
        // processing and storing is now complete.
        // mCompletionHook.run();
    }
}

