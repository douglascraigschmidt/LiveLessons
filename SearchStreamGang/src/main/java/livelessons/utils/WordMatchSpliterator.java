package livelessons.utils;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * This class is used in conjunction with StreamSupport.stream() to
 * create a stream of SearchResult objects that match the number of
 * times a phrase appears in an input string.  The comparison is
 * case-insensitive.
 */
public class WordMatchSpliterator
       implements Spliterator<SearchResults.Result> {
    /**
     * The word to match.
     */
    private String mWord;
        
    /**
     * The input data to do the matching.
     */
    private String mInput;
        
    /**
     * The current position in the input data.
     */
    private int mCurrentPos;

    /**
     * The minimum size of an input string to split.
     */
    private int mMinSplitSize = 0;

    /**
     * Constructor initializes the fields and super class.
     */
    public WordMatchSpliterator(String input,
                                String word) {
        mWord = word;
        mInput = input;
        mMinSplitSize = input.length() / 2;
        mCurrentPos = 0;
    }

    /**
     * Constructor initializes the fields and super class.
     */
    public WordMatchSpliterator(String input,
                                String word,
                                int minSplitSize) {
        mWord = word;
        mInput = input;
        mCurrentPos = 0;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Attempt to advance the spliterator by one position.
     */
    public boolean tryAdvance(Consumer<? super SearchResults.Result> action) {
        // Try to find a phrase match in the input.
        mCurrentPos = StringUtils.indexOf(mInput,
                                          false,
                                          mWord,
                                          mCurrentPos);

        /*
        mCurrentPos = mInput.indexOf(mWord,
                                     mCurrentPos);
        */

        // If there's no match then we're done with the iteration.
        if (mCurrentPos == -1) 
            return false;
        else {
            action.accept(new SearchResults.Result(mCurrentPos));

            // Compute the next potential position.
            int nextPos = mCurrentPos + mWord.length();

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
    public Spliterator<SearchResults.Result> trySplit() {
        int currentSize = mInput.length() - mCurrentPos;
        // System.out.println("in trySplit(): currentSize = " + currentSize);

        // Bail out if the input is too small to split further.
        if (currentSize < mMinSplitSize)
            return null;

        // Compute the candidate split point.
        int splitPos = (currentSize / 2) + mCurrentPos;

        // Subtract the phrase length so we can check to make sure the
        // phrase doesn't span across splitPos.
        int startPos = splitPos - mWord.length();

        // Check if phrase is too long for this segment.
        if (startPos < 0
            || mWord.length() > splitPos) {
            // System.out.println("phrase " + mPhrase + " is too long for segment");
            return null;
        }

        // Handle the case where a phrase spans across the initial
        // splitPos.
        for (;
             startPos < splitPos;
             startPos++) {
            assert (startPos + mWord.length() > mInput.length());

            // Check to see if the phrase matches.
            if (mInput.regionMatches(true,
                                     startPos,
                                     mWord,
                                     0,
                                     mWord.length())) {
                // System.out.println("phrase " + mPhrase + " matched at " + startPos);

                // If the phrase matches then update splitPos to right
                // after the end of the phrase.
                splitPos = startPos + mWord.length();
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
        return new WordMatchSpliterator(mInput.substring(originalCurrentPos,
                                                         splitPos),
                                        mWord,
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

