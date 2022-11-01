package livelessons.streamgangs;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import livelessons.utils.SearchForPhrasesTask;
import livelessons.utils.SearchResults;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang framework to use the Java fork/join
 * framework to perform parallel searches of each input data string
 * and for each phrase (from an array of phrases) within each input
 * data string.
 */
public class SearchWithForkJoin
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithForkJoin(List<String> phrasesToFind,
                              List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses the Java fork-join pool to
     * perform a parallel search for phrases in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Use the fork-join common pool to search the input looking
        // for phrases that match.
        return ForkJoinPool
            .commonPool()
            .invoke(new SearchWithForkJoinTask(getInput(),
                                               mPhrasesToFind));
    }
    
    /**
     * This class demonstrates the use of the Java 7 fork-join
     * framework to search for phrases in the works of Shakespeare.
     * This version uses Java 8 streams to implement the class logic
     * more concisely.
     */
    private static class SearchWithForkJoinTask
        extends RecursiveTask<List<List<SearchResults>>> {
        /**
         * The list of strings to search.
         */
        private List<? extends CharSequence> mInputList;

        /**
         * The list of phrases to find.
         */
        private List<String> mPhrasesToFind;

        /**
         * The minimum size of an input list to split.
         */
        private final int mMinSplitSize;

        /**
         * Construtor initializes the fields.
         */
        SearchWithForkJoinTask(List<? extends CharSequence> inputList,
                               List<String> phrasesToFind) {
            mInputList = inputList;
            mPhrasesToFind = phrasesToFind;
            mMinSplitSize = inputList.size() / 2;
        }

        /**
         * This constructor is used internally by the compute() method.
         * It initializes all the fields for the "left hand size" of a
         * split.
         */
        private SearchWithForkJoinTask(List<? extends CharSequence> inputList,
                                       List<String> phrasesToFind,
                                       int minSplitSize) {
            mInputList = inputList;
            mPhrasesToFind = phrasesToFind;
            mMinSplitSize = minSplitSize;
        }

        /**
         * Perform the computations sequentially at this point.
         */
        private List<List<SearchResults>> computeSequentially() {
            // Return the list of lists of SearchResults.
            return mInputList
                // Convert the list of input strings into a stream.
                .stream()

                // Create a SearchForPhrasesTask that searches an input
                // string for a list of phrases and store the results from
                // computing the task.
                .map(input
                     ->
                     new SearchForPhrasesTask(input,
                                              mPhrasesToFind).compute())

                // If a phrase was found add it to the list of results.
                .filter(not(List<SearchResults>::isEmpty))

                // Trigger stream processing and collect the results into a
                // list.
                .collect(toList());
        }

        /**
         * Searches for phrases to find in the input list.
         */
        @Override
        protected List<List<SearchResults>> compute() {
            if (mInputList.size() <= mMinSplitSize)
                return computeSequentially();
            else 
                // Compute position to split the input list and forward to
                // the splitInputList() method to perform the split.
                return splitInputList(mInputList.size() / 2);
        }

        /**
         * Use the fork-join framework to recursively split the input
         * list and return a list of lists of SearchResults that
         * contain all matching phrases in the input list.
         */
        private List<List<SearchResults>> splitInputList(int splitPos) {
            // Create and fork a new SearchWithForkJoinTask that
            // concurrently handles the "left hand" part of the input,
            // while "this" handles the "right hand" part of the
            // input.
            ForkJoinTask<List<List<SearchResults>>> leftTask =
                new SearchWithForkJoinTask(mInputList.subList(0, 
                                                              splitPos),
                                           mPhrasesToFind,
                                           mMinSplitSize).fork();

            // Update "this" SearchWithForkJoinTask to handle the
            // "right hand" portion of the input.
            mInputList =
                mInputList.subList(splitPos, mInputList.size());

            // Recursively call compute() to continue the splitting.
            List<List<SearchResults>> rightResult = compute();

            // Wait and join the results from the left task.
            List<List<SearchResults>> leftResult = leftTask.join();

            // sConcatenate the left result with the right result.
            leftResult.addAll(rightResult);

            // Return the result.
            return leftResult;
        }
    }
}

