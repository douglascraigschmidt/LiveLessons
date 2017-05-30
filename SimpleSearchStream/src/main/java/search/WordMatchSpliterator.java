package search;

import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * This class is used in conjunction with StreamSupport.stream() to
 * create a stream of SearchResults.Result objects that match the
 * number of times a phrase appears in an input string.
 */
public class WordMatchSpliterator
       extends Spliterators.AbstractSpliterator<SearchResults.Result> {
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
     * Constructor initializes the fields and super class.
     */
    public WordMatchSpliterator(String input,
                                String word) {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
        mWord = word;
        mInput = input;
        mCurrentPos = 0;
    }

    /**
     * Attempt to advance the spliterator by one position.
     */
    public boolean tryAdvance(Consumer<? super SearchResults.Result> action) {
        mCurrentPos = mInput.indexOf(mWord, mCurrentPos);

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
}

