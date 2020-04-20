import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This task uses the Java fork-join framework and the parallel
 * streams framework to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
public class FileCounterParallelStream
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    FileCounterParallelStream(File file) {
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
     * as all the files in folders reachable from this file.
     */
    @Override
    protected Long compute() {
        return Stream
            // Convert the list of files into a stream of files.
            .of(Objects.requireNonNull(mFile.listFiles()))

            // Convert the sequential stream to a parallel stream.
            .parallel()

            // Process each file according to its type.
            .mapToLong(file -> {
                    // Determine if mFile is a file (document) vs. a
                    // directory (folder).
                    if (file.isFile()) {
                        // Increment the count of documents.
                        mDocumentCount.incrementAndGet();

                        // Return the length of the file.
                        return mFile.length();
                    } else {
                        // Increment the count of folders.
                        mFolderCount.incrementAndGet();

                        // Recursively count the number of files in
                        // a (sub)folder.
                        return new FileCounterParallelStream(file,
                                                             mDocumentCount,
                                                             mFolderCount).compute();

                    }
                })

            // Sum the sizes of all the files.
            .sum();
    }
}

