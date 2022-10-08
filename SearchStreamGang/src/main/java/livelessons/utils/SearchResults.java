package livelessons.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;

/**
 * Keeps track of how many times a word appears in an input string.
 */
public class SearchResults {
    /**
     * Hold the index of one search result.
     */
    public static class Result {
        /**
         * The index in the search String where the word that was
         * found.
         */
        private final int mIndex;

        /**
         * Create a Result object contains meta-data about a search
         * result.
         */
        public Result(int index) {
            mIndex = index;
        }

        /**
         * Return the index.
         */
        public int getIndex() {
            return mIndex;
        }

        /**
         * Return a string version of the object.
         */
        @Override
        public String toString() {
            return String.format("%d", mIndex);
        }
    }

    /**
     * Id of the Thread that found a search result.
     */
    private long mThreadId;

    /**
     * The word that was found.
     */
    private String mWord;

    /**
     * The section title this search is associated with.
     */
    private String mTitle;

    /**
     * The cycle in which the search result was found.
     */
    private long mCycle;

    /**
     * The List of Result objects that matched the @code mWord.
     */
    private final List<Result> mList;

    /**
     * Return the list of Results.
     */
    public List<Result> getResultList() {
        return mList;
    }

    /**
     * Create a SearchResults with values for the various fields.
     * This constructor is also passed a filled in resultList.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String word,
                         String title,
                         List<Result> resultList) {
        mThreadId = threadId;
        mCycle = cycle;
        mWord = word;
        mTitle = title;
        mList = resultList;
    }

    /**
     * Create a SearchResults with values for the various fields.
     * This constructor is also passed a filled in resultList.
     */
    public SearchResults(String word,
                         String title,
                         List<Result> resultList) {
        mThreadId = Thread.currentThread().getId();
        mCycle = 1;
        mWord = word;
        mTitle = title;
        mList = resultList;
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * Convert to header to String form.
     */
    public String headerToString() {
        return "\"" + mWord + "\" at ";
    }

    /**
     * Returns true if there are no search results.
     */
    public boolean isEmpty() {
        return mList.size() == 0;
    }

    /**
     * Returns the number of results.
     */
    public int size() {
        return mList.size();
    }

    /**
     * Return the word.
     */
    public String getWord() {
        return mWord;
    }

    /**
     * Return a string version of the object.
     */
    @Override
    public String toString() {
        String output = "";

        if (!isEmpty()) {
            output += headerToString()
                // Create a string containing indices of all the matches.
                + "["
                + mList
                // Convert list to a stream.
                .stream()

                // Create a custom collector to join all the results
                // together.
                .collect(Collector.of(() -> new StringJoiner("|"),   // supplier
                                      (j, r) -> j.add(r.toString()), // accumulator
                                      StringJoiner::merge,           // combiner
                                      StringJoiner::toString))       // finisher
                + "]";
        }
        
        return output;
    }

    /**
     * Print the results.
     */
    public SearchResults print() {
        if (!isEmpty()) 
            System.out.println(toString());

        return this;
    }
}
