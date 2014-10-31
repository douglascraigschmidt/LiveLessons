import java.util.ArrayList;
import java.util.List;

/**
 * @class SearchResults
 *
 * @brief Holds the search results.
 */
public class SearchResults {
    /**
     * @class SearchResult
     *
     * @brief Holds one search result.
     */
    public class Result {
        /**
         * The index in the search String where the word that was
         * found.
         */
        public int mIndex;

        /**
         * Create a Result object contains meta-data about a search
         * result..
         */
        public Result(int index) {
            mIndex = index;
        }
    }

    /**
     * Id of the Thread that found a search result.
     */
    public long mThreadId;

    /**
     * The word that was found.
     */
    public String mWord;

    /**
     * The input String used for the search.
     */
    public String mInputData;

    /**
     * The cycle in which the search result was found.
     */
    public long mCycle;

    /**
     * The List of Result objects that matched the @code mWord.
     */
    protected List<Result> mList;

    /**
     * Create an empty SearchResults, which is used to shutdown
     * processing of the BlockingQueue.
     */
    public SearchResults() {
        mList = null;
    }

    /**
     * Create a SearchResults with values for the various fields.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String word,
                         String inputData) {
        mThreadId = threadId;
        mCycle = cycle;
        mWord = word;
        mInputData = inputData;
        mList = new ArrayList<Result>();
    }

    /**
     * Convert to String form.
     */
    public String toString() {
        return 
            "["
            + mThreadId
            + "|"
            + mCycle
            + "] "
            + mWord
            + " at "
            + mInputData;
    }

    /**
     * Add a Result.
     */
    public void add(int index) {
        mList.add(new Result(index));
    }

    /**
     * Returns true if there are no search results.
     */
    public boolean isEmpty() {
        return mList.size() == 0;
    }

    /**
     * Print the results.
     */
    void print() {
        if (!isEmpty()) {
            System.out.print(toString());

            for (Result result : mList)
                System.out.print ("["
                                  + result.mIndex
                                  + "]");
            System.out.println("");
        }
    }
}

