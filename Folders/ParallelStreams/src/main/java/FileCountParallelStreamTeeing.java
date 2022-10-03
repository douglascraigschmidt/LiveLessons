import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * This class uses the Java parallel streams framework and a teeing
 * {@link Collector} to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountParallelStreamTeeing
       extends AbstractFileCounter {
    /**
     * This Function handles a document.
     */
    Function<File, Long> mHandleDocument = entry ->
        // Is entry a document?
        entry.isFile()

        // Handle a document
        ? handleDocument(entry)

        // Return 0.
        : 0L;

    /**
     * This Function handles a folder.
     */
    Function<File, Long> mHandleFolder = entry ->
        // Is entry a folder?
        !entry.isFile()

        // Handle a folder.
        ? handleFolder(entry,
                       mDocumentCount,
                       mFolderCount,
                       // A factory that creates a
                       // FileCountParallelStreamTeeing object.
                       FileCountParallelStreamTeeing::new)

        // Return 0.
        : 0L;

    /**
     * Constructor initializes the fields.
     */
    FileCountParallelStreamTeeing(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCountParallelStreamTeeing(File file,
                                          AtomicLong documentCount,
                                          AtomicLong folderCount) {
        super(file, documentCount, folderCount);
    }

    /**
     * @return The size in bytes of the file, as well as all
     *         the files in folders reachable from this file
     */
    @Override
    protected long compute() {
        return Arrays
            // Convert the list of files into a stream of files.
            .stream(mFile.listFiles())

            // Convert the sequential stream to a parallel stream.
            .parallel()

            // Collect the results into a single Long value.
            .collect(// Use the teeing collector to process each entry
                     // according to its type.
                     teeing(
                            // Handle documents.
                            mapping(mHandleDocument,
                                    // Sum document results together.
                                    summingLong(Long::longValue)),

                            // Handle folders.
                            mapping(mHandleFolder,
                                    // Sum folder results together.
                                    summingLong(Long::longValue)),

                            // Sum both document and folder results
                            // together.
                            Long::sum));
    }
}

