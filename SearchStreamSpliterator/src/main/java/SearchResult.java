/**
 * This class keeps track of state information that's returned by the
 * WordMatchSpliterator to the SearchStream.  All the fields are final
 * so objects of this class are immutable.
 */
public class SearchResult {
    /**
     * The string that's being searched.
     */
    final String mInputData;

    /**
     * The phrase that's being searched for in the inputData.
     */
    final String mPhrase;
    
    /**
     * The index of the phrase match in the mInputData.
     */
    final Integer mIndex;

    /**
     * The id of the thread where the search was performed.
     */
    final long mThreadId;

    /**
     * Constructor initializes the fields.
     */
    SearchResult(String inputData,
                 String phrase,
                 Integer index) {
        mPhrase = phrase;
        mIndex = index;
        mInputData = inputData;
        mThreadId = Thread.currentThread().getId();
    }
}

