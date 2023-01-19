package search;

import utils.Document;
import utils.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Implements RecursiveTask and uses the fork-join framework to search
 * for list of phrases in documents in a given work of Shakespeare.
 */
class SearchDocsInWorkTask
      extends RecursiveTask<List<SearchResults>> {
    /**
     * The recursive directory folder containing a particular work
     * of Shakespeare.
     */
    private final Folder mWork;
        
    /**
     * The list of phrases to find in the work.
     */
    private final List<String> mPhrasesToFind;

    /**
     * Indicates whether to search for a phrase in each work
     * in parallel.
     */
    private final boolean mParallelSearching;

    /**
     * Indicates whether to processes the phrases in parallel.
     */
    private final boolean mParallelPhrases;

    /**
     * Indicates whether to processes the docs in parallel.
     */
    private final boolean mParallelDocument;

    /**
     * Constructor initializes the field.
     */
    SearchDocsInWorkTask(Folder work,
                         List<String> phrasesToFind,
                         boolean parallelSearching,
                         boolean parallelPhrases,
                         boolean parallelDocument) {
        mWork = work;
        mPhrasesToFind = phrasesToFind;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mParallelDocument = parallelDocument;
    }

    /**
     * Searches one work to find all occurrences of the given
     * phrases.
     */
    @Override
    protected List<SearchResults> compute() {
        // Get the folder with the text for all the docs in the work.
        List<Folder> docsFolder = mWork
            .getSubFolders();
            
        // Start by adding the intro document.
        List<Document> docs =
            mWork.getDocuments();

        // Then add all the act documents if there are any.
        if (docsFolder.size() > 0)
            docs.addAll(docsFolder.get(0).getDocuments());
            
        if (mParallelDocument)
            return computeParallel(docs);
        else
            return computeSequential(docs);
    }

    /**
     * Search for the docs in the work in parallel using the fork-join pool.
     */
    private List<SearchResults> computeParallel(List<Document> docs) {
        // The list of fork-join tasks to run in parallel.
        List<ForkJoinTask<List<SearchResults>>> forks =
            new ArrayList<>();

        // The list of results.
        List<SearchResults> results = 
            new ArrayList<>();

        // Iterate through all the documents in the work.
        for (Document doc : docs)
            // Convert each Document to a SearchPhrasesInDocTask and
            // fork it to run in parallel.
            forks.add(new SearchPhrasesInDocTask(mWork.getName(),
                                                 doc,
                                                 mPhrasesToFind,
                                                 mParallelSearching,
                                                 mParallelPhrases).fork());

        // Join all the results.
        for (ForkJoinTask<List<SearchResults>> task : forks)
            results.addAll(task.join());

        // Return the results.
        return results;
    }

    /**
     * Search the docs in the work sequentially.
     */
    private List<SearchResults> computeSequential(List<Document> docs) {
        // The list of results.
        List<SearchResults> results = 
            new ArrayList<>();

        // Iterate through all the documents in the work.
        for (Document doc : docs)
            // Convert each Document to a SearchPhrasesInDocTask and
            // compute it.
            results.addAll(new SearchPhrasesInDocTask(mWork.getName(),
                                                      doc,
                                                      mPhrasesToFind,
                                                      mParallelSearching,
                                                      mParallelPhrases).compute());
        // Return the results.
        return results;
    }
}

