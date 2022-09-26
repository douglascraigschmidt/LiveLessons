package search;

import utils.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * A {@link RecursiveTask} that searches a {@link Document} for a list
 * of phrases.
 */
public class SearchPhrasesInDocTask
       extends RecursiveTask<List<SearchResults>> {
    /**
     * Title of the work that's being searched.
     */
    private final String mTitle;

    /**
     * The Document to search.
     */
    private final Document mDocument;

    /**
     * The list of phrases to find.
     */
    private final List<String> mPhraseList;

    /**
     * Indicates whether to run the search concurrently.
     */
    private final boolean mParallelSearching;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    private final boolean mParallelPhrases;

    /**
     * Constructor initializes the field.
     */
    SearchPhrasesInDocTask(String title,
                           Document document,
                           List<String> phraseList,
                           boolean parallelSearching,
                           boolean parallelPhrases) {
        mTitle = title;
        mDocument = document;
        mPhraseList = phraseList;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
    }

    /**
     * This method searches a {@link Document} for all occurrences of
     * the phrases to find.
     */
    @Override
    public List<SearchResults> compute() {
        if (mParallelPhrases)
            return computeParallel();
        else
            return computeSequential();
    }

    /**
     * Perform the computations in parallel.
     *
     * @return A list of search results that matched the phrases
     */
    private List<SearchResults> computeParallel() {
        // The list of fork-join tasks to run in parallel.
        List<ForkJoinTask<SearchResults>> forks =
            new ArrayList<>();

        // The list of results.
        List<SearchResults> results = 
            new ArrayList<>();

        // Iterate through all the phrases in the document.
        for (String phrase : mPhraseList) 
            // Use a PhraseMatchTask to add the indices of all places
            // in the inputData where phrase matches.
            forks.add(new PhraseMatchTask(mTitle,
                                          mDocument.getContents(),
                                          phrase,
                                          mParallelSearching).fork());

        // Join all the results.
        for (ForkJoinTask<SearchResults> task : forks) {
            SearchResults searchResults = task.join();
            
            if (!searchResults.isEmpty()) {
                // If phrase was found add it to the list of results.
                results.add(searchResults);
            } 
        }

        // Return the results.
        return results;
    }

    /**
     * Perform the computations sequentially.
     *
     * @return A list of search results that matched the phrases
     */
    private List<SearchResults> computeSequential() {
        // The list of results.
        List<SearchResults> results = 
            new ArrayList<>();

        // Iterate through all the phrases in the document.
        for (String phrase : mPhraseList) {
            // Use a PhraseMatchTask to add the indices of all places
            // in the inputData where phrase matches.
            SearchResults searchResults =
                new PhraseMatchTask(mTitle,
                                    mDocument.getContents(),
                                    phrase,
                                    mParallelSearching).compute();

            if (!searchResults.isEmpty())
                // If phrase was found add it to the list of results.
                results.add(searchResults);
        }

        // Return the results.
        return results;
    }
}

