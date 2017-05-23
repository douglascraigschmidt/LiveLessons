package search;

/**
 * This class keeps track of state information associated when a word
 * matches the input string.  All the fields are final so objects of
 * this class are immutable.
 */
public class SearchResult {
    /**
     * The string that's being searched.
     */
    public final String mInputData;

    /**
     * The word that's being searched for in the inputData.
     */
    public final String mWord;
    
    /**
     * The index of the word match in the mInputData.
     */
    public final int mIndex;

    /**
     * Constructor initializes the fields.
     */
    public SearchResult(String inputData,
                        String word,
                        int index) {
        mWord = word;
        mIndex = index;
        mInputData = inputData;
    }

    /**
     * Return the input
     */
    public String getInput() {
        return mInput;
    }

    /**
     * Return the word.
     */
    public String getWord() {
        return mWord;
    }

    /**
     * Return the index.
     */
    public int getIndex() {
        return mIndex;
    }
 }

