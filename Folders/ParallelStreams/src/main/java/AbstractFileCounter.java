import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Abstract super class for the various {@code FileCounter*}
 * subclasses.
 */
public abstract class AbstractFileCounter {
    /**
     * The current file that's being analyzed.
     */
    protected final File mFile;

    /**
     * If true use a parallel stream, else use a sequential stream.
     */
    protected static boolean sParallel;

    /**
     * Keeps track of the total number of documents encountered.
     */
    protected static final AtomicLong sDocumentCount =
        new AtomicLong(0);

    /**
     * Keeps track of the total number of folders encountered.
     */
    protected static final AtomicLong sFolderCount =
        new AtomicLong(0);

    /**
     * Constructor initializes the fields.
     */
    AbstractFileCounter(File file) {
        mFile = file;
    }

    /**
     * @return The size in bytes of the file, as well as all
     *         the files in folders reachable from this file
     */
    protected abstract long compute();

    /**
     * @return The number of documents counted during the recursive
     *         traversal
     */
    public static long documentCount() {
        return sDocumentCount.get();
    }

    /**
     * @return The number of folders counted during the recursive
     *         traversal
     */
    public static long folderCount() {
        return sFolderCount.get();
    }

    /**
     * Processes a document.
     *
     * @param document The document to process
     * @return The length of the document in bytes
     */
    protected long handleDocument(File document) {
        // Increment the count of documents.
        sDocumentCount.incrementAndGet();

        // Return the length of the document.
        return document.length();
    }

    /**
     * Process a folder.
     *
     * @param folder The folder to process
     * @param function A factory that returns an object used to
     *                recursively count the number of files in a
     *                (sub)folder
     * @return A count of the number of bytes in files in a
     *         (sub)folder
     */
    protected long handleFolder
        (File folder,
         Function<File, AbstractFileCounter> function) {
        // Increment the count of folders.
        sFolderCount.incrementAndGet();

        return function
            // Call the factory to create a subclass of
            // AbstractFileCount.
            .apply(folder)

            // Recursively count the number of bytes in files in
            // a (sub)folder.
            .compute();
    };
}

