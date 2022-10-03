import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class uses the Java parallel streams framework and direct
 * indexing into an array of two {@link ToLongFunction} objects to
 * compute the size in bytes of a given file, as well as all the files
 * in folders reachable from this file.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountParallelStreamIndex
       extends AbstractFileCounter {
    /**
     * This two element array is used to optimize the {@code
     * compute()} method below!
     */
    private final ToLongFunction<File>[] mOps = new ToLongFunction[] {
        // Count the number of bytes in a recursive folder.
        folder -> handleFolder((File) folder,
                               mDocumentCount,
                               mFolderCount,
                               // A factory that creates a
                               // FileCountParallelStreamIndex object.
                               FileCountParallelStreamIndex::new),

        // Count the number of bytes in a document.
        file -> handleDocument((File) file)
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
     *         in folders reachable from this file
     */
    @Override
    protected long compute() {
        return Arrays
            // Convert file array into a List of files.
            .asList(mFile.listFiles())

            // Convert the List to a parallel stream.
            .parallelStream()

            // Get the number of bytes for a document or a recursive
            // folder.
            .mapToLong(entry ->
                       // Index into the appropriate array entry to
                       // get the right Function.
                       mOps[Boolean.compare(entry.isFile(), false)]

                       // Apply the Function.
                       .applyAsLong(entry))

            // Sum all the results together.
            .sum();
    }
}

