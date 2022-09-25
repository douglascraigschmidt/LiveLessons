import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * This task uses the Java Files.walk() method and a sequential stream
 * to count the files and compute the size in bytes of all files in
 * folders reachable from the given root file.
 */
public class FileCounterWalkSequentialStream
       extends AbstractFileCounter {
    /**
     * Constructor initializes the super class.
     */
    FileCounterWalkSequentialStream(File rootFile) {
        super(rootFile);
    }

    /**
     * This hook method returns the size in bytes of the root file, as
     * well as all the files in folders reachable from this file.
     */
    @Override
    protected Long compute() {
        try (var stream = Files
                // Return a stream of all entries rooted at mFile.
                .walk(mFile.toPath())) {

            return stream
                // Handle files (documents) and directories (folders).
                .mapToLong(entry -> {
                        if (Files.isDirectory(entry)) {
                            // Increment the count of folders.
                            mFolderCount.incrementAndGet();

                            // We don't count the size of a directory.
                            return 0;
                        } else {
                            // Increment the count of documents.
                            mDocumentCount.incrementAndGet();

                            // Return the number of bytes in the file.
                            return entry.toFile().length();
                        }
                    })

                // Sum all the file sizes in all the folders.
                .sum();
        } catch (IOException e) {
            // Return 0 if an exception occurs (shouldn't happen).
            return 0L;
        }
    }
}

