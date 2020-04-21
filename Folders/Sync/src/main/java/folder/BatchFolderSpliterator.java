package folder;

import utils.Options;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * In conjunction with {@code StreamSupport.stream()} and {@code
 * Spliterators.spliterator()} this class creates a sequential or
 * parallel stream of {@code Dirents} from a recursively-structured
 * directory folder.
 */
public class BatchFolderSpliterator
       extends Spliterators.AbstractSpliterator<Dirent> {
    /**
     * Size of the batch to process, which doubles every time it's used.
     */
    private int mBatchSize;

    /**
     * Iterator traverses the folder contents one directory entry at a
     * time.
     */
    private final Iterator<Dirent> mIterator;
        
    /**
     * Constructor initializes the fields and super class.
     */
    public BatchFolderSpliterator(Folder folder) {
        super(folder.size(), NONNULL + IMMUTABLE);

        // Make the initial batch size match the number of processors.
        mBatchSize =
            (int) folder.size() / Runtime.getRuntime().availableProcessors();

        // Initialize the breadth-first search iterator.
        mIterator = new BFSIterator(folder);
    }

    /**
     * Attempt to advance the spliterator by one Dirent.
     */
    public boolean tryAdvance(Consumer<? super Dirent> action) {
        // If there's a dirent available.
        if (mIterator.hasNext()) {
            // Obtain and accept the current entry.
            action.accept(mIterator.next());
            // Keep going.
            return true;
        } else
            // Bail out.
            return false;
    }

    /**
     * If this spliterator can be partitioned, returns a spliterator
     * covering elements in the current folder, that will, upon return
     * from this method, not be covered by this spliterator.
     */
    public Spliterator<Dirent> trySplit() {
        // If there's a dirent available.
        if (mIterator.hasNext()) 
            // Split off and process the next batch of dirents in
            // parallel.
            return splitBatch();
        else
            // Bail out.
            return null;
    }

    /**
     * Split off and process the next batch of dirents in parallel.
     */
    private Spliterator<Dirent> splitBatch() {
        // This array holds the next batch of dirents to process.
        Object[] direntArray = new Object[mBatchSize];
        int index = 0;

        // Iterate thru mBatchSize dirents and add them to the array.
        while (index < mBatchSize && mIterator.hasNext())
            direntArray[index++] = mIterator.next();

        // Double the batch size each time it's used.
        mBatchSize += mBatchSize;

        // Return a spliterator that will process this batch.
        return Spliterators.spliterator(direntArray, 0, index, 0);
    }

    /**
     * This iterator traverses each element in the folder using
     * (reverse) breadth-first search.
     */
    private static class BFSIterator
        implements Iterator<Dirent> {
        /**
         * The current entry to process.
         */
        private Dirent mCurrentEntry;

        /**
         * The list of (sub)folders to process.
         */
        private final List<Dirent> mFoldersList;

        /**
         * The list of documents to process.
         */
        private final List<Dirent> mDocsList;

        /**
         * Constructor initializes the fields.
         */
        BFSIterator(Folder rootFolder) {
            // Make the rootFolder the current entry. 
            mCurrentEntry = rootFolder;

            // Add the subfolders (if any) in the rootFolder.
            mFoldersList = new ArrayList<>(rootFolder.getSubFolders());

            // Add the documents (if any) in the rootFolder.
            mDocsList = new ArrayList<>(rootFolder.getDocuments());
        }

        /**
         * @return True if the iterator can continue, false if it's at
         * the end
         */
        public boolean hasNext() {
            // See if we need to refresh the current entry.
            if (mCurrentEntry == null) {
                // See if there are any subfolders left to process.
                if (mFoldersList.size() > 0) {
                    // If there are subfolders left then pop the one
                    // at the end and make it the current entry.
                    mCurrentEntry =
                        mFoldersList.remove(mFoldersList.size() - 1);

                    // Add any/all subfolders from the new current
                    // entry to the end of the subfolders list.
                    mFoldersList.addAll(mCurrentEntry.getSubFolders());

                    // Add any/all documents from the new current
                    // entry to the end of the documents list.
                    mDocsList.addAll(mCurrentEntry.getDocuments());
                }
                // See if there are any documents left to process.
                else if (mDocsList.size() > 0) 
                    // Pop the document at the end of the list off and
                    // make it the current entry.
                    mCurrentEntry =
                        mDocsList.remove(mDocsList.size() - 1);
            }

            // Return false if there are no more entries, else true.
            return mCurrentEntry != null;
        }

        /**
         * @return The next unseen entry in the folder
         */
        public Dirent next() {
            // Store the current entry.
            Dirent nextDirent = mCurrentEntry;

            // Reset current entry to null.
            mCurrentEntry = null;
            
            // Return the current entry.
            return nextDirent;
        }
    }

    /**
     * Only prints {@code string} when the verbose option is enabled.
     */
    void debug(String string) {
        if (Options.getInstance().getVerbose())
            System.out.println(string);
    }
}
