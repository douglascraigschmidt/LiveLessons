package counters;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * This task uses the Java parallel streams framework to compute the
 * size in bytes of a given file, as well as all the files in folders
 * reachable from this file.
 */
public class FileCounterParallelStream
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    public FileCounterParallelStream(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCounterParallelStream(File file,
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

            return Stream
                // Convert the List to a sequential stream.
                .of(Objects
                    .requireNonNull(mFile
                                    // Get the List of files.
                                    .listFiles()))

                // Convert to a parallel stream.
                .parallel()

                // Map each "file" recursively into a
                // FileCounterParallelStream.
                .mapToLong(temp -> new FileCounterParallelStream
                           (temp,
                            mDocumentCount,
                            mFolderCount).compute())

                // Sum the sizes of all the files.
                .sum();
        }
    }
}

