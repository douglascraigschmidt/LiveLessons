package search;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * A RecursiveTask that searches an input string for a list of
 * phrases.  It uses indices instead of sublists to minimize copying.
 * Thanks to Sanjeev Kumar for suggesting/implementing this approach.
 */
public class IndexAwareSearchForPhrasesTask
       extends SearchForPhrasesTask {
    /**
     * The beginning of the sublist range.
     */
    private final int mStartIndex;

    /**
     * One past the end of the sublist range.
     */
    private final int mEndIndex;

    public IndexAwareSearchForPhrasesTask(CharSequence inputString,
                                          List<String> phraseList,
                                          boolean parallelSearching,
                                          boolean parallelPhrases) {
        super(inputString,
              phraseList,
              parallelSearching,
              parallelPhrases);
        // Initialize the fields.
        mStartIndex = 0;
        mEndIndex = phraseList.size();
        mMinSplitSize = getPartitionSize() / 2;
    }

    /**
     * Return the size of a partition.
     */
    @Override
    protected int getPartitionSize() {
        return mEndIndex-mStartIndex;
    }

    /**
     * Return the start index.
     */
    @Override
    int getStartIndex() {
        return mStartIndex;
    }

    /**
     * Return the end index.
     */
    @Override
    int getEndIndex() {
        return mEndIndex;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    private IndexAwareSearchForPhrasesTask(CharSequence inputString,
                                           List<String> phraseList,
                                           boolean parallelSearching,
                                           boolean parallelPhrases,
                                           int minSplitSize,
                                           int start,
                                           int end) {
        super(inputString,
              phraseList,
              parallelSearching,
              parallelPhrases,
              minSplitSize);
        mStartIndex = start;
        mEndIndex = end;
    }

    /**
     * Recursively compute the right task.
     */
    @Override
    protected List<SearchResults> computeRightTask(int splitPos,
                                                   int mMinSplitSize) {
        return new IndexAwareSearchForPhrasesTask(mInputString,
                                                  mPhraseList,
                                                  mParallelSearching,
                                                  mParallelPhrases,
                                                  mMinSplitSize,
                                                  mStartIndex + splitPos,
                                                  mEndIndex).compute();
    }

    /**
     * Create and fork a new IndexAwareSearchForPhrasesTask that
     * concurrently handles the "left hand" part of the input.
     */
    @Override
    protected ForkJoinTask<List<SearchResults>> forkLeftTask(int splitPos,
                                                             int mMinSplitSize) {
        return new IndexAwareSearchForPhrasesTask(mInputString,
                                                  mPhraseList,
                                                  mParallelSearching,
                                                  mParallelPhrases,
                                                  mMinSplitSize,
                                                  mStartIndex,
                                                  mStartIndex + splitPos).fork();
    }
}
