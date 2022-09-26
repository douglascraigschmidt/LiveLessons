import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

/**
 * This task uses the Java fork-join framework and the parallel
 * streams framework to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
public class FileCountParallelStreamIndex
       extends AbstractFileCounter {
    /**
     * This two element array is used to optimize the
     * {@code compute} method below!
     */
    private final ToLongFunction<File>[] mOps = new ToLongFunction[] {
            // Count the number of bypes in a document.
            file -> handleDocument((File) file),

            // Count the number of bypes in a recursive folder.
            folder -> handleFolder((File) folder,
                    mDocumentCount,
                    mFolderCount,
                    FileCountParallelStreamIndex::new)
    };

    /**
     * Constructor initializes the fields.
     */
    FileCountParallelStreamIndex(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    public FileCountParallelStreamIndex(File file,
                                        AtomicLong documentCount,
                                        AtomicLong folderCount) {
        super(file, documentCount, folderCount);
    }

    /**
     * @return The size in bytes of the file, as well as all the files
     * in folders reachable from this file
     */
    @Override
    protected long compute() {
        return Stream
            // Convert file list into a sequential stream of files.
            .of(Objects.requireNonNull(mFile.listFiles()))

            // Convert the sequential stream to a parallel stream.
            .parallel()

            // Get the number of bytes for a document or a recursive
            // folder.
            .mapToLong(entry ->
                       // Index to the right array entry and call
                       // the method.
                       mOps[entry.isFile() ? 0 : 1].applyAsLong(entry))

            // Sum all the results together.
            .sum();
    }
}

