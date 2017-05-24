package search;

/**
 * This class keeps track of state information that's returned by the
 * WordMatchSpliterator to the SearchStream.  All the fields are final
 * so objects of this class are immutable.
 */
public class SearchResult {
    /**
     * The string that's being searched.
     */
    public final String mInput;

    /**
     * The phrase that's being searched for in the inputData.
     */
    public final String mPhrase;
    
    /**
     * The index of the phrase match in the mInputData.
     */
    public final int mIndex;

    /**
     * The id of the thread where the search was performed.
     */
    public final long mThreadId;

    /**
     * Constructor initializes the fields.
     */
    protected SearchResult(String input,
                           String phrase,
                           int index) {
        mPhrase = phrase;
        mIndex = index;
        mInput = input;
        mThreadId = Thread.currentThread().getId();
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
    public String getPhrase() {
        return mPhrase;
    }

    /**
     * Return the index.
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * Returns a string version of this object.
     */
    @Override
    public String toString() {
        return "" + mIndex;
    }
}

