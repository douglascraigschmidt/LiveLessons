package search;

import utils.Document;
import utils.Folder;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static utils.StreamsUtils.PentaFunction;

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
            List<Document> docs = mWork.getDocuments();

            // Then add all the act documents if there are any.
            if (docsFolder.size() > 0)
                docs.addAll(docsFolder.get(0).getDocuments());
            
            // Create a stream for all the documents to search.
            Stream<Document> docsStream = docs
                .stream();

            // Convert to a parallel stream if requested.
            if (mParallelInput)
                docsStream.parallel();

            /*
            return docsStream
                // Convert each Document to a SearchForPhrasesTask and
                // fork it to run in parallel.
                .map(doc
                     -> new SearchForPhrasesTask(mWork.getName(),
                                                 doc.getContents(),
                                                 mPhrasesToFind,
                                                 mParallelSearching,
                                                 mParallelPhrases).fork())

                // Join all the SearchForPhrasesTask results.
                .map(ForkJoinTask::join)

                // Flatten the results of join() into a single stream
                // containing all the search results for this work.
                .flatMap(List::stream)

                // Trigger intermediate operations and return a list.
                .collect(toList());
            */

            // Create a list of all the tasks that search for phrases
            // in the docs comprising a work.
            List<SearchForPhrasesTask> docsList = docsStream
                // Convert each Document object to a
                // SearchForPhrasesTask object.
                .map(doc
                     -> new SearchForPhrasesTask(mWork.getName(),
                                                 doc.getContents(),
                                                 mPhrasesToFind,
                                                 mParallelSearching,
                                                 mParallelPhrases))

                // Trigger intermediate operation processing and
                // return a list.
                .collect(toList());

            Stream<SearchForPhrasesTask> resultsStream = invokeAll(docsList)
                // Convert the list into a stream.
                .stream();

            if (mParallelInput)
                resultsStream.parallel();

            // Return a list of SearchResults corresponding to this
            // work.  The invokeAll() method forks all the tasks in
            // the docsList and returns a collection once all the
            // tasks are done.
            return resultsStream
                // Join all the SearchForPhrasesTask results.
                .map(RecursiveTask::join)

                // Flatten the results of join() into a single stream
                // containing all the search results for this work.
                .flatMap(List::stream)

                // Trigger intermediate operation processing and
                // return a list.
                .collect(toList());
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
            // Create a stream containing all the works of
            // Shakespeare.
            Stream<Folder> worksStream = mRootFolder
                // Get a list of all the folders in the root
                // directory.
                .getSubFolders()

                // Conver the list into a stream.
                .stream();

            // Convert to a parallel stream if requested.
            if (mParallelInput)
                worksStream.parallel();

            /*
            // Return a list of lists of search results - one list for
            // each work of Shakespeare.
            return worksStream
                // Map each work to a WorkSearchTask and fork it to
                // run in parallel.
                .map(work
                     -> new WorkSearchTask(work).fork())

                // Join with each forked task.
                .map(ForkJoinTask::join)
                
                // Trigger intermediate operation processing and
                // return a list.
                .collect(toList());
            */

            // Create a list of WorkSearchTasks.
            List<WorkSearchTask> worksList = worksStream
                // Map each work of Shakespeare in the list into a
                // new WorkSearchTask.
                .map(WorkSearchTask::new)

                // Trigger intermediate operation processing and
                // return a list.
                .collect(toList());

            // Return a list of lists of search results - one list for
            // each work of Shakespeare.  The invokeAll() method forks
            // all the tasks in the worksList and returns a collection
            // once all the tasks are done.
            return invokeAll(worksList)
                // Convert the list into a stream.
                .stream()

                // Join all the WorkSearchTask results.
                .map(RecursiveTask::join)

                // Trigger intermediate operation processing and
                // return a list.
                .collect(toList());
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
