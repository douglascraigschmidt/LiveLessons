package search;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * This class demonstrates the use of the Java 7 fork-join framework
 * to search for phrases in the works of Shakespeare.  It uses indices
 * instead of sublists to minimize copying.  Thanks to Sanjeev Kumar
 * for suggesting/implementing this approach.
 */
public class IndexAwareSearchWithForkJoinTask
       extends SearchWithForkJoinTask {
    /**
     * The beginning of the sublist range.
     */
    private final int mStartIndex;

    /**
     * One past the end of the sublist range.
     */
    private final int mEndIndex;

    /**
     * Constructor initializes the fields.
     */
    public IndexAwareSearchWithForkJoinTask(List<? extends CharSequence> inputList,
                                            List<String> phrasesToFind,
                                            boolean parallelSearching,
                                            boolean parallelPhrases,
                                            boolean parallelInput) {
        // Initialize the super class.
        super(inputList,
                phrasesToFind,
                parallelSearching,
                parallelPhrases,
                parallelInput);

        // Initialize the fields.
        mStartIndex = 0;
        mEndIndex = inputList.size();
        mMinSplitSize = getPartitionSize() / 2;
        // This implementation use the IndexAwareSearchForPhrasesTask.
        mConsRef = IndexAwareSearchForPhrasesTask::new;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    public IndexAwareSearchWithForkJoinTask(List<? extends CharSequence> inputList,
                                            List<String> phrasesToFind,
                                            boolean parallelSearching,
                                            boolean parallelPhrases,
                                            boolean parallelInput,
                                            int minSplitSize,
                                            int start,
                                            int end) {
        super(inputList,
              phrasesToFind,
              parallelSearching,
              parallelPhrases,
              parallelInput,
              minSplitSize);
        mStartIndex = start;
        mEndIndex = end;
    }

    /**
     * Return the size of a partition.
     */
    @Override
    int getPartitionSize() {
        return mEndIndex - mStartIndex;
    }

    /**
     * Return the start index.
     */
    int getStartIndex() {
        return mStartIndex;
    }

    /**
     * Return the end index.
     */
    int getEndIndex() {
        return mEndIndex;
    }

    /**
     * Recursively compute the right task.
     */
    @Override
    protected List<List<SearchResults>> computeRightTask(int splitPos,
                                                         int mMinSplitSize) {
        IndexAwareSearchWithForkJoinTask indexAwareSearchWithForkJoinTask =
            new IndexAwareSearchWithForkJoinTask(mInputList,
                                                 mPhrasesToFind,
                                                 mParallelSearching,
                                                 mParallelPhrases,
                                                 mParallelInput,
                                                 mMinSplitSize,
                                                 mStartIndex + splitPos,
                                                 mEndIndex);
        return indexAwareSearchWithForkJoinTask.compute();
    }

    /**
     * Create and fork a new SearchWithForkJoinTask that concurrently
     * handles the "left hand" part of the input
     */
    @Override
    protected ForkJoinTask<List<List<SearchResults>>> forkLeftTask(int splitPos,
                                                                   int mMinSplitSize) {
        return new IndexAwareSearchWithForkJoinTask(mInputList,
                                                    mPhrasesToFind,
                                                    mParallelSearching,
                                                    mParallelPhrases,
                                                    mParallelInput,
                                                    mMinSplitSize,
                                                    mStartIndex,
                                                    mStartIndex + splitPos).fork();
    }
}
