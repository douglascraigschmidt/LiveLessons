import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

/**
 * This task uses the Java fork-join framework and the parallel
 * streams framework to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
public class FileCounterParallelStreamIndex
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    FileCounterParallelStreamIndex(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCounterParallelStreamIndex(File file,
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
        ToLongFunction<File>[] ops = new ToLongFunction[] {
            file -> handleDocument((File) file),
            folder -> handleFolder((File) folder,
                    () ->
                            new FileCounterParallelStreamIndex((File) folder,
                            mDocumentCount,
                            mFolderCount).compute())
        };

        return Stream
            // Convert the list of files into a stream of files.
            .of(Objects.requireNonNull(mFile.listFiles()))

            // Convert the sequential stream to a parallel stream.
            .parallel()

            //
            .mapToLong(entry ->
                       ops[entry.isFile() ? 0 : 1].applyAsLong(entry))

            // Sum all the results together.
            .sum();
    }
}

