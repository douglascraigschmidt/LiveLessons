package filecounters;

import java.io.File;
import java.util.List;

/**
 * This class uses the Java streams framework and the Java ternary
 * operator to count the number of folders and documents in a (large)
 * recursive folder hierarchy, as well as calculate the cumulative
 * size in bytes of all the documents.
 */
@SuppressWarnings("ConstantConditions")
public class FileCountStreamTernary
       extends AbstractFileCounter {
    /**
     * Constructor initializes the fields.
     */
    public FileCountStreamTernary(File file,
                                  boolean parallel) {
        super(file, parallel);
    }

    /**
     * Constructor initializes the superclass (internal).
     */
    private FileCountStreamTernary(File file) {
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
            .mapToLong(file -> file
                       // Determine if mFile is a file (document)
                       // vs. a directory (folder).
                       .isFile()

                       // Handle a document.
                       ? handleDocument(file)

                       // Handle a folder.
                       : handleFolder(file,
                                      // A factory that creates a
                                      // filecounters.FileCountStream object.
                                      FileCountStreamTernary::new))

            // Sum the sizes of all the files.
            .sum();
    }
}

