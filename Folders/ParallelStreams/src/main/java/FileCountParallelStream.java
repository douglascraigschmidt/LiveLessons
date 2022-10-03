import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * This class uses the Java parallel streams framework and the Java
 * ternary operator to compute the size in bytes of a given file, as
 * well as all the files in folders reachable from this file.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountParallelStream
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    FileCountParallelStream(File file) {
        super(file);
    }

    /**
     * Constructor initializes the fields (used internally).
     */
    private FileCountParallelStream(File file,
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
        return Stream
            // Convert the array of files into a stream of files.
            .of(mFile.listFiles())

            // Convert the sequential stream to a parallel stream.
            .parallel()

            // Process each file according to its type.
            .mapToLong(file -> file
                       // Determine if mFile is a file (document)
                       // vs. a directory (folder).
                       .isFile()

                       // Handle a document.
                       ? handleDocument(file)

                       // Handle a folder.
                       : handleFolder(file,
                                      mDocumentCount,
                                      mFolderCount,
                                      // A factory that creates a
                                      // FileCountParallelStream
                                      // object.
                                      FileCountParallelStream::new))

            // Sum the sizes of all the files.
            .sum();
    }
}

