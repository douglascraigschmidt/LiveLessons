package folder;

import utils.ArrayUtils;
import utils.ExceptionUtils;

import java.io.File;
import java.io.IOException;
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
    private List<CompletableFuture<Dirent>> mSubFolderFutures;

    /**
     * The list of futures to documents contained in this folder.
     */
    private List<CompletableFuture<Dirent>> mDocumentFutures;

    /**
     * The list of subfolders contained in this folder, which are
     * initialized only after all the futures in mSubFolderFutures
     * have completed.
     */
    private List<Dirent> mSubFolders;

    /**
     * The list of documents contained in this folder, which are
     * initialized only after all the futures in mDocumentFutures have
     * completed.
     */
    private List<Dirent> mDocuments;

    /**
     * The total number of entries in this recursively structured
     * folder.
     */
    private long mSize;

    /**
     * Constructor initializes the fields.
     */
    Folder() {
        mSubFolderFutures = new ArrayList<>();
        mDocumentFutures = new ArrayList<>();
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
     * @return The total number of entries in this recursively
     * structured folder.
     */
    public long size() {
        return mSize;
    }

    /**
     * @return A spliterator for this class
     */
    public Spliterator<Dirent> spliterator() {
        return new RecursiveFolderSpliterator(this);
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

    /**
     * @return A parallel stream containing all elements rooted at
     * this directory entry
     */
    @Override
    public Stream<Dirent> parallelStream() {
        return StreamSupport.stream(spliterator(),
                                    true);
    }

    /*
     * The following factory methods are used by clients of this
     * class.
     */

    /**
     * Factory method that asynchronously creates a folder from the
     * given @a file.
     *
     * @param file The file associated with the folder in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return A future to the document that will be complete when the
     *         contents of the folder are available
     */
    public static CompletableFuture<Dirent>
        fromDirectory(File file,
                      boolean parallel) throws IOException {
        return fromDirectory(file.toPath(),
                             parallel);
    }

    /**
     * Factory method that asynchronously creates a folder from the
     * given @a file.
     *
     * @param rootPath The path of the folder in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return A future to the document that will be complete when the
     *         contents of the folder are available
     */
    public static CompletableFuture<Dirent>
        fromDirectory(Path rootPath,
                      boolean parallel) throws IOException {
        // Return a future that completes once the folder's contents
        // are available.
        return CompletableFuture.supplyAsync(() -> {
                Function<Path, Stream<Path>> getStream = ExceptionUtils
                    .rethrowFunction(path
                                     // List all subfolders and
                                     // documents in just this folder.
                                     -> Files.walk(path, 1));

                // Create a stream containing all the contents at the
                // given rootPath.
                Stream<Path> pathStream = getStream.apply(rootPath);

                // Convert the stream to parallel if directed.
                if (parallel)
                    //noinspection ResultOfMethodCallIgnored
                    pathStream.parallel();

                // Create a future to the folder containing all the
                // contents at the given rootPath.
                return pathStream
                    // Eliminate rootPath to avoid infinite recursion.
                    .filter(path -> !path.equals(rootPath))

                    // Terminate the stream and create a Folder
                    // containing all entries in this folder.
                    .collect(FolderCollector.toFolder(parallel));
            })
            .thenCompose(folderFuture -> {
                    // Run the following code after the folder's
                    // contents are available.
                    return folderFuture
                        .thenApply(folder
                                   -> {
                                       // Set the path of the folder.
                                       folder.setPath(rootPath);
                                       ((Folder) folder).computeSize();

                                       // Return the folder, which is wrapped in
                                       // a future.
                                       return folder;
                                   });
                });
    }

    /**
     * Determine how many subfolders and documents are rooted at this
     * folder.
     */
    private void computeSize() {
        long folderCount = getSubFolders()
            // Convert list to a stream.
            .stream()

            // Get the size of each subfolder.
            .mapToLong(subFolder -> ((Folder) subFolder).mSize)

            // Sub up the sizes of the subfolders.
            .sum();

        // Count the number of documents in this folder.
        long docCount = (long) getDocuments().size();

        // Update the field with the correct count.
        mSize = folderCount 
            + docCount
            // Add 1 to count this folder.
            + 1;
    }

    /*
     * The methods below are used by the FolderCollector.
     */

    /**
     * Add a new @a entry to the appropriate list of futures.
     */
    void addEntry(Path entry,
                  boolean parallel) {
        // This adapter simplifies exception handling.
        Function<Path, CompletableFuture<Dirent>> getFolder = ExceptionUtils
            .rethrowFunction(file 
                             // Asynchronously create a folder from a
                             // directory file.
                             -> Folder.fromDirectory(file,
                                                     parallel));

        // This adapter simplifies exception handling.
        Function<Path, CompletableFuture<Dirent>> getDocument = ExceptionUtils
            .rethrowFunction(path
                             // Asynchronously create a document from
                             // a path.
                             -> Document.fromPath(path));

        // Add entry to the appropriate list of futures.
        if (Files.isDirectory(entry))
            mSubFolderFutures.add(getFolder.apply(entry));
        else
            mDocumentFutures.add(getDocument.apply(entry));
    }

    /**
     * Merge the contents of @a folder with the contents of this
     * folder.
     *
     * @param folder The folder to merge from
     * @return The merged result
     */
    Folder addAll(Folder folder) {
        mSubFolderFutures.addAll(folder.mSubFolderFutures);
        mDocumentFutures.addAll(folder.mDocumentFutures);
        return this;
    }

    /**
     * @return A future to a folder that will complete when all
     * entries in the folder complete
     */
    CompletableFuture<Folder> joinAll() {
        // Create an array containing all the futures for subfolders
        // and documents.
        CompletableFuture[] futures =
                ArrayUtils.concat(mSubFolderFutures,
                                  mDocumentFutures);

        // Create a future that will complete when all the other
        // futures have completed.
        CompletableFuture<Void> allDoneFuture =
            CompletableFuture.allOf(futures);
            
        // Return a future to this folder after first initializing its
        // subfolder/document fields after allDoneFuture completes.
        return allDoneFuture
            .thenApply(v -> {
                    // Initialize all the subfolders.
                    mSubFolders = mSubFolderFutures
                        // Convert the list into a stream.
                        .stream()

                        // Convert the future to a directory entry.
                        // Note that join() won't block since all the
                        // futures have completed by this point.
                        .map(subFolderFuture
                             -> subFolderFuture.join())

                        // Trigger intermediate processing and return
                        // a list.
                        .collect(toList());

                    // Initialize all the documents.
                    mDocuments = mDocumentFutures
                        // Convert the list into a stream.
                        .stream()


                        // Convert the future to a directory entry.
                        // Note that join() won't block since all the
                        // futures have completed by this point.
                        .map(documentFuture
                             -> documentFuture.join())

                        // Trigger intermediate processing and return
                        // a list.
                        .collect(toList());

                    // Initialize the size.
                    mSize = mSubFolders.size() + mDocuments.size();

                    // Return this folder, which is converted to a
                    // future to a folder.
                    return this;
                });
    }
}
