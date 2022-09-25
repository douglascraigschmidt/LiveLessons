import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.FileVisitResult.CONTINUE;

/**
 * This task uses the Java Files.walkFileTree() method and Java 7 features
 * to count the files and compute the size in bytes of all files in
 * folders reachable from the given root file.
 */
public class FileCounterWalkFileTree
       extends AbstractFileCounter {
    /**
     * Constructor initializes the super class.
     */
    FileCounterWalkFileTree(File rootFile) {
        super(rootFile);
    }

    /**
     * This hook method returns the size in bytes of the root file, as
     * well as all the files in folders reachable from this file.
     */
    @Override
    protected Long compute() {
        // Use an AtomicLong since anonymous classes can't (easily)
        // have side-effects.
        AtomicLong totalSize = new AtomicLong(0);
        try {
            Files
                // Walk the entire directory tree and visit each entry
                // using the Visitor pattern.
                .walkFileTree(mFile.toPath(),
                              // Create a new file visitor.
                              makeVisitor(totalSize));
        } catch (IOException e) {
            // Swallow exception for simplicity.
        }

        // Return the total size of all the bytes reachable from the
        // root file.
        return totalSize.get();
    }

    /**
     * A factory method returning a {@link SimpleFileVisitor} that
     * traverses the file system.
     *
     * @param totalSize An {@link AtomicLong} that keeps track of the total size
     *                 since anonymous classes can't (easily) have side-effects
     * @return A .
     */
    private SimpleFileVisitor<Path> makeVisitor(AtomicLong totalSize) {
        return new SimpleFileVisitor<Path>() {
            @Override
            /*
             * Visit a directory (folder) and increment its count.
             */
            public FileVisitResult preVisitDirectory(Path dir,
                                                     BasicFileAttributes attrs) {
                // Increment the count of folders.
                mFolderCount.incrementAndGet();

                // Keep going.
                return CONTINUE;
            }

            @Override
            /*
             * Visit a file (document), increment its count, and
             * increment the total length.
             */
            public FileVisitResult visitFile(Path file,
                                             BasicFileAttributes attrs) {
                // Increment the count of documents.
                mDocumentCount.incrementAndGet();

                // Increment the total size of all the bytes.
                totalSize.addAndGet(file.toFile().length());

                // Keep going.
                return CONTINUE;
            }
        };
    }
}

