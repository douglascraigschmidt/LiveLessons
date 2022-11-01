package folder;

import utils.ExceptionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Represents the contents of a folder, which can include recursive
 * (sub)folders and/or documents.
 */
public class Folder 
       extends Dirent {
    /**
     * The list of futures to subfolders contained in this folder.
     */
    List<CompletableFuture<Dirent>> mSubFolderFutures;

    /**
     * The list of futures to documents contained in this folder.
     */
    List<CompletableFuture<Dirent>> mDocumentFutures;

    /**
     * The list of subfolders contained in this folder, which are
     * initialized only after all the futures in mSubFolderFutures
     * have completed.
     */
    List<Dirent> mSubFolders;

    /**
     * The list of documents contained in this folder, which are
     * initialized only after all the futures in mDocumentFutures have
     * completed.
     */
    List<Dirent> mDocuments;

    /**
     * Constructor initializes the fields.
     */
    Folder(Path path) {
        super(path, 1);

        // Initialize all the fields.
        mSubFolderFutures = new ArrayList<>();
        mDocumentFutures = new ArrayList<>();
        mSubFolders = new ArrayList<>();
        mDocuments = new ArrayList<>();
    }
    
    /**
     * @return The list of subfolders in this folder
     */
    @Override
    public List<Dirent> getSubFolders() {
        return mSubFolders;
    }
    
    /**
     * @return The list of documents in this folder
     */
    @Override
    public List<Dirent> getDocuments() {
        return mDocuments;
    }

    /**
     * @return A spliterator for this class
     */
    public Spliterator<Dirent> spliterator() {
        // Create a spliterator that uses breadth-first search
        // to traverse the recursive folder structure.
        return new BFSFolderSpliterator(this);
    }

    /**
     * @return A sequential stream containing all elements rooted at
     * this directory entry
     */
    @Override
    public Stream<Dirent> stream() {
        return StreamSupport.stream(spliterator(),
                                    false);
    }

    /*
     * The following factory methods are used by clients of this
     * class.
     */

    /**
     * Factory method that asynchronously creates a folder from the
     * given {@code file}.
     *
     * @param file The file associated with the folder in the file system
     *
     * @return A future to the document that will be complete when the
     *         contents of the folder are available
     */
    public static CompletableFuture<Dirent> fromDirectory(File file) {
        // Return a future to a document.
        return fromDirectory(file.toPath());
    }

    /**
     * Factory method that asynchronously creates a folder from the
     * given {@code file}.
     *
     * @param rootPath The path of the folder in the file system
     *
     * @return A future to the document that will be complete when the
     *         contents of the folder are available
     */
    public static CompletableFuture<Dirent> fromDirectory(Path rootPath) {
        // This function creates a stream containing all contents at
        // the given rootPath.
        Function<Path, Stream<Path>> getStream = ExceptionUtils
            // An adapter that simplifies checked exceptions.
            .rethrowFunction(path -> Files
                             // Stream all contents in this path.
                             .walk(path,
                                   // Limit to just this folder.
                                   1));

        // Return a future that completes after folder is available.
        return CompletableFuture
            // This supplier lambda runs in the common fork-join pool.
            .supplyAsync(() -> getStream
                         // Create a stream containing all the
                         // contents at the given rootPath.
                         .apply(rootPath)

                         // Avoid infinite recursion.
                         .filter(path -> !path.equals(rootPath))

                         // Terminate stream and create a folder with
                         // all entries in this (sub)folder.
                         .collect(FolderCollector.toFolder(rootPath)))
            // This completion stage method "de-nests" the
            // future-to-future returned by supplyAsync()!
            .thenCompose(folderFuture -> folderFuture
                         // Function.identity() and thenApply()/
                         // thenCompose() de-nests the folder.
                         .thenApply(Function.identity()));
    }

    /*
     * The methods below are used by FolderCollector.
     */

    /**
     * Add a new {@code entry} to the appropriate list of futures.
     */
    void addEntry(Path entry) {
        // Add entry to the appropriate list of futures.
        if (Files.isDirectory(entry)) {
            var subFolderF = Folder
                // Asynchronously (and recursively) create a future to a
                // folder from the entry.
                .fromDirectory(entry)

                // This completion stage method is always called and
                // doesn't affect the future returned by fromDirectory().
                .whenComplete((subFolder, ___) -> {
                    if (subFolder != null)
                        // Increase the size of this folder
                        // by the size of the new folder.
                        addToSize(subFolder.getSize());
                });

            // Add the future to the folder futures list.
            mSubFolderFutures.add(subFolderF);
        } else {
            var documentF = Document
                // Asynchronously create a document from the entry
                .fromPath(entry)

                // This completion stage method is always called and
                // doesn't affect the future returned by fromDirectory().
                .whenComplete((document, ___) -> {
                    if (document != null)
                        // Count this new document.
                        addToSize(1);
                    });
            
            mDocumentFutures
                // Add the future to the document futures list.
                .add(documentF);
        }
    }

    /**
     * Merge contents of {@code folder} into contents of this folder.
     *
     * @param folder The folder to merge from
     * @return The merged result
     */
    Folder merge(Folder folder) {
        // Update the lists.
        mSubFolderFutures.addAll(folder.mSubFolderFutures);
        mDocumentFutures.addAll(folder.mDocumentFutures);

        // Update the size.
        addToSize(folder.getSize());

        // Return this object.
        return this;
    }

    /**
     * This method is called after all the subfolders and documents
     * have finished processing asynchronously to update the lists of
     * subfolders and documents.
     *
     * @return The updated folder.
     */
    Folder whenComplete(Void v) {
        // Initialize all the completed subfolders.
        mSubFolders = collectToList(mSubFolderFutures);

        // Initialize all the completed documents.
        mDocuments = collectToList(mDocumentFutures);

        // Return this folder.
        return this;
    }

    /**
     * Converts a list of completable futures to dirents into a list
     * of dirents by joining them, which won't block.
     *
     * @param listOfFutures The list of completable futures to dirents
     *                      to convert
     * @return A list of dirents
     */
    private List<Dirent> collectToList
            (List<CompletableFuture<Dirent>> listOfFutures) {
        // Return a list of completed dirents.
        return listOfFutures
            // Convert the list into a stream.
            .stream()

            // Convert the future to a directory entry (join() won't
            // block since all the futures have completed by this
            // point).
            .map(CompletableFuture::join)

            // Trigger intermediate processing and return
            // a list.
            .collect(toList());
    }
}
