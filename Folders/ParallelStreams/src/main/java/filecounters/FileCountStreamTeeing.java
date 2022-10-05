package filecounters;

import java.io.File;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static java.util.stream.Collectors.*;

/**
 * This class uses the Java streams framework and a teeing {@link
 * Collector} to count the number of folders and documents in a
 * (large) recursive folder hierarchy, as well as calculate the
 * cumulative size in bytes of all the documents.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountStreamTeeing
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
             FileCountStreamTeeing::new),
            // Combiner.
            (a, b) -> { a[0] += b[0]; return a; },
            // Finisher.
            a -> a[0]);

    /**
     * Constructor initializes the fields.
     */
    public FileCountStreamTeeing(File file,
                                 boolean parallel) {
        super(file, parallel);
    }

    /**
     * Constructor initializes the fields.
     */
    public FileCountStreamTeeing(File file) {
        super(file);
    }

    /**
     * @return The size in bytes of the file, as well as all
     *         the files in folders reachable from this file
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


