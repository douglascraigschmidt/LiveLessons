package utils;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

import static java.lang.Integer.max;

/**
 * Represents a "work" of Shakespeare.
 */
public class Work {
    /**
     * The complete contents of the work.
     */
    private CharSequence mContents;

    /**
     * A RecursiveTask that shows how to convert a recursive directory
     * folder containing the contents of a work of Shakespeare into a
     * CharSequence containing the contents of this work.
     */
    private static class GetWorkContentsTask
            extends RecursiveTask<CharSequence> {
        /**
         * A list of folders that contain the contents of a work of
         * Shakespeare.
         */
        private List<Document> mDocsList;

        /**
         * Indicates whether to process the documents in parallel.
         */
        private final boolean mParallel;

        /**
         * The minimum size of the work list to split.
         */
        private int mMinSplitSize;

        /**
         * Constructor initializes the fields.
         */
        GetWorkContentsTask(Folder work,
                            boolean parallel) {
            // Get the folder with the text for all the docs.
            List<Folder> docsFolder = work
                .getSubFolders();

            // Start by adding the intro document to the docsList.
            mDocsList = work.getDocuments();

            // Then add all the act documents if there are any.
            if (docsFolder.size() > 0)
                mDocsList.addAll(docsFolder.get(0).getDocuments());

            mParallel = parallel;

            // Don't let mMinSplitSize fall below 1!
            mMinSplitSize = max(mDocsList.size() / 2, 1);
        }

        /**
         * This constructor is used internally by the compute()
         * method.  It initializes all the fields for the "left hand
         * size" of a split.
         */
        private GetWorkContentsTask(List<Document> docsList,
                                    boolean parallel,
                                    int minSplitSize) {
            mDocsList = docsList;
            mParallel = parallel;
            mMinSplitSize = minSplitSize;
        }

        /**
         * Perform the computations sequentially at this point.
         */
        private CharSequence computeSequentially() {
            // Used to concatenate the CharSequences together.
            StringBuilder stringBuilder = new StringBuilder();

            // Iterate through the list of documents.
            for (Document doc : mDocsList)
                // Append the contents of each document.
                stringBuilder.append(doc.getContents());

            // Return a CharSequence containing the contents of this
            // Shakespeare work.
            return stringBuilder.toString();
        }

        /**
         * Converts a recursive directory folder containing the
         * complete works of Shakespeare into a list of CharSequences
         * containing the contents of this work.
         */
        @Override
        public CharSequence compute() {
            // Check to see if we're done splitting and should now
            // compute sequentially.
            if (mDocsList.size() <= mMinSplitSize
                || !mParallel) {
                return computeSequentially();
            }
            else {
                // Compute position to split the list and forward to
                // the splitDocsList() method to perform the split.
                return splitDocsList(mDocsList.size() / 2);
            }
        }

        /**
         * Use the fork-join framework to recursively split the input
         * list and return a list of CharSequence that contain all
         * matching phrases in the input list.
         */
        private CharSequence splitDocsList(int splitPos) {
            // Create and fork a new GetWorkContentsTask that
            // concurrently handles the "left hand" part of the input,
            // while "this" handles the "right hand" part of the
            // input.
            ForkJoinTask<CharSequence> leftTask =
                forkLeftTask(splitPos, mMinSplitSize);

            // Update "this" GetWorkContentsTask to handle the "right
            // hand" portion of the input.
            CharSequence rightResult = computeRightTask(splitPos);

            // Wait and join the results from the left task.
            CharSequence leftResult = leftTask.join();

            // Used to concatenate the CharSequences together.
            StringBuilder stringBuilder = new StringBuilder();

            // Return the result.
            return stringBuilder
                // Append the left result.
                .append(leftResult)
                // Concatenate the left result with the right result.
                .append(rightResult)
                .toString();
        }

        /**
         * Compute the right task.
         */
        CharSequence computeRightTask(int splitPos) {
            // Update mDocsList to contain a sublist at the split
            // position.
            mDocsList = mDocsList.subList(splitPos,
                                          mDocsList.size());

            // Recursively call compute() to continue the splitting.
            return compute();
        }

        /**
         * Fork the right task.
         */
        ForkJoinTask<CharSequence> forkLeftTask(int splitPos,
                                                int minSplitSize) {
            // Create and fork a new task to handle the "right hand"
            // portion of the split.
            return new GetWorkContentsTask(mDocsList.subList(0,
                                                             splitPos),
                                           mParallel,
                                           minSplitSize).fork();
        }
    }

    /**
     * Constructor initializes the fields.
     */
    public Work(Folder folder,
                boolean parallel) {
        // Concatenate the contents of the intro document and all the
        // acts together.
        mContents =
            new GetWorkContentsTask(folder,
                                    parallel).compute();
        // System.out.println("first 20 characters of contents are: " + mContents.subSequence(0, 20));
    } 

    /**
     * The complete contents of the work.
     */
    public CharSequence getContents() {
        return mContents;
    }
}
