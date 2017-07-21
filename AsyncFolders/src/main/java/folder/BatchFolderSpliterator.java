package folder;

import utils.Options;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * This class is used in conjunction with StreamSupport.stream() and
 * the Java Regex class to create a sequential or parallel stream of
 * Dirents from a recursively structured directory folder.
 */
public class BatchFolderSpliterator
       extends Spliterators.AbstractSpliterator<Dirent> {
    /**
     * The current entry that's being processed.
     */
    private Dirent mCurrentEntry;

    /**
     * The current folder that's being processed.
     */
    private Folder mCurrentFolder;

    /**
     * Size of the batch to process.
     */
    private final int mBatchSize;

    /**
     * Stack
     */
    private final Stack<Folder> mStack;

    /**
     * Only prints @a string when the verbose option is enabled.
     */
    void debug(String string) {
        if (Options.getInstance().getVerbose())
            System.out.println(string);
    }

    /**
     * Constructor initializes the fields and super class.
     */
    public BatchFolderSpliterator(Folder folder) {
        super(folder.size(), NONNULL + IMMUTABLE);
        //         Options.getInstance().setVerbose(true);

        mBatchSize = (int) folder.size() / Runtime.getRuntime().availableProcessors();

        mStack = new Stack<>();
        mCurrentFolder = new Folder(folder);
    }

    /**
     * Attempt to advance the spliterator by one Dirent.
     */
    public boolean tryAdvance(Consumer<? super Dirent> action) {
        // If there's no current entry try to get the entry one.
        if (mCurrentEntry == null)
            mCurrentEntry = getNextEntry();

        // If there's still no current entry then we're done.
        if (mCurrentEntry == null) 
            // Inform the caller to bail out.
            return false;
        else {
            // debug("accepting " + mCurrentEntry.getName());
            // Accept the current entry.
            action.accept(mCurrentEntry);
        }

        // Reset for the next iteration through the spliterator.
        mCurrentEntry = null;

        // Inform the caller that we still want to continue.
        return true;
    }

    /**
     * @return The next unseen entry in the folder.
     */
    private Dirent getNextEntry() {
        // Handle the case where there's no current folder.
        if (mCurrentFolder == null) 
            // If there are no folders on the stack then we're done.
            if (mStack.empty())
                return null;
            else
                // Pop the next folder off the stack and process it.
                mCurrentFolder = mStack.pop();

        // If the current folder has no more subfolders..
        if (mCurrentFolder.getSubFolders().isEmpty()) {
            // and current folder has no more documents..
            if (mCurrentFolder.getDocuments().isEmpty()) {
                // Reset current folder and call getNextEntry()
                // recursively to see if anything exists up the stack.
                mCurrentFolder = null;
                return getNextEntry();
            } else 
                // Remove and return the next document to process
                // in this folder.
                return mCurrentFolder
                    .getDocuments()
                    .remove(mCurrentFolder.getDocuments().size() - 1);
        } else 
            // Pop the last folder off the end of the subfolders list
            // and push it on the stack so it will be processed next.
            return mStack.push(new Folder(mCurrentFolder
                                          .getSubFolders()
                                          .remove(mCurrentFolder
                                                  .getSubFolders()
                                                  .size() - 1)));
    }

    /**
     * If this spliterator can be partitioned, returns a Spliterator
     * covering elements in the current folder, that will, upon return
     * from this method, not be covered by this Spliterator.
     */
    public Spliterator<Dirent> trySplit() {
        if (mCurrentFolder != null)
            mCurrentEntry = mCurrentFolder;

        // Get the next entry to process.
        if (mCurrentEntry == null)
            mCurrentEntry = getNextEntry();

        if (mCurrentEntry == null)
            return null;
        else {
            Object[] a = new Object[mBatchSize];

            int i;

            for (i = 0; i < mBatchSize; i++)
                if ((a[i] = getNextEntry()) == null)
                    break;

            mCurrentEntry = null;
            return Spliterators.spliterator(a, 0, i, 0); // Spliterator.ORDERED);
        }
    }
}
