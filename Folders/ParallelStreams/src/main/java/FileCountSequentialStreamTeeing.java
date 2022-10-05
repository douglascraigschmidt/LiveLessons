import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collectors.*;

/**
 * This class uses the Java sequential streams framework and a teeing
 * {@link Collector} to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountSequentialStreamTeeing
       extends AbstractFileCounter {
    /**
     * This {@link Collector} handles a document.
     */
    Collector<File, ?, Long> mDocumentCollector = Collector
        .of(// Supplier.
            () -> new long[1],
            // Accumulator.
            (a, f) -> a[0] += handleDocument(f),
            // Combiner.
            (a, b) -> { a[0] += b[0]; return a; },
            // Finisher.
            a -> a[0]);

    /**
     * This {@link Collector} handles a folder.
     */
    Collector<File, ?, Long> mFolderCollector = Collector
        .of(// Supplier
            () -> new long[1],
            // Accumulator.
            (a, f) -> a[0] += handleFolder
            (f,
             mDocumentCount,
             mFolderCount,
             FileCountSequentialStreamTeeing::new),
            // Combiner.
            (a, b) -> { a[0] += b[0]; return a; },
            // Finisher.
            a -> a[0]);

    /**
     * Constructor initializes the fields.
     */
    FileCountSequentialStreamTeeing(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCountSequentialStreamTeeing(File file,
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
            // Convert the list of files into a sequential stream of
            // files.
            .stream(mFile.listFiles())

            // Collect the results into a single Long value.
            .collect(// Use the teeing collector to process each entry
                     // according to its type.
                     teeing(// Handle documents.
                            filtering(File::isFile,
                                      mDocumentCollector),

                            // Handle folders.
                            filtering(Predicate.not(File::isFile),
                                      mFolderCollector),

                            // Sum both document and folder results
                            // together.
                            Long::sum));
    }
}


