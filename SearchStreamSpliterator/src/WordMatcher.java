/**
 * This class is used in conjunction with WordMatchSpliterator to
 * identify all indices in the input data that match a word.
 */
public class WordMatcher {
    /**
     * The word to match.
     */
    private String mWord;
        
    /**
     * The input data to do the matching.
     */
    private String mInputData;
        
    /**
     * The current position in the input data.
     */
    private int mCurrentPosition;

    /**
     * Constructor initializes the fields.
     */
    public WordMatcher(String word, 
                       String inputData) {
        mWord = word;
        mInputData = inputData;

        // Try to find the match (if any) of the word in the input
        // data.
        mCurrentPosition = mInputData.indexOf(mWord, 0);
    }

    /**
     * @return true if a match was found.
     */
    public boolean find() {
        return mCurrentPosition != -1;
    }

    /**
     * Return SearchResult containing the word and its index in the
     * input data when a match occurs.
     */
    public SearchResult next() {
        // Create a new SearchResult with the current position.
        SearchResult searchResult =
                new SearchResult(mInputData, mWord, mCurrentPosition);

        // Advance the current position.
        mCurrentPosition =
            mInputData.indexOf(mWord, 
                               mCurrentPosition + mWord.length());

        // Return the SearchResult.
        return searchResult;
    }
}
