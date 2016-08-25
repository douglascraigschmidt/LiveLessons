/**
 * This class is used in conjunction with WordMatchItr to identify
 * all indices in the input data that match a word.
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
     * Constructor initializes the object.
     */
    public WordMatcher(String word) {
        mWord = word;
    }

    /**
     * Associate @a inputData with the WordMatcher.
     */
    public WordMatcher with(String inputData) {
        mInputData = inputData;

        // Try to find the match (if any) of the word in the input
        // data.
        mCurrentPosition = mInputData.indexOf(mWord, 0);
        return this;
    }

    /**
     * @return true if a match was found.
     */
    public boolean find() {
        return mCurrentPosition != -1;
    }

    /**
     * Return the index in the input data of the word that
     * matched.
     */
    public Integer next() {
        Integer index = mCurrentPosition;
        mCurrentPosition =
            mInputData.indexOf(mWord, 
                               mCurrentPosition + mWord.length());

        return index;
    }
}
