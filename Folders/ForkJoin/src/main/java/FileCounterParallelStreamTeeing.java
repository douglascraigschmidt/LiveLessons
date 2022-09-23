import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * This task uses the Java fork-join framework and the parallel
 * streams framework to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
public class FileCounterParallelStreamTeeing
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    FileCounterParallelStreamTeeing(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCounterParallelStreamTeeing(File file,
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

            //
            .collect(// Use the teeing collector to process each
                     // entry according to its type.
                     teeing
                     (mapping(entry ->
                              // Is entry a document?
                              entry.isFile()
                              ? handleDocument(entry)
                              : 0L,
                              summingLong(Long::longValue)),
                      mapping(entry ->
                              // Is entry a folder?
                              !entry.isFile()
                              ? handleFolder(entry, 
                                             () ->
                                             new FileCounterParallelStreamTeeing((File) entry,
                                                                                 mDocumentCount,
                                                                                 mFolderCount).compute())
                              : 0L,
                              summingLong(Long::longValue)),
                      Long::sum));
    }
}

