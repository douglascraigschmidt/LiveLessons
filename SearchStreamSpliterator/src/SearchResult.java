/**
 * This class keeps track of state information that's returned by the
 * WordMatcher to the SearchStream.  All the fields are final so
 * objects of this class are immutable.
 */
public class SearchResult {
    /**
     * The string that's being searched.
     */
    final String mInputData;

    /**
     * The word that's being searched for in the inputData.
     */
    final String mWord;
    
    /**
     * The index of the word match in the mInputData.
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
                  String word,
                  Integer index) {
        mWord = word;
        mIndex = index;
        mInputData = inputData;
        mThreadId = Thread.currentThread().getId();
    }
}

