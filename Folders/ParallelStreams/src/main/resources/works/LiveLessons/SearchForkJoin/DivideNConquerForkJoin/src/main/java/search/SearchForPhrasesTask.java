package search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A RecursiveTask that searches an input string for a list of
 * phrases.
 */
public class SearchForPhrasesTask
       extends RecursiveTask<List<SearchResults>> {
    /**
     * The input string to search.
     */
    final CharSequence mInputString;

    /**
     * The list of phrases to find.
     */
    List<String> mPhraseList;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    boolean mParallelSearching;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    boolean mParallelPhrases;

    /**
     * The minimum size of the phrases list to split.
     */
    int mMinSplitSize;

    /**
     * Constructor initializes the field.
     */
    SearchForPhrasesTask(CharSequence inputString,
                         List<String> phraseList,
                         boolean parallelSearching,
                         boolean parallelPhrases) {
        mInputString = inputString;
        mPhraseList = phraseList;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mMinSplitSize = getPartitionSize()/ 2;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    SearchForPhrasesTask(CharSequence inputString,
                         List<String> phraseList,
                         boolean parallelSearching,
                         boolean parallelPhrases,
                         int minSplitSize) {
        mInputString = inputString;
        mPhraseList = phraseList;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Perform the computations sequentially at this point.
     * @param startIndex Starting index for the computations
     * @param endIndex   Ending index for the computations
     */
    private List<SearchResults> computeSequentially(int startIndex,
                                                    int endIndex) {
        // Create a list to hold the results.
        List<SearchResults> results =
            new ArrayList<>(getPartitionSize());

        // Get the section title.
        String title = getTitle(mInputString);

        // Skip over the title.
        CharSequence input = mInputString.subSequence(title.length(),
                                                      mInputString.length());

        // Loop through each phrase to find.
        for (int i = startIndex; i < endIndex; i++) {
            String phrase = mPhraseList.get(i);
            // Find all indices where the phrase matches in the input
            // data.
            SearchResults sr =
                new SearchResults
                (phrase,
                 title,
                 // Use a PhraseMatchTask to add the indices of all
                 // places in the inputData where phrase matches.
                 new PhraseMatchTask(input,
                                     phrase,
                                     mParallelSearching).compute());

            // If a phrase was found add it to the list of results.
            if (sr.size() > 0)
                results.add(sr);
        }

        // Return the results.
        return results;
    }

    /**
     * This method searches the {@code inputString} for all
     * occurrences of the phrases to find.
     */
    @Override
    public List<SearchResults> compute() {
        int partitionSize = getPartitionSize();

        if (partitionSize < mMinSplitSize || !mParallelPhrases)
            return computeSequentially(getStartIndex(),
                                       getEndIndex());
        else 
            // Compute position to split the phrase list and forward
            // to the splitPhraseList() method to perform the split.
            return splitPhraseList(partitionSize / 2);
    }

    /**
     * @return The size of the partition
     */
    protected int getPartitionSize() {
        return mPhraseList.size();
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
                forkLeftTask(splitPos,
                             mMinSplitSize);

        // Update "this" SearchForPhrasesTask to handle the "right
        // hand" portion of the input.
        List<SearchResults> rightResult = computeRightTask(splitPos,
                                                           mMinSplitSize);

        // Return the combined result from the leftTask with the
        // rightResult.
        return combineResults(leftTask, rightResult);
    }

    /**
     * Compute the right task.
     */
    protected List<SearchResults> computeRightTask(int splitPos,
                                                   int unused) {
        // Update mPhraseList to contain a sublist at the split
        // position.
        mPhraseList = mPhraseList.subList(splitPos,
                                          getPartitionSize());

        // Recursively call compute() to continue the splitting.
        return compute();
    }

    /**
     * Compute the left task.
     */
    protected ForkJoinTask<List<SearchResults>> forkLeftTask(int splitPos,
                                                             int mMinSplitSize) {
            // Create and fork a new task to handle the "right hand"
            // portion of the split.
        return new SearchForPhrasesTask(mInputString,
                                        mPhraseList.subList(0, 
                                                            splitPos),
                                        mParallelSearching,
                                        mParallelPhrases,
                                        mMinSplitSize).fork();
    }

    /**
     * @return the combined result from the {@code leftTask} with the
     * {@code rightResult}
     */
    protected List<SearchResults>
        combineResults(ForkJoinTask<List<SearchResults>> leftTask,
                       List<SearchResults> rightResult) {

        // Wait and join the results from the left task.
        List<SearchResults> leftResult = leftTask.join();

        // Concatenate the left result with the right result.
        leftResult.addAll(rightResult);

        // Return the result.
        return leftResult;
    }

    /**
     * @return The title portion of the {@code inputData}
     */
    private String getTitle(CharSequence input) {
        // Create a Matcher.
        Matcher m = Pattern
            // This regex matches the first line in the input.
            .compile("(?m)^.*$")

            // Create a matcher for this pattern.
            .matcher(input);

        return m.find()
            ? m.group()
            : "";
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
        return mPhraseList.size();
    }
}

