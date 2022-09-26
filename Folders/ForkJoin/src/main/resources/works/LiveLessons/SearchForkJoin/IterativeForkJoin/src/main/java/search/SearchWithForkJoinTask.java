package search;

import utils.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Implements RecursiveTask and uses the Java fork-join framework to
 * search for phrases in a directory containing the complete works of
 * Shakespeare.
 */
public class SearchWithForkJoinTask
       extends RecursiveTask<List<List<SearchResults>>> {
    /**
     * The recursive directory folder containing the complete works of
     * Shakespeare.
     */
    private final Folder mRootFolder;

    /**
     * The list of phrases to find.
     */
    private final List<String> mPhrasesToFind;

    /**
     * Indicates whether to search for a phrase in each string
     * in parallel.
     */
    private final boolean mParallelSearching;

    /**
     * Indicates whether to process the phrases in parallel.
     */
    private final boolean mParallelPhrases;

    /**
     * Indicates whether to process the documents in parallel.
     */
    private final boolean mParallelDocs;

    /**
     * Indicates whether to process the works in parallel.
     */
    private final boolean mParallelWorks;

    /**
     * Constructor initializes the fields.
     */
    public SearchWithForkJoinTask(Folder folder,
                                  List<String> phrasesToFind,
                                  boolean parallelSearching,
                                  boolean parallelPhrases,
                                  boolean parallelDocs,
                                  boolean parallelWorks) {
        mRootFolder = folder;
        mPhrasesToFind = phrasesToFind;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mParallelDocs = parallelDocs;
        mParallelWorks = parallelWorks;
    }

    /**
     * Search for phrases to find starting at the root folder.
     */
    @Override
    protected List<List<SearchResults>> compute() {
        // Get a list of all folders in root directory.
        List<Folder> subFolders = mRootFolder
            .getSubFolders();

        if (mParallelWorks)
            return computeParallel(subFolders);
        else
            return computeSequential(subFolders);
    }

    /**
     * Search for phrases in parallel using the fork-join pool.
     */
    private List<List<SearchResults>> computeParallel(List<Folder> subFolders) {
        // The list of fork-join tasks to run in parallel.
        List<ForkJoinTask<List<SearchResults>>> forks =
            new ArrayList<>();

        // The list of results.
        List<List<SearchResults>> results = 
            new ArrayList<>();
                
        // Iterate through all works of Shakespeare.
        for (Folder work : subFolders)
            // Map each work in the folder to a SearchDocsInWorkTask
            // and fork it to run in parallel.
            forks.add(new SearchDocsInWorkTask(work,
                                               mPhrasesToFind,
                                               mParallelSearching,
                                               mParallelPhrases,
                                               mParallelDocs).fork());

        // Join all the results.
        for (ForkJoinTask<List<SearchResults>> task : forks)
            results.add(task.join());

        // Return the results.
        return results;
    }

    /**
     * Search for phrases sequentially.
     */
    private List<List<SearchResults>> computeSequential(List<Folder> subFolders) {
        // The list of results.
        List<List<SearchResults>> results = 
            new ArrayList<>();
                
        // Iterate through all works of Shakespeare.
        for (Folder work : subFolders)
            // Map each work in the folder to a SearchDocsInWorkTask and
            // compute it.
            results.add(new SearchDocsInWorkTask(work,
                                                     mPhrasesToFind,
                                                     mParallelSearching,
                                                     mParallelPhrases,
                                                     mParallelDocs).compute());

        // Return the results.
        return results;
    }
}
