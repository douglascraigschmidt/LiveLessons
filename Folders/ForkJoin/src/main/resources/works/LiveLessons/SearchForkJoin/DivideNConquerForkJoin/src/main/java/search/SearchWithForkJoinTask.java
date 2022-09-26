package search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static utils.StreamsUtils.QuadFunction;

/**
 * This class demonstrates the use of the Java fork-join framework to
 * search for phrases in the works of Shakespeare.
 */
public class SearchWithForkJoinTask
       extends RecursiveTask<List<List<SearchResults>>> {
    /**
     * The list of strings to search.
     */
    List<? extends CharSequence> mInputList;

    /**
     * The list of phrases to find.
     */
    List<String> mPhrasesToFind;

    /**
     * Indicates whether to search for a phrase in each string
     * concurrently.
     */
    boolean mParallelSearching;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    boolean mParallelPhrases;

    /**
     * Indicates whether to run the input concurrently.
     */
    boolean mParallelInput;

    /**
     * The minimum size of an input list to split.
     */
    int mMinSplitSize;

    /**
     * Customize the QuadFunction for the SearchForPhrasesTask
     * hierarchy of classes so we can use constructor references.
     */
    interface SearchForPhrasesTaskFactory
              extends QuadFunction<CharSequence,
                                   List<String>,
                                   Boolean,
                                   Boolean,
                                   SearchForPhrasesTask> {}

    /**
     * This constructor reference creates the appropriate
     * [IndexAware]SearchForPhrasesTask object.
     */
    SearchForPhrasesTaskFactory mConstructorRef;

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
        mMinSplitSize = getPartitionSize() / 2;
        // By default use the SearchForPhrasesTask.
        mConstructorRef = SearchForPhrasesTask::new;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    SearchWithForkJoinTask(List<? extends CharSequence> inputList,
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
        mConstructorRef = SearchForPhrasesTask::new;
    }

    /**
     * Searches for phrases to find in the input list.
     */
    @Override
    protected List<List<SearchResults>> compute() {
        int partitionSize = getPartitionSize();

        if (partitionSize <= mMinSplitSize || !mParallelInput)
            return computeSequentially(getStartIndex(), getEndIndex());
        else
            // Compute position to split the input list and forward to
            // the splitInputList() method to perform the split.
            return splitInputList(partitionSize / 2);
    }

    /**
     * Perform the computations sequentially at this point from {@code
     * startIndex} to {@code endIndex}.
     */
    private List<List<SearchResults>> computeSequentially(int startIndex,
                                                          int endIndex) {
        // Create a list to hold the results.
        List<List<SearchResults>> results =
            new ArrayList<>(getPartitionSize());

        // Loop through each input string in the "sublist" range.
        for (int i = startIndex; i < endIndex; i++) {
            // Get the ith CharSequence in the list.
            CharSequence input = mInputList.get(i);

            // Create a SearchForPhrasesTask that searches an input
            // string for a list of phrases and store the results from
            // computing the task.
            List<SearchResults> lsr =
                mConstructorRef.apply(input,
                                      mPhrasesToFind,
                                      mParallelSearching,
                                      mParallelPhrases).compute();

            // If a phrase was found add it to the list of results.
            if (lsr.size() > 0)
                results.add(lsr);
        }

        // Return the results.
        return results;
    }

    /**
     * Return the size of a partition.
     */
    int getPartitionSize() {
        return mInputList.size();
    }

    /**
     * Return the start index.
     */
    int getStartIndex() {
        return 0;
    }

    /**
     * Return the end index.
     */
    int getEndIndex() {
        return mInputList.size();
    }

    /**
     * Use the fork-join framework to recursively split the input list
     * and return a list of lists of SearchResults that contain all
     * matching phrases in the input list.
     */
    private List<List<SearchResults>> splitInputList(int splitPos) {
        // Create and fork a new SearchWithForkJoinTask that
        // concurrently handles the "left hand" part of the input.
        ForkJoinTask<List<List<SearchResults>>> leftTask =
            forkLeftTask(splitPos, mMinSplitSize);

        // Recursively compute the right task.
        List<List<SearchResults>> rightResult =
            computeRightTask(splitPos,
                             mMinSplitSize);

        // Return the combined result from the leftTask with the
        // rightResult.
        return combineResults(leftTask, rightResult);
    }

    /**
     * Create and fork a new SearchWithForkJoinTask that concurrently
     * handles the "left hand" part of the input
     */
    protected ForkJoinTask<List<List<SearchResults>>> forkLeftTask(int splitPos,
                                                                   int mMinSplitSize) {
        return new SearchWithForkJoinTask(mInputList.subList(0, splitPos),
                                          mPhrasesToFind,
                                          mParallelSearching,
                                          mParallelPhrases,
                                          mParallelInput,
                                          mMinSplitSize).fork();
    }

    /**
     * Recursively compute the right task.
     */
    protected List<List<SearchResults>> computeRightTask(int splitPos,
                                                         int mUnused) {
        // Update "this" SearchWithForkJoinTask to handle the "right
        // hand" portion of the input.
        mInputList =
            mInputList.subList(splitPos, getPartitionSize());

        // Recursively call compute() to continue the splitting.
        return compute();
    }

    /**
     * @return the combined result from the {@code leftTask} with the
     * {@code rightResult}
     */
    protected List<List<SearchResults>> 
        combineResults(ForkJoinTask<List<List<SearchResults>>> leftTask,
                       List<List<SearchResults>> rightResult) {
        // Wait and join the results from the left task.,
        List<List<SearchResults>> leftResult = leftTask.join();

        // sConcatenate the left result with the right result.
        leftResult.addAll(rightResult);

        // Return the result.
        return leftResult;
    }
}
