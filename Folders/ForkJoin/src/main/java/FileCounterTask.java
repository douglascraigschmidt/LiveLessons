import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This task computes the size in bytes of the file (or all the files
 * associated with subdirectory).
 */
public class FileCounterTask extends RecursiveTask<Long> {
    /**
     * The current file that's being analyzed.
     */
    private final File mFile;

    /**
     * Keeps track of the total number of documents encountered.
     */
    private static final AtomicLong sDocumentCount = new AtomicLong(0);

    /**
     * Keeps track of the total number of folders encountered.
     */
    private static final AtomicLong sFolderCount = new AtomicLong(0);

    /**
     * Constructor initializes the file.
     */
    FileCounterTask(File file) {
        mFile = file;
    }

    /**
     * @return The number of documents counted during the recursive traversal.
     */
    public long documentCount() {
        return sDocumentCount.get();
    }

    /**
     * @return The number of folders counted during the recursive traversal.
     */
    public long folderCount() {
        return sFolderCount.get();
    }

    /**
     * This hook method returns the size in bytes of the file (or
     * all the files associated with subdirectory).
     */
    @Override
    protected Long compute() {
        // Determine if mFile is a file (vs. a directory).
        if (mFile.isFile()) {
            // Increment the count of documents.
            sDocumentCount.incrementAndGet();

            // Return the length of the file.
            return mFile.length();
        } else {
            // Create a list of tasks to fork to process a folder.
            List<ForkJoinTask<Long>> forks = Stream
                // Convert the list of files into a stream of files.
                .of(Objects.requireNonNull(mFile.listFiles()))

                .peek(file -> {
                    if (file.isDirectory())
                        sFolderCount.incrementAndGet();
                })

                // Map each file into a FileTask and fork it.
                .map(temp -> new FileCounterTask(temp).fork())

                // Trigger intermediate operation processing and
                // collect the results into a list.
                .collect(toList());

            return forks
                // Convert the list to a stream.
                .stream()
                    
                // Join the tasks.
                .mapToLong(ForkJoinTask::join)

                // Sum the sizes of all the files.
                .sum();
        }
    }
}

