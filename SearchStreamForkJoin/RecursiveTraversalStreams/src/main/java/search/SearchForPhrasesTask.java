package search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static utils.StreamsUtils.not;

/**
 * A RecursiveTask that searches an input string for a list of
 * phrases.
 */
public class SearchForPhrasesTask
       extends RecursiveTask<List<SearchResults>> {
    /**
     * Title of the work that's being searched.
     */
    private String mTitle;

    /**
     * The input string to search.
     */
    private final CharSequence mInputString;

    /**
     * The list of phrases to find.
     */
    private List<String> mPhraseList;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    private boolean mParallelSearching;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    private boolean mParallelPhrases;

    /**
     * The minimum size of the phrases list to split.
     */
    private int mMinSplitSize;

    /**
     * Constructor initializes the field.
     */
    public SearchForPhrasesTask(String title, 
                                CharSequence inputString,
                                List<String> phraseList,
                                boolean parallelSearching,
                                boolean parallelPhrases) {
        mTitle = title;
        mInputString = inputString;
        mPhraseList = phraseList;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mMinSplitSize = phraseList.size() / 2;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    public SearchForPhrasesTask(String title,
                                CharSequence inputString,
                                List<String> phraseList,
                                boolean parallelSearching,
                                boolean parallelPhrases,
                                int minSplitSize) {
        mTitle = title;
        mInputString = inputString;
        mPhraseList = phraseList;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Perform the computations sequentially at this point.
     *
     * @return A list of search results that matched the phrases
     */
    private List<SearchResults> computeSequentially() {
        // Return a list of search results that matched the phrases.
        return mPhraseList
            // Convert the list into a stream.
            .stream()

            // Find all indices where the phrase matches in the input.
            .map(phrase
                 -> new SearchResults
                 (phrase,
                  mTitle,
                  // Use a PhraseMatchTask to add the indices of all
                  // places in the inputData where phrase matches.
                  new PhraseMatchTask(mInputString,
                                      phrase,
                                      mParallelSearching).compute()))

            // If a phrase was found add it to the list of results.
            .filter(not(SearchResults::isEmpty))

            // Trigger intermediate operation processing and return a list.
            .collect(toList());
    }

    /**
     * This method searches the @a inputString for all occurrences of
     * the phrases to find.
     */
    @Override
    public List<SearchResults> compute() {
        if (mPhraseList.size() < mMinSplitSize
            || !mParallelPhrases)
            return computeSequentially();
        else 
            // Compute position to split the phrase list and forward
            // to the splitPhraseList() method to perform the split.
            return splitPhraseList(mPhraseList.size() / 2);
    }

    /**
     * Use the fork-join framework to recursively split the input list
     * and return a list of SearchResults that contain all matching
     * phrases in the input list.
     */
    private List<SearchResults> splitPhraseList(int splitPos) {
        // Create and fork a new SearchWithForkJoinTask that
        // concurrently handles the "left hand" part of the input,
        // while "this" handles the "right hand" part of the input.
        ForkJoinTask<List<SearchResults>> leftTask =
                forkLeftTask(splitPos,mMinSplitSize);

        // Update "this" SearchForPhrasesTask to handle the "right
        // hand" portion of the input.
        List<SearchResults> rightResult =
            computeRightTask(splitPos,mMinSplitSize);

        // Wait and join the results from the left task.
        List<SearchResults> leftResult = leftTask.join();

        // sConcatenate the left result with the right result.
        leftResult.addAll(rightResult);

        // Return the result.
        return leftResult;
    }

    /**
     *
     */
    protected List<SearchResults> computeRightTask(int splitPos,
                                                   int mMinSplitSize) {
        mPhraseList = mPhraseList.subList(splitPos,
                                          mPhraseList.size());

        // Recursively call compute() to continue the splitting.
        return compute();
    }

    /**
     *
     */
    protected ForkJoinTask<List<SearchResults>> forkLeftTask(int splitPos,
                                                             int mMinSplitSize) {
        return new SearchForPhrasesTask(mTitle,
                                        mInputString,
                                        mPhraseList.subList(0, splitPos),
                                        mParallelSearching,
                                        mParallelPhrases,
                                        mMinSplitSize).fork();
    }
}

