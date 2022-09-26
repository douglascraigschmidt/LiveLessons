import utils.TriFunction;

import java.io.File;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

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
     * Keeps track of the total number of documents encountered.
     */
    protected final AtomicLong mDocumentCount;

    /**
     * Keeps track of the total number of folders encountered.
     */
    protected final AtomicLong mFolderCount;

    /**
     * Constructor initializes the fields.
     */
    AbstractFileCounter(File file) {
        mFile = file;
        mDocumentCount = new AtomicLong(0);
        mFolderCount = new AtomicLong(0);
    }

    /**
     * Constructor initializes the fields.
     */
    AbstractFileCounter(File file,
                        AtomicLong documentCount,
                        AtomicLong folderCount) {
        mFile = file;
        mDocumentCount = documentCount;
        mFolderCount = folderCount;
    }

    /**
     * @return The size in bytes of the file, as well as all
     *         the files in folders reachable from this file
     */
    protected abstract long compute();

    /**
     * @return The number of documents counted during the recursive
     * traversal
     */
    public long documentCount() {
        return mDocumentCount.get();
    }

    /**
     * @return The number of folders counted during the recursive
     * traversal
     */
    public long folderCount() {
        return mFolderCount.get();
    }

    /**
     * Processes a document.
     *
     * @param document The document to process
     * @return The length of the document in bytes
     */
    protected long handleDocument(File document) {
        // Increment the count of documents.
        mDocumentCount.incrementAndGet();

        // Return the length of the document.
        return document.length();
    }

    /**
     * Process a folder.
     *
     * @param folder The folder to process
     * @param function A {@link TriFunction} factory that
     *                recursively counts the number of
     *                files in a (sub)folder
     * @return A count of the number of files in a (sub)folder
     */
    protected long handleFolder(File folder,
                                AtomicLong documentCount,
                                AtomicLong folderCount,
                                TriFunction<File, AtomicLong, AtomicLong, AbstractFileCounter> function) {
        // Increment the count of folders.
        mFolderCount.incrementAndGet();

        // Call the factory to recursively count the number of files
        // in a (sub)folder.
        return function
            .apply(folder, documentCount, folderCount)
            .compute();
    };
}

