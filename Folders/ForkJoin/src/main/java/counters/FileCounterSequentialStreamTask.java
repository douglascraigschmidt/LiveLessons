package counters;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * This task uses the Java fork-join framework and the sequential
 * streams framework to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
public class FileCounterSequentialStreamTask
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    public FileCounterSequentialStreamTask(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCounterSequentialStreamTask
        (File file,
         AtomicLong documentCount,
         AtomicLong folderCount) {
        super(file, documentCount, folderCount);
    }

    /**
     * This hook method returns the size in bytes of the file, as well
     * as all the "files" in folders reachable from {@code mFile}.
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

            // Create a List of tasks to fork to process the contents
            // of a folder.
            var forks = Stream
                // Convert the List of "files" into a Stream of "files".
                .of(Objects.requireNonNull(mFile.listFiles()))

                // Map each "file" into a FileCounterSequentialStream and
                // fork it.
                .map(temp -> new FileCounterSequentialStreamTask
                     (temp,
                      mDocumentCount,
                      mFolderCount).fork())

                // Trigger intermediate operations and collect results
                // into an immutable List.
                .toList();

            return forks
                // Convert the List to a Stream.
                .stream()
                    
                // Join all the tasks.
                .mapToLong(ForkJoinTask::join)

                // Sum the sizes of all the "files".
                .sum();
        }
    }
}

