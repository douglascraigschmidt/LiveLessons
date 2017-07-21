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
public class StackFolderSpliterator
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
     * A stack used keep track of subfolders that need to be
     * processed.
     */
    private Stack<Folder> mStack;

    /**
     * The level of the recursion for nested calls to
     * StackFolderSpliterator (useful for debugging).
     */
    private int mLevel;

    /**
     * A helper class that's used to keep track of intermediate
     * results when splitting a folder's subfolders.
     */
    static class FauxFolder extends Folder {
        /**
         * Constructor initializes the fields from the parameters.
         */
        FauxFolder(Path path,
                   List<Folder> subFolders,
                   List<Document> documents) {
            super(path, subFolders, documents);
        }
    }

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
    public StackFolderSpliterator(Folder folder) {
        super(folder.size(), NONNULL + IMMUTABLE);
        //         Options.getInstance().setVerbose(true);

        mLevel = 0;
        mStack = new Stack<>();
        mCurrentFolder = new Folder(folder);
    }

    /**
     * Internal constructor that's used during splits.
     */
    private StackFolderSpliterator(Folder dummyFolder,
                              int level) {
        super(dummyFolder.size(), NONNULL + IMMUTABLE);

        mLevel = level;
        mStack = new Stack<>();
        mCurrentFolder = dummyFolder;
    }

    /**
     * Attempt to advance the spliterator by one Dirent.
     */
    public boolean tryAdvance(Consumer<? super Dirent> action) {
        // If there's no current entry try to get the entry one.
        if (mCurrentEntry == null)
            mCurrentEntry = getNextEntry(true);

        // If there's still no current entry then we're done.
        if (mCurrentEntry == null) 
            // Inform the caller to bail out.
            return false;
        else {
            // Ignore FauxFolders, which only exist to maximize
            // parallelism.
            if (!(mCurrentEntry instanceof FauxFolder)) {
                // debug("accepting " + mCurrentEntry.getName());
                // Accept the current entry.
                action.accept(mCurrentEntry);
            }

            // Reset for the next iteration through the spliterator.
            mCurrentEntry = null;

            // Inform the caller that we still want to continue.
            return true;
        }
    }

    /**
     * @return The next unseen entry in the folder.
     */
    private Dirent getNextEntry(boolean ignoreDummy) {
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
                return getNextEntry(ignoreDummy);
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
        // Get the next entry to process.
        if (mCurrentFolder != null)
            mCurrentEntry = mCurrentFolder;
        else if (mCurrentEntry == null)
            mCurrentEntry = getNextEntry(false);

        // If the current entry is a folder..
        if (mCurrentEntry instanceof Folder) 
            // .. then try to split it recursively.
            return tryToSplitFolder((Folder) mCurrentEntry);
        // If the current entry is just a document..
        else if (mCurrentEntry instanceof Document)
            // .. then we're done with the splitting.
            return null;
        else
            // Something weird has happened..
            throw new UnsupportedOperationException();
    }

    /**
     * Try to split the contents of @a folder.
     *
     * @return A spliterator that's null if there's only one entry in
     * the folder, else one that contains the "left hand" dirents of
     * the split.
     */
    private Spliterator<Dirent> tryToSplitFolder(Folder folder) {
        // Get all the subfolders in the folder (if any).
        List<Folder> subFolderList = folder.getSubFolders();

        // Get all the documents in the folder (if any);
        List<Document> docsList = folder.getDocuments();

        // If there's just one subfolder (or less) then we're done
        // with the splitting.
        if (subFolderList.size() < 1
            && docsList.size() < 1) 
            return null;
        else {
            // Try to split documents evenly.
            int splitDocumentsPos = docsList.size() / 2;

            List<Document> leftHandDocs;
            List<Document> rightHandDocs;

            // If there's more than one document in the list then
            // split it evenly.
            if (splitDocumentsPos > 1) {
                // Create a sublist containing the left hand
                // documents.
                leftHandDocs =
                    docsList.subList(0,
                                     splitDocumentsPos);

                // Create a sublist containing the right hand
                // documents.
                rightHandDocs =
                    docsList.subList(splitDocumentsPos,
                                     docsList.size());
            } else {
                // Create a no-op placeholder for the left hand docs.
                leftHandDocs = new ArrayList<>();
                
                // The right hand contains the single entry.
                rightHandDocs = docsList;
            }

            // Try to split subfolders evenly.
            int splitSubFoldersPos = subFolderList.size() / 2;

            // If there are 2 or more subfolders then split them in
            // half and process them in parallel.
            if (splitSubFoldersPos >= 1)
                return splitFolder(folder,
                                   splitSubFoldersPos,
                                   leftHandDocs,
                                   rightHandDocs);

            // If there's just a single subfolder then process it
            // separately.
            else if (subFolderList.size() == 1) {
                Folder subFolder = subFolderList.get(0);
                mCurrentEntry = mCurrentFolder =
                    new FauxFolder(Paths.get("onlyChildDummy"
                                             + mLevel
                                             + subFolder.getName()),
                                   subFolderList,
                                   docsList);
                return null;
            } 
            // There's nothing else to do for this folder, so return
            // and let tryAdvance() work its magic.
            else 
                return null;
        }
    }

    /**
     * Split the contents of @a folder.
     *
     * @return A spliterator that contains the "left hand" dirents of
     * the split.
     */
    private Spliterator<Dirent> splitFolder(Folder folder,
                                            int splitSubFoldersPos,
                                            List<Document> leftHandDocs,
                                            List<Document> rightHandDocs) {
        // Create a sublist containing the left hand subfolders.
        List<Folder> leftHandSubFolders =
            folder.getSubFolders().subList(0,
                                           splitSubFoldersPos);

        // Create a sublist containing the right hand subfolders.
        List<Folder> rightHandSubFolders = 
            folder.getSubFolders().subList(splitSubFoldersPos,
                                           folder.getSubFolders().size());

        // Create a new FauxFolder containing the left hand subfolders.
        Folder leftHandFolder =
            new FauxFolder(Paths.get("leftDummy"
                                     + mLevel
                                     + leftHandSubFolders
                                     .stream()
                                     .map(f -> f.getName())
                                     .collect(joining())),
                           leftHandSubFolders,
                           leftHandDocs);

        // Create a new FauxFolder containing the right hand subfolders.
        Folder rightHandFolder =
            new FauxFolder(Paths.get("rightDummy"
                                     + mLevel
                                     + rightHandSubFolders
                                     .stream()
                                     .map(f -> f.getName())
                                     .collect(joining())),
                           rightHandSubFolders,
                           rightHandDocs);

        // Update the fields with the current folder to process.
        mCurrentEntry = mCurrentFolder = rightHandFolder; 

        // Create and return a new StackFolderSpliterator containing the
        // left hand dirents.
        return new StackFolderSpliterator(leftHandFolder,
                                     mLevel + 1);
    }
}
