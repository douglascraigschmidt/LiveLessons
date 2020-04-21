package folder;

import utils.ExceptionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
     * The total number of entries in this recursively structured
     * folder.
     */
    long mSize;

    /**
     * Constructor initializes the fields.
     */
    Folder() {
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
        // Return a future that completes once the folder's contents
        // are available.
        return CompletableFuture
            // Arrange to run this supplier lambda in the common
            // fork-join pool.
            .supplyAsync(() -> {
                    // This function creates a stream containing all
                    // the contents at the given rootPath.
                    Function<Path, Stream<Path>> getStream = ExceptionUtils
                        // An adapter that simplifies checked exceptions.
                        .rethrowFunction(path -> Files
                                         // List all subfolders and
                                         // documents in this path.
                                         .walk(path,
                                               // Limit to just
                                               // this folder.
                                               1));

                    // Create a stream containing all the contents at
                    // the given rootPath.
                    Stream<Path> pathStream = getStream.apply(rootPath);

                    // Create a future to the folder containing all
                    // the contents at the given rootPath.
                    return pathStream
                        // Eliminate rootPath to avoid infinite
                        // recursion.
                        .filter(path -> !path.equals(rootPath))

                        // Terminate stream and create a Folder containing
                        // all entries in this (sub)folder.
                        .collect(FolderCollector.toFolder());
                })
            // This completion stage method is needed since supplyAsync() returns
            // a future to a future!
            .thenCompose(folderFuture ->
                    // Run the following code after the folder's
                    // contents are available.
                    folderFuture
                        .thenApply(folder -> {
                                       // Set the path of the folder.
                                       folder.setPath(rootPath);
                                       folder.computeSize();

                                       // Return the folder, which is
                                       // wrapped in a future.
                                       return folder;
                                   }));
    }

    /**
     * Determine how many subfolders and documents are rooted at this
     * folder.
     */
    private void computeSize() {
        // Count the number of subfolders in this folder.
        long folderCount = getSubFolders()
            // Convert list to a stream.
            .stream()

            // Get the size of each subfolder.
            .mapToLong(subFolder -> ((Folder) subFolder).mSize)

            // Sub up the sizes of the subfolders.
            .sum();

        // Count the number of documents in this folder.
        long docCount = getDocuments().size();

        // Update the field with the correct count.
        mSize = folderCount + docCount
            // Add 1 to count this folder.
            + 1;
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
            // Asynchronously create a folder from the entry and add a
            // future to the folder futures list.
            mSubFolderFutures.add(Folder.fromDirectory(entry));
        } else {
            // Asynchronously create a document from the entry and add
            // a future to the document futures list.
            mDocumentFutures.add(Document.fromPath(entry));
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

        // Initialize the size.
        mSize = mSubFolderFutures.size() + mDocumentFutures.size();

        // Return this object.
        return this;
    }
}
