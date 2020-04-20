package livelessons.utils;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * A RecursiveTask that searches an input string for a list of
 * phrases.  This version uses Java 8 streams to implement the class
 * logic more concisely.
 */
public class SearchForPhrasesTask
       extends RecursiveTask<List<SearchResults>> {
    /**
     * The input string to search.
     */
    private final CharSequence mInputString;

    /**
     * The list of phrases to find.
     */
    private List<String> mPhraseList;

    /**
     * The minimum size of the phrases list to split.
     */
    private final int mMinSplitSize;

    /**
     * Constructor initializes the field.
     */
    public SearchForPhrasesTask(CharSequence inputString,
                                List<String> phraseList) {
        mInputString = inputString;
        mPhraseList = phraseList;
        mMinSplitSize = phraseList.size()/ 2;
    }

    /**
     * This constructor is used internally by the compute() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    private SearchForPhrasesTask(CharSequence inputString,
                                 List<String> phraseList,
                                 int minSplitSize) {
        mInputString = inputString;
        mPhraseList = phraseList;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Perform the computations sequentially at this point.
     */
    private List<SearchResults> computeSequentially() {
        // Get the section title.
        String title = getTitle(mInputString);

        // Skip over the title.
        CharSequence input = mInputString.subSequence(title.length(),
                                                      mInputString.length());

        // Return the list of SearchResults.
        return mPhraseList
            // Convert the list of phrases into a stream.
            .stream()

            // Find all indices where the phrase matches in the input
            // data.
            .map(phrase
                 -> new SearchResults
                 (Thread.currentThread().getId(),
                  1,
                  phrase,
                  title,
                  // Use a PhraseMatchTask to add the indices of all
                  // places in the inputData where phrase matches.
                  new PhraseMatchTask(input,
                                      phrase).compute()))

            // If a phrase was found add it to the list of results.
            .filter(not(SearchResults::isEmpty))

            // Trigger stream procesing and collect the results into a
            // list.
            .collect(toList());
    }

    /**
     * This method searches the @a inputString for all occurrences of
     * the phrases to find.
     */
    @Override
    public List<SearchResults> compute() {
        if (mPhraseList.size() < mMinSplitSize)
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
            new SearchForPhrasesTask(mInputString,
                                     mPhraseList.subList(0, splitPos),
                                     mMinSplitSize).fork();

        // Update "this" SearchForPhrasesTask to handle the "right
        // hand" portion of the input.
        mPhraseList = mPhraseList.subList(splitPos, mPhraseList.size());

        // Recursively call compute() to continue the splitting.
        List<SearchResults> rightResult = compute();

        // Wait and join the results from the left task.
        List<SearchResults> leftResult = leftTask.join();

        // sConcatenate the left result with the right result.
        leftResult.addAll(rightResult);

        // Return the result.
        return leftResult;
    }

    /**
     * Return the title portion of the @a inputData.
     */
    private String getTitle(CharSequence input) {
        // Create a Matcher.
        Matcher m = Pattern
            // This regex matchs the first line in the input.
            .compile("(?m)^.*$")

            // Create a matcher for this pattern.
            .matcher(input);

        return m.find()
            ? m.group()
            : "";
    }
}

