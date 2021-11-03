package search;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collector;

/**
 * Keeps track of how many times a phrase appears in an input string.
 */
public class SearchResults {
    /**
     * Hold the index of one search result.
     */
    public static class Result {
        /**
         * The index in the search String where the phrase that was
         * found.
         */
        private final int mIndex;

        /**
         * Create a Result object contains meta-data about a search
         * result.
         */
        Result(int index) {
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
     * The phrase that was found.
     */
    private String mPhrase;

    /**
     * The section title this search is associated with.
     */
    private String mTitle;

    /**
     * The cycle in which the search result was found.
     */
    private long mCycle;

    /**
     * The List of Result objects that matched the @code mPhrase.
     */
    private final List<Result> mList;

    /**
     * Create an empty SearchResults, which is used to shutdown
     * processing of the BlockingQueue.
     */
    public SearchResults() {
        mList = null;
    }

    /**
     * Return the list of Results.
     */
    List<Result> getResultList() {
        return mList;
    }

    /**
     * Create a SearchResults with values for the various fields.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String phrase,
                         String title) {
        mThreadId = threadId;
        mCycle = cycle;
        mPhrase = phrase;
        mTitle = title;
        mList = new ArrayList<>();
    }

    /**
     * Create a SearchResults with values for the various fields.
     * This constructor is also passed a filled in resultList.
     */
    public SearchResults(long threadId,
                         long cycle,
                         String phrase,
                         String title,
                         List<Result> resultList) {
        mThreadId = threadId;
        mCycle = cycle;
        mPhrase = phrase;
        mTitle = title;
        mList = resultList;
    }

    /**
     * Create a SearchResults with values for the various fields.
     * This constructor is also passed a filled in resultList.
     */
    SearchResults(String phrase,
                  String title,
                  List<Result> resultList) {
        mThreadId = Thread.currentThread().getId();
        mCycle = 1;
        mPhrase = phrase;
        mTitle = title;
        mList = resultList;
    }

    /**
     * @return The title (if any) associated with these search results.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Set the title associated with these search results.
     */
    public void setTitle (String title) {
        mTitle = title;
    }

    /**
     * Convert to header to String form.
     */
    private String headerToString() {
        return "\"" + mPhrase + "\" at ";
    }

    /**
     * Add a Result.
     */
    public void add(int index) {
        mList.add(new Result(index));
    }

    /**
     * Add all the elements of {@code searchResults} results list to
     * the end of this results list.
     */
    public void addAll(SearchResults searchResults) {
        if (searchResults.mList != null) 
            mList.addAll(searchResults.mList);
    }

    /**
     * Returns true if there are no search results.
     */
    public boolean isEmpty() {
        return mList == null || mList.size() == 0;
    }

    /**
     * Returns the number of results.
     */
    public int size() {
        if (mList == null)
            return 0;
        else
            return mList.size();
    }

    /**
     * Return the phrase.
     */
    public String getPhrase() {
        return mPhrase;
    }

    /**
     * Return a string version of the object.
     */
    @Override
    public String toString() {
        String output = "";

        if (!isEmpty()) {
            output += headerToString()
                // Create a string containing indices of all the
                // matches.
                + "["
                + mList
                // Convert list to a stream.
                .stream()

                // Create a custom collector to join all the results
                // together.
                .collect(Collector
                         .of(// supplier
                             () -> new StringJoiner("|"),  

                             // accumulator
                             (j, r) -> j.add(r.toString()),       

                             // combiner
                             StringJoiner::merge,                 

                             // finisher
                             StringJoiner::toString))             
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
