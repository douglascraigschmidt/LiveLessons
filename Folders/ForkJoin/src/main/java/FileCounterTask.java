import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This task uses the Java fork-join framework and Java 7 features to compute the size in
 * bytes of a given file, as well as all the files in folders
 * reachable from this file.
 */
public class FileCounterTask
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    FileCounterTask(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCounterTask(File file,
                            AtomicLong documentCount,
                            AtomicLong folderCount) {
        super(file, documentCount, folderCount);
    }

    /**
     * This hook method returns the size in bytes of the file, as well
     * as all the files in folders reachable from this file.
     */
    @Override
    protected Long compute() {
        // Determine if mFile is a file (document) vs. a directory
        // (folder).
        if (mFile.isFile()) {
            // Increment the count of documents.
            mDocumentCount.incrementAndGet();

            // Return the length of the file.
            return mFile.length();
        } else {
            // Increment the count of folders.
            mFolderCount.incrementAndGet();

            // Create a list of tasks to fork to process the contents
            // of a folder.
            List<ForkJoinTask<Long>> forks = new ArrayList<>();

            // Loop through each file in the directory.
            for (File file : Objects.requireNonNull(mFile.listFiles()))
                // Create a FileCounterTask for each file, fork it,
                // and add it to the list.
                forks.add(new FileCounterTask(file,
                                              mDocumentCount,
                                              mFolderCount).fork());

            // Accumulator to store the partial sums.
            long sum = 0;

            // Loop through each task.
            for (ForkJoinTask<Long> task : forks)
                // Join each tasks and increment sum accordingly.
                sum += task.join();

            // Return the sum.
            return sum;
        }
    }
}

