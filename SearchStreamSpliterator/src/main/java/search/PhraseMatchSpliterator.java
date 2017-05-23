package search;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * This class is used in conjunction with StreamSupport.stream() to
 * create a stream of SearchResult objects that match the number of
 * times a phrase appears in an input string.  The comparison is
 * case-insensitive.
 */
public class PhraseMatchSpliterator
       implements Spliterator<SearchResult> {
    /**
     * The input string.
     */
    private final String mInput;

    /**
     * The current position in the input.
     */
    private int mCurrentPos = 0;

    /**
     * The phrase to search for in the input string.
     */
    private final String mPhrase;

    /**
     * The minimum size of an input string to split.
     */
    private int mMinSplitSize = 0;

    /**
     * Constructor initializes the fields.
     */
    public PhraseMatchSpliterator(String input,
                                  String phrase) {
        // Initialize the fields.
        mInput = input;
        mPhrase = phrase;
        mMinSplitSize = input.length() / 2;
        // System.out.println("in PhraseMatchSpliterator, sMIN_SPLIT_SIZE = " + sMIN_SPLIT_SIZE + " phrase = " + phrase);
    }

    /**
     * This constructor is used internally by the trySplit() method
     * below.
     */
    @SuppressWarnings("unused")
    private PhraseMatchSpliterator(String input,
                                   String phrase,
                                   int minSplitSize) {
        // System.out.println("in PhraseMatchSpliterator, input length = " + input.length());

        // Initialize the fields.
        mInput = input;
        mPhrase = phrase;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Attempt to advance the spliterator by one matching phrase.
     */
    @Override
    public boolean tryAdvance(Consumer<? super SearchResult> action) {
        // Try to find a phrase match in the input, ignoring case.
        mCurrentPos = mInput
            .toLowerCase()
            .indexOf(mPhrase.toLowerCase(),
                     mCurrentPos);

        // If there's no match then we're done with the iteration.
        if (mCurrentPos == -1) {
            // System.out.println("no match for " + mPhrase);
            return false;
        } else {
            // System.out.println("found a match for " + mPhrase);

            // Create a new SearchResult with the current position.
            SearchResult searchResult =
                new SearchResult(mInput, mPhrase, mCurrentPos);

            // Store the SearchResult of the match in the action.
            action.accept(searchResult);

            // Compute the next potential position.
            int nextPos = mCurrentPos + mPhrase.length();

            // Return false if we're at the end of the input.
            if (nextPos >= mInput.length()) 
                return false;
            else {
                // Advance the current position, skipping over the
                // phrase that matched.
                mCurrentPos = nextPos;

                // Indicate that the spliterator should continue.
                return true;
            } 
        }
    }

    /**
     * Attempt to split the input so phrases can be matched
     * concurrently.
     */
    @Override
    public Spliterator<SearchResult> trySplit() {
        int currentSize = mInput.length() - mCurrentPos;
        // System.out.println("in trySplit(): currentSize = " + currentSize);

        // Bail out if the input is too small to split further.
        if (currentSize < mMinSplitSize)
            return null;

        // Compute the candidate split point.
        int splitPos = (currentSize / 2) + mCurrentPos;

        // Subtract the phrase length so we can check to make sure the
        // phrase doesn't span across splitPos.
        int startPos = splitPos - mPhrase.length();

        // Check if phrase is too long for this segment.
        if (startPos < 0
            || mPhrase.length() > splitPos) {
            // System.out.println("phrase " + mPhrase + " is too long for segment");
            return null;
        }

        // Handle the case where a phrase spans across the initial
        // splitPos.
        for (;
             startPos < splitPos;
             startPos++) {
            assert (startPos + mPhrase.length() > mInput.length());

            // Check to see if the phrase matches.
            if (mInput.regionMatches(true, startPos, mPhrase, 0, mPhrase.length())) {
                // System.out.println("phrase " + mPhrase + " matched at " + startPos);

                // If the phrase matches then update splitPos to right
                // after the end of the phrase.
                splitPos = startPos + mPhrase.length();
                break;
            }
        }

        // Keep track of the current position.
        int originalCurrentPos = mCurrentPos;

        // Update the current position to be splitPos.
        mCurrentPos = splitPos;

        // Create a new PhraseMatchSpliterator object that handles the
        // "left hand" portion of the input, while the "this" object
        // handles the "right hand" portion of the input.
        return new PhraseMatchSpliterator(mInput.substring(originalCurrentPos,
                                                           splitPos),
                                          mPhrase,
                                          mMinSplitSize);
    }

    /**
     * Estimate the current size of the remaining input.
     */
    @Override
    public long estimateSize() {
        return mInput.length() - mCurrentPos;
    }

    /**
     * The characteristics for this spliterator.
     */
    @Override
    public int characteristics() {
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}

