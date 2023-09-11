package counters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This task uses the Java fork-join framework and Java 7 features to
 * compute the size in bytes of a given file, as well as all the files
 * in folders reachable from a "file" that is a folder.
 */
public class FileCounterForkJoinTask
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    public FileCounterForkJoinTask(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCounterForkJoinTask(File file,
                                    AtomicLong documentCount,
                                    AtomicLong folderCount) {
        super(file, documentCount, folderCount);
    }

    /**
     * @return The size in bytes of the file plus all the files
     * in folders (recursively) reachable from this file.
     */
    @Override
    protected Long compute() {
        // Determine if mFile is a file (document) vs. a directory
        // (folder). This is the base case of the recursion.
        if (mFile.isFile()) {
            // Increment the count of documents.
            mDocumentCount.incrementAndGet();

            // Return the length of the file.
            return mFile.length();
        } else {
            // Increment the count of folders.
            mFolderCount.incrementAndGet();

            // Create a List of tasks to fork to process the contents
            // of a folder in parallel.
            List<ForkJoinTask<Long>> forks = new ArrayList<>();

            // Use a for-each loop to iterate thru each "file" in the
            // directory.
            for (File file : Objects.requireNonNull(mFile.listFiles()))
                // Create a FileCounterForkJoinTask for each file, fork it,
                // and add it to the List.
                forks.add(new FileCounterForkJoinTask(file,
                                                      mDocumentCount,
                                                      mFolderCount).fork());

            // Accumulator to store the partial sums.
            long sum = 0;

            // Use a for-each loop to iterate thru each task.
            for (var task : forks)
                // Join each task and increment count accordingly.
                sum += task.join();

            // Return the sum of the total number of bytes of all
            // files recursively reachable from this file.
            return sum;
        }
    }
}

