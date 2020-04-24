package folder;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This super class defines the common capabilities provided by a
 * directory entry and is inherited by the Folder and Document
 * subclasses.
 */
public abstract class Dirent
       implements Iterable<Dirent> {
    /**
     * Path of the document.
     */
    private File mPath;

    /**
     * The total number of entries in this recursively structured
     * folder.
     */
    private long mSize;

    /**
     * Default constructor.
     */
    public Dirent() {
    }

    /**
     * Constructor initializes the fields.
     */
    public Dirent(File path, long size) {
        mPath = path;
        mSize = size;
    }

    /**
     * @return Name of the dirent
     */
    public String getName() {
        return mPath.getName();
    }

    /**
     * Set path of the dirent.
     */
    public void setPath(File path) {
        mPath = path;
    }

    /**
     * @return Path of the dirent
     */
    public File getPath() {
        return mPath;
    }

    /**
     * @return The list of subfolders in this folder
     */
    public List<Dirent> getSubFolders() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return The list of documents in this folder
     */
    public List<Dirent> getDocuments() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return The contents of this Dirent
     */
    public CharSequence getContents() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return The total number of entries in this recursively
     * structured dirent.
     */
    public long getSize() {
        return mSize;
    }

    /**
     * Set the total number of entries in this recursively
     * structured dirent.
     */
    public void setSize(long size) {
        mSize = size;
    }

    /**
     * Add {@code size} to the current size.
     */
    public void addToSize(long size) {
        mSize += size;
    }

    /**
     * Factory method that returns an iterator.
     */
    public Iterator<Dirent> iterator() {
        return new BFSIterator(this);
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
        BFSIterator(Dirent rootFolder) {
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
                else if (mDocsList.size() > 0) {
                    // Pop the document at the end of the list off and
                    // make it the current entry.
                    mCurrentEntry =
                            mDocsList.remove(mDocsList.size() - 1);
                }
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
}
