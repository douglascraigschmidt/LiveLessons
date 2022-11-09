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
     * The {@link List} of {@link CompletableFuture} objects to
     * subfolders contained in this {@link Folder}.
     */
    final List<CompletableFuture<Dirent>> mSubFolderFutures;

    /**
     * The {@link List} of {@link CompletableFuture} objects to
     * documents contained in this {@link Folder}.
     */
    final List<CompletableFuture<Dirent>> mDocumentFutures;

    /**
     * The {@link List} of subfolders contained in this {@link
     * Folder}, which are initialized only after all the {@link
     * CompletableFuture} objects in {@code mSubFolderFutures}
     * complete.
     */
    private List<Dirent> mSubFolders;

    /**
     * The {@link List} of documents contained in this {@link Folder},
     * which are initialized only after all the {@link
     * CompletableFuture} objects in {@code mDocumentFutures}
     * complete.
     */
    private List<Dirent> mDocuments;

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
     * @return The {@link List} of subfolders in this {@link Folder}
     */
    @Override
    public List<Dirent> getSubFolders() {
        return mSubFolders;
    }
    
    /**
     * @return The {@link List} of documents in this {@link Folder}
     */
    @Override
    public List<Dirent> getDocuments() {
        return mDocuments;
    }

    /**
     * @return A {@link spliterator} for this {@link Folder}
     */
    public Spliterator<Dirent> spliterator() {
        // Create a spliterator that uses breadth-first search to
        // traverse the recursive folder structure.
        return new BFSFolderSpliterator(this);
    }

    /**
     * @return A sequential stream containing all the {@link Dirent}
     *         elements rooted in this {@link Folder}
     */
    @Override
    public Stream<Dirent> stream() {
        return StreamSupport.stream(spliterator(),
                                    false);
    }

    /*
     * The following factory methods are used by clients of {@link
     * Folder}.
     */

    /**
     * Factory method that asynchronously creates a {@link Folder}
     * from the given {@link File}.
     *
     * @param file The {@link File} associated with the {@link Folder}
     *             in the file system
     *
     * @return A {@link CompletableFuture} that emits the {@link
     *         Folder} when it completes
     */
    public static CompletableFuture<Dirent> fromDirectory(File file) {
        // Return a CompletableFuture to a Folder.
        return fromDirectory(file.toPath());
    }

    /**
     * Factory method that asynchronously creates a {@link Folder}
     * from the given {@link Path}.
     *
     * @param rootPath The {@link Path} of this {@link Folder} in the
     *                 file system
     *
     * @return A {@link CompletableFuture} to that emits the {@link
     *         Folder} when it completes
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

        // Return a CompletableFuture emits the Folder when it
        // completes.
        return CompletableFuture
            // This Supplier lambda runs in the common fork-join pool.
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
     * The methods below are used by {@link FolderCollector}.
     */

    /**
     * Add a new {@code Path} to the appropriate {@link List} of
     * {@link CompletableFuture} objects.
     */
    void addEntry(Path entry) {
        // Add entry to the appropriate List of CompletableFuture
        // objects.
        if (Files.isDirectory(entry)) {
            var subFolderF = Folder
                // Asynchronously (and recursively) create a
                // CompletableFuture to a subfolder from the entry.
                .fromDirectory(entry)

                // This completion stage method is always called and
                // doesn't affect the CompletableFuture returned by
                // fromDirectory().
                .whenComplete((subFolder, ___) -> {
                    if (subFolder != null)
                        // Increase the size of this folder by the
                        // size of the new folder.
                        addToSize(subFolder.getSize());
                });

            // Add the CompletableFuture to the subfolder's List of
            // CompletableFuture objects.
            mSubFolderFutures.add(subFolderF);
        } else {
            var documentF = Document
                // Asynchronously create a document from the entry
                .fromPath(entry)

                // This completion stage method is always called and
                // doesn't affect the CompletableFuture returned by
                // fromDirectory().
                .whenComplete((document, ___) -> {
                    if (document != null)
                        // Count this new document.
                        addToSize(1);
                    });
            
            mDocumentFutures
                // Add the CompletableFuture to the document's List of
                // CompletableFuture objects.
                .add(documentF);
        }
    }

    /**
     * Merge contents of the {@code folder} param into contents of
     * this {@link Folder}.
     *
     * @param folder The {@link Folder} to merge from
     * @return A {@link Folder} containing the merged result
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
     * @return The updated folder
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
        // Return a List of completed Dirent objects.
        return listOfFutures
            // Convert the List into a Stream.
            .stream()

            // Convert the CompletableFuture to a Dirent (join() won't
            // block since all CompletableFuture objects have
            // completed by this point).
            .map(CompletableFuture::join)

            // Trigger intermediate processing and return a list.
            .toList();
    }
}
