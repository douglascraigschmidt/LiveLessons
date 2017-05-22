import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * This class is used in conjunction with StreamSupport.stream() to
 * create a stream of SearchResult objects that match the number of
 * times a word appears in an input string.
 */
public class WordMatchSpliterator
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
     * The word to search for in the input string.
     */
    private final String mWord;

    /**
     * The minimum size of an input string to split.
     */
    private static int sMIN_SPLIT_SIZE = 0;

    /**
     * Constructor initializes the fields.
     */
    public WordMatchSpliterator(String input,
                                String word) {
        // Initialize the fields.
        mInput = input;
        mWord = word;
        sMIN_SPLIT_SIZE = input.length() / 2;
        // System.out.println("in WordMatchSpliterator, sMIN_SPLIT_SIZE = " + sMIN_SPLIT_SIZE + " word = " + word);
    }

    /**
     * This constructor is used internally by the trySplit() method
     * below.
     */
    @SuppressWarnings("unused")
    private WordMatchSpliterator(String input,
                                 String word,
                                 boolean unused) {
        // System.out.println("in WordMatchSpliterator, input length = " + input.length());

        // Initialize the fields.
        mInput = input;
        mWord = word;
    }

    /**
     * Attempt to advance the spliterator by one matching word.
     */
    @Override
    public boolean tryAdvance(Consumer<? super SearchResult> action) {
        // Try to find a word match in the input, ignoring case.
        mCurrentPos = mInput
            .toLowerCase()
            .indexOf(mWord.toLowerCase(),
                     mCurrentPos);

        // If there's no match then we're done with the iteration.
        if (mCurrentPos == -1) {
            // System.out.println("no match for " + mWord);
            return false;
        } else {
            // System.out.println("found a match for " + mWord);

            // Create a new SearchResult with the current position.
            SearchResult searchResult =
                new SearchResult(mInput, mWord, mCurrentPos);

            // Store the SearchResult of the match in the action.
            action.accept(searchResult);

            // Compute the next potential position.
            int nextPos = mCurrentPos + mWord.length();

            // Return false if we're at the end of the input.
            if (nextPos >= mInput.length()) 
                return false;
            else {
                // Advance the current position, skipping over the
                // word that matched.
                mCurrentPos = nextPos;

                // Indicate that the spliterator should continue.
                return true;
            } 
        }
    }

    /**
     * Attempt to split the input processing so it can run
     * concurrently.
     */
    @Override
    public Spliterator<SearchResult> trySplit() {
        int currentSize = mInput.length() - mCurrentPos;
        // System.out.println("in trySplit(): currentSize = " + currentSize);

        // Bail out if the input is too small to split further.
        if (currentSize < sMIN_SPLIT_SIZE)
            return null;

        // Compute the candidate split point.
        int splitPos = (currentSize / 2) + mCurrentPos;

        // Subtract the word length so we can check to make sure the
        // word doesn't span across splitPos.
        int startPos = splitPos - mWord.length();

        // Check if word is too long for this segment.
        if (startPos < 0
            || mWord.length() > splitPos) {
            // System.out.println("word " + mWord + " is too long for segment");
            return null;
        }

        // Handle the case where a word spans across the initial
        // splitPos.
        for (;
             startPos < splitPos;
             startPos++) {
            assert (startPos + mWord.length() > mInput.length());

            // Check to see if the word matches.
            if (mInput.regionMatches(true, startPos, mWord, 0, mWord.length())) {
                // System.out.println("word " + mWord + " matched at " + startPos);

                // If the word matches then update splitPos to right
                // after the end of the word.
                splitPos = startPos + mWord.length();
                break;
            }
        }

        // Keep track of the current position.
        int originalCurrentPos = mCurrentPos;

        // Update the current position to be splitPos.
        mCurrentPos = splitPos;

        // Create a new WordMatchSpliterator object that handles the
        // "left hand" portion of the input, while the "this" object
        // handles the "right hand" portion of the input.
        return new WordMatchSpliterator(mInput.substring(originalCurrentPos,
                                                         splitPos),
                                        mWord,
                                        true);
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

