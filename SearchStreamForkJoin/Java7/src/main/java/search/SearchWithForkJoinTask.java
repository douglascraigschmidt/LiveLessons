package search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * This class demonstrates the use of the Java 7 fork-join framework
 * to search for phrases in the works of Shakespeare.  
 */
public class SearchWithForkJoinTask
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
     * Indicates whether to search for a phrase in each string
     * concurrently.
     */
    private boolean mParallelSearching;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    private boolean mParallelPhrases;

    /**
     * Indicates whether to run the input concurrently.
     */
    private boolean mParallelInput;

    /**
     * The minimum size of an input list to split.
     */
    private final int mMinSplitSize;

    /**
     * Constructor initializes the fields.
     */
    public SearchWithForkJoinTask(List<? extends CharSequence> inputList,
                                  List<String> phrasesToFind,
                                  boolean parallelSearching,
                                  boolean parallelPhrases,
                                  boolean parallelInput) {
        mInputList = inputList;
        mPhrasesToFind = phrasesToFind;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mParallelInput = parallelInput;
        mMinSplitSize = inputList.size() / 2;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    private SearchWithForkJoinTask(List<? extends CharSequence> inputList,
                                   List<String> phrasesToFind,
                                   boolean parallelSearching,
                                   boolean parallelPhrases,
                                   boolean parallelInput,
                                   int minSplitSize) {
        mInputList = inputList;
        mPhrasesToFind = phrasesToFind;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mParallelInput = parallelInput;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Perform the computations sequentially at this point.
     */
    private List<List<SearchResults>> computeSequentially() {
        // Create a list to hold the results.
        List<List<SearchResults>> results =
            new ArrayList<>(mInputList.size());

        // Loop through each input string in the list.
        for (CharSequence input : mInputList) {
            // Create a SearchForPhrasesTask that searches an input
            // string for a list of phrases and store the results from
            // computing the task.
            List<SearchResults> lsr =
                new SearchForPhrasesTask(input,
                                         mPhrasesToFind,
                                         mParallelSearching,
                                         mParallelPhrases)
                .compute();

            // If a phrase was found add it to the list of results.
            if (lsr.size() > 0)
                results.add(lsr);
        }

        // Return the results.
        return results;
    }

    /**
     * Searches for phrases to find in the input list.
     */
    @Override
    protected List<List<SearchResults>> compute() {
        if (mInputList.size() <= mMinSplitSize
            || !mParallelInput)
            return computeSequentially();
        else 
            // Compute position to split the input list and forward to
            // the splitInputList() method to perform the split.
            return splitInputList(mInputList.size() / 2);
    }

    /**
     * Use the fork-join framework to recursively split the input list
     * and return a list of lists of SearchResults that contain all
     * matching phrases in the input list.
     */
    private List<List<SearchResults>> splitInputList(int splitPos) {
        // Create and fork a new SearchWithForkJoinTask that
        // concurrently handles the "left hand" part of the input,
        // while "this" handles the "right hand" part of the input.
        ForkJoinTask<List<List<SearchResults>>> leftTask =
            new SearchWithForkJoinTask(mInputList.subList(0, splitPos),
                                       mPhrasesToFind,
                                       mParallelSearching,
                                       mParallelPhrases,
                                       mParallelInput,
                                       mMinSplitSize).fork();

        // Update "this" SearchWithForkJoinTask to handle the "right
        // hand" portion of the input.
        mInputList = mInputList.subList(splitPos, mInputList.size());

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
