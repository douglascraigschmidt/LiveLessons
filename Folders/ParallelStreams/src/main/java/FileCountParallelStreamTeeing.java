import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
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
     * This {@link Collector} handles a {@link Document}.
     */
    Collector<File, ?, Long> documentCollector = Collector
        .of(() -> new long[1],
            (a, f) -> a[0] += handleDocument(f),
            (a, b) -> {
                a[0] += b[0];
                return a;
            },
            a -> a[0]);

    /**
     * This {@link Collector} handles a {@link Folder}.
     */
    Collector<File, ?, Long> folderCollector = Collector
        .of(() -> new long[1],
            (a, f) -> a[0] += handleFolder(f,
                                           mDocumentCount,
                                           mFolderCount,
                                           FileCountParallelStreamTeeing::new),
            (a, b) -> {
                a[0] += b[0];
                return a;
            },
            a -> a[0]);

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
                     teeing(// Handle documents.
                            filtering(File::isFile, 
                                      documentCollector),

                            // Handle folders.
                            filtering(Predicate.not(File::isFile), 
                                      folderCollector),

                            // Sum both document and folder results
                            // together.
                            Long::sum));
    }
}


