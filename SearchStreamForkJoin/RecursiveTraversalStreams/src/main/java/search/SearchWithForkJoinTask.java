package search;

import utils.Document;
import utils.Folder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Implements RecursiveTask and shows how the Java fork-join framework
 * can be used with Java 8 streams to search for phrases in a
 * directory containing the complete works of Shakespeare.
 */
public class SearchWithForkJoinTask
       extends RecursiveTask<List<List<SearchResults>>> {
    /**
     * The recursive directory folder containing the complete works of
     * Shakespeare.
     */
    private final Folder mFolder;

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
     * Indicates whether to processes the phrases in parallel.
     */
    private final boolean mParallelPhrases;

    /**
     * Indicates whether to process the input in parallel.
     */
    private final boolean mParallelInput;

    /**
     * Constructor initializes the fields.
     */
    public SearchWithForkJoinTask(Folder folder,
                                  List<String> phrasesToFind,
                                  boolean parallelSearching,
                                  boolean parallelPhrases,
                                  boolean parallelInput) {
        mFolder = folder;
        mPhrasesToFind = phrasesToFind;
        mParallelSearching = parallelSearching;
        mParallelPhrases = parallelPhrases;
        mParallelInput = parallelInput;
    }

    /**
     * Implements RecursiveTask to show how to search for list of
     * phrases in a given work of Shakespeare.
     */
    class WorkSearchTask
          extends RecursiveTask<List<SearchResults>> {
        /**
         * The recursive directory folder containing a particular work
         * of Shakespeare.
         */
        private final Folder mWork;
        
        /**
         * Constructor initializes the field.
         */
        WorkSearchTask(Folder work) {
            mWork = work;
        }

        /**
         * Searches one work to find all occurrences of the given
         * phrases.
         */
        @Override
        protected List<SearchResults> compute() {
            // Get the folder with the text for all the docs.
            List<Folder> docsFolder = mWork
                .getSubFolders();
            
            // Start by adding the intro document.
            List<Document> docs = 
                mWork.getDocuments();

            // Then add all the act documents if there are any.
            if (docsFolder.size() > 0)
                docs.addAll(docsFolder.get(0).getDocuments());
            
            // The list of fork-join tasks to run in parallel.
            List<ForkJoinTask<List<SearchResults>>> forks =
                new ArrayList<>();

            // The list of results.
            List<SearchResults> results = 
                new ArrayList<>();

            for (Document doc : mWork.getDocuments())
                 // Convert each Document to a SearchForPhrasesTask and
                 // fork it to run in parallel.
                 forks.add(new SearchForPhrasesTask(mWork.getName(),
                                                    doc.getContents(),
                                                    mPhrasesToFind,
                                                    mParallelSearching,
                                                    mParallelPhrases).fork());

            // Join all the results.
            for (ForkJoinTask<List<SearchResults>> task : forks)
                results.addAll(task.join());

            // Return the results.
            return results;
        }
    }

    /**
     * Searches all the works of Shakespeare to find all occurrences
     * of the given phrases.
     */
    class MasterSearchTask
          extends RecursiveTask<List<List<SearchResults>>> {
        /**
         * The recursive directory folder containing the complete
         * works of Shakespeare.
         */
        private final Folder mRootFolder;
       
        /**
         * Constructor initializes the field.
         */
        MasterSearchTask(Folder rootFolder) {
            mRootFolder = rootFolder;
        }

        /**
         * Searches all works to find all occurrences of the given
         * phrases.
         *
         * @return A list of lists of search results - one list for
         * each work of Shakespeare
         */
        @Override
        protected List<List<SearchResults>> compute() {
            // The list of fork-join tasks to run in parallel.
            List<ForkJoinTask<List<SearchResults>>> forks =
                new ArrayList<>();

            // The list of results.
            List<List<SearchResults>> results = 
                new ArrayList<>();
                
            // Create a stream containing all the works of
            // Shakespeare.
            for (Folder work : mRootFolder
                     // Get a list of all folders in root directory.
                     .getSubFolders())
                // Map each work in the folder to a WorkSearchTask and
                // fork it to run in parallel.
                forks.add(new WorkSearchTask(work).fork());

            // Join all the results.
            for (ForkJoinTask<List<SearchResults>> task : forks)
                results.add(task.join());

            // Return the results.
            return results;
        }
    }
        
    /**
     * Search for phrases to find starting at the root folder.
     */
    @Override
    protected List<List<SearchResults>> compute() {
        // Start at the root of the recursive directory structure.
        return new MasterSearchTask(mFolder).compute();
    }
}
