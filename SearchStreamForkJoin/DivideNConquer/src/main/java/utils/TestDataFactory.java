package utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * This utility class contains methods for obtaining test data.
 */
public class TestDataFactory {
    /**
     * A utility class should always define a private constructor.
     */
    private TestDataFactory() {
    }

    /**
     * Return a folder object that's used to search a recursive
     * directory containing the complete works of William Shakespeare.
     */
    private static Folder getRootFolder(String rootFolderName,
                                        boolean parallel)
        throws URISyntaxException, IOException {
        return Folder
            .fromDirectory(new File(ClassLoader
                                    .getSystemResource(rootFolderName)
                                    .toURI()),
                           parallel);
    }

    /**
     * A RecursiveTask that shows how to convert a recursive directory
     * folder containing the complete works of Shakespeare into a list
     * of CharSequences containing the contents of this work.
     */
    private static class GetInputListTask
            extends RecursiveTask<List<CharSequence>> {
        /**
         * A list of folders containing the works of Shakespeare.
         */
        private List<Folder> mWorkList;

        /**
         * Indicates whether to process the folders and documents in
         * parallel.
         */
        private final boolean mParallel;

        /**
         * The minimum size of the work list to split.
         */
        private int mMinSplitSize;

        /**
         * Constructor initializes the fields.
         */
        GetInputListTask(List<Folder> workList,
                         boolean parallel) {
            mWorkList = workList;
            mParallel = parallel;
            mMinSplitSize = workList.size() / 2;
        }

        /**
         * This constructor is used internally by the compute()
         * method.  It initializes all the fields for the "left hand
         * size" of a split.
         */
        private GetInputListTask(List<Folder> workList,
                                 boolean parallel,
                                 int minSplitSize) {
            mWorkList = workList;
            mParallel = parallel;
            mMinSplitSize = minSplitSize;
        }

        /**
         * Perform the computations sequentially at this point.
         */
        private List<CharSequence> computeSequentially() {
            // Return a list of CharSequences, one for each work of
            // Shakespeare.
            return mWorkList
                // Convert the list into a stream.
                .stream()

                // Map each Folder object to a Work object and then
                // get the contents of the work as a CharSequence.
                .map((Folder folder)
                     -> new Work(folder, 
                                 mParallel).getContents())

                // Trigger intermediate operations and return a list.
                .collect(toList());
        }

        /**
         * Converts a recursive directory folder containing the
         * complete works of Shakespeare into a list of CharSequences
         * containing the contents of this work.
         */
        @Override
        public List<CharSequence> compute() {
            // Check to see if we're done spliting and should now
            // compute sequentially.
            if (mWorkList.size() < mMinSplitSize
                || !mParallel)
                return computeSequentially();
            else 
                // Compute position to split the list and forward to
                // the splitWorkList() method to perform the split.
                return splitWorkList(mWorkList.size() / 2);
        }

        /**
         * Use the fork-join framework to recursively split the input
         * list and return a list of CharSequence that contain all
         * matching phrases in the input list.
         */
        private List<CharSequence> splitWorkList(int splitPos) {
            // Create and fork a new GetInputListTask that
            // concurrently handles the "left hand" part of the input,
            // while "this" handles the "right hand" part of the
            // input.
            ForkJoinTask<List<CharSequence>> leftTask =
                forkLeftTask(splitPos, mMinSplitSize);

            // Update "this" GetInputListTask to handle the "right
            // hand" portion of the input.
            List<CharSequence> rightResult =
                computeRightTask(splitPos);

            // Wait and join the results from the left task.
            List<CharSequence> leftResult = leftTask.join();

            // Concatenate the left result with the right result.
            leftResult.addAll(rightResult);

            // Return the result.
            return leftResult;
        }

        /**
         * Compute the right task.
         */
        List<CharSequence> computeRightTask(int splitPos) {
            // Update mWorkList to contain a sublist at the split
            // position.
            mWorkList = mWorkList.subList(splitPos,
                                          mWorkList.size());

            // Recursively call compute() to continue the splitting.
            return compute();
        }

        /**
         * Fork the right task.
         */
        ForkJoinTask<List<CharSequence>> forkLeftTask(int splitPos,
                                                      int minSplitSize) {
            // Create and fork a new task to handle the "right hand"
            // portion of the split.
            return new GetInputListTask(mWorkList.subList(0,
                                                          splitPos),
                                        mParallel,
                                        minSplitSize).fork();
        }
    }

    /**
     * @return The input data in the given @a rootFolderName as a list
     * of CharSequences.
     */
    public static List<CharSequence> getInput(String rootFolderName,
                                              boolean parallel)
            throws IOException, URISyntaxException {
        // Return the input data in the given @a rootFolderName as a
        // list of CharSequences.
        return new GetInputListTask(getRootFolder(rootFolderName,
                                                  parallel)
                                    // Get all the subfolders.
                                    .getSubFolders(),
                                    parallel).compute();
    }

    /**
     * Return the phrase list in the @a filename as a list of
     * non-empty strings.
     */
    public static List<String> getPhraseList(String filename) {
        try {
            return Files
                // Read all lines from filename.
                .lines(Paths.get(ClassLoader.getSystemResource
                                        (filename).toURI()))
                // Filter out any empty strings.
                .filter(((Predicate<String>) String::isEmpty).negate())

                // Collect the results into a string.
                .collect(toList());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
