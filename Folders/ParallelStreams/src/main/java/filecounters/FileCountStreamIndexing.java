package filecounters;

import java.io.File;
import java.util.List;
import java.util.function.ToLongFunction;

/**
 * This class uses the Java streams framework and direct indexing into
 * an array of two {@link ToLongFunction} objects to count the number
 * of folders and documents in a (large) recursive folder hierarchy,
 * as well as calculate the cumulative size in bytes of all the
 * documents.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountStreamIndexing
       extends AbstractFileCounter {
    /**
     * This two element array is used to optimize the {@code
     * compute()} method below!
     */
    private final ToLongFunction<File>[] mOps = new ToLongFunction[] {
        // Count the number of bytes in a recursive folder.
        folder -> handleFolder((File) folder,
                               // A factory that creates a
                               // FileCountStreamIndex object.
                               FileCountStreamIndexing::new),

        // Count the number of bytes in a document.
        file -> handleDocument((File) file)
    };

    /**
     * Constructor initializes the fields.
     */
    public FileCountStreamIndexing(File file,
                                   boolean parallel) {
        super(file, parallel);
    }

    /**
     * Constructor initializes the fields.
     */
    private FileCountStreamIndexing(File file) {
        super(file);
    }

    /**
     * @return The size in bytes of the file, as well as all the files
     *         in folders reachable from this file
     */
    @Override
    public long compute() {
        var list = List
            // Convert file array into a List of files.
            .of(mFile.listFiles());

        return // Create either a parallel or sequential stream.
            (sParallel
             ? list.parallelStream()
             : list.stream())

            // Process each file according to its type.
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

