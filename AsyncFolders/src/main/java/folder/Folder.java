package folder;

import utils.ArrayUtils;
import utils.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Represents the contents of a folder, which can be subfolders or
 * documents.
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
     * The list of subfolders contained in this folder.
     */
    List<Folder> mSubFolders;

    /**
     * The list of documents contained in this folder.
     */
    List<Document> mDocuments;

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
    }

    /**
     * Constructor initializes the fields from the parameters.
     */
    Folder(Path path,
           List<Folder> subFolders,
           List<Document> documents) {
        super(path);
        mSubFolders = new ArrayList<>(subFolders);
        mDocuments = new ArrayList<>(documents);
        mSize = mSubFolders.size() 
            + mDocuments.size();
    }

    /**
     * Copy constructor initializes the fields from the parameters.
     */
    Folder(Folder originalFolder) {
        super(originalFolder.getPath());
        mSubFolders = new ArrayList<>(originalFolder.getSubFolders());
        mDocuments = new ArrayList<>(originalFolder.getDocuments());
        mSize = originalFolder.mSize;
    }

    /**
     * @return The list of subfolders in this folder
     */
    @Override
    public List<Folder> getSubFolders() {
        return mSubFolders;
    }
    
    /**
     * @return The list of documents in this folder
     */
    @Override
    public List<Document> getDocuments() {
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
     *
     */
    @Override
    public void accept(EntryVisitor entryVisitor) {
        entryVisitor.visit(this);
    }

    /**
     *
     */
    @Override
    public Stream<Dirent> stream() {
        return StreamSupport
            .stream(new BatchFolderSpliterator(this),
                    false);
    }

    /**
     *
     */
    @Override
    public Stream<Dirent> parallelStream() {
        return StreamSupport
            .stream(new BatchFolderSpliterator(this),
                    true);
    }

    /*
     * These factor methods are used by clients of this class.
     */

    /**
     * Factory method that creates a folder from the given @a file.
     */
    public static CompletableFuture<Dirent>
        fromDirectory(File file,
                      EntryVisitor entryVisitor,
                      boolean parallel) throws IOException {
        return fromDirectory(file.toPath(),
                             entryVisitor,
                             parallel);
    }

    /**
     * Factory method that creates a folder from the given @a rootPath.
     */
    public static CompletableFuture<Dirent>
        fromDirectory(Path rootPath,
                      EntryVisitor entryVisitor,
                      boolean parallel) throws IOException {
        return CompletableFuture.supplyAsync(() -> {
                Function<Path, Stream<Path>> getStream = ExceptionUtils
                    .rethrowFunction(path
                                     // List all subfolders and
                                     // documents in just this folder.
                                     -> Files.walk(path, 1));

                // Create a stream containing all the contents at the given
                // rootPath.
                Stream<Path> pathStream = getStream.apply(rootPath);

                // Convert the stream to parallel if directed.
                if (parallel)
                    //noinspection ResultOfMethodCallIgnored
                    pathStream.parallel();

                // Create a future to the folder containing all the
                // contents at the given rootPath.
                CompletableFuture<Folder> folderFuture = pathStream
                    // Eliminate ourselves to avoid infinite recursion.
                    .filter(path -> !path.equals(rootPath))

                    // Terminate the stream and create a Folder
                    // containing all entries in this folder.
                    .collect(FolderCollector.toFolder(entryVisitor,
                                                      parallel));

                // Return a Folder containing the contents of this
                // directory.
                return folderFuture.join();
            })
            .thenApply((Dirent folder)
                       -> {
                           // Set the path of the folder.
                           folder.setPath(rootPath);
                            ((Folder) folder).computeSize();

                           if (entryVisitor != null)
                               folder.accept(entryVisitor);

                           return folder;
                       });
    }

    private void computeSize() {
        long folderCount = getSubFolders()
            .stream()
            .mapToLong(subFolder -> subFolder.mSize)
            // .peek(System.out::println)
            .sum();
        long docCount = getDocuments()
            .stream()
            // .peek(System.out::println)
            .count();
        mSize = folderCount + docCount + 1;
    }

    /*
     * The methods below are used by the FolderCollector.
     */

    /**
     *
     */
    void addEntry(Path entry,
                  EntryVisitor entryVisitor,
                  boolean parallel) {
        Function<Path, CompletableFuture<Dirent>> getFolder = ExceptionUtils
            .rethrowFunction(file 
                             -> Folder.fromDirectory(file,
                                                     entryVisitor,
                                                     parallel));
        Function<Path, CompletableFuture<Dirent>> getDocument = ExceptionUtils
            .rethrowFunction(path
                             -> Document.fromPath(path,
                                                  entryVisitor));

        if (Files.isDirectory(entry))
            mSubFolderFutures.add(getFolder.apply(entry));
        else
            mDocumentFutures.add(getDocument.apply(entry));
    }

    /**
     *
     */
    Folder addAll(Folder folder) {
        mSubFolderFutures.addAll(folder.mSubFolderFutures);
        mDocumentFutures.addAll(folder.mDocumentFutures);
        return this;
    }

    /**
     *
     * @return A future to the Folder that will complete when all
     * entries in the Folder complete
     */
    CompletableFuture<Folder> joinAll() {
        // Create an array containing all the futures for subfoldres
        // and documents.
        CompletableFuture[] futures =
                ArrayUtils.concat(mSubFolderFutures,
                                  mDocumentFutures);

        // Create a future that will complete when all the
        // futures have completed.
        CompletableFuture<Void> allDoneFuture =
            CompletableFuture.allOf(futures);
            
        return allDoneFuture
            .thenApply(v -> {
                             mSubFolders = mSubFolderFutures
                                 .stream()
                                 .map(entryCompletableFuture
                                      -> (Folder) entryCompletableFuture.join())
                                 .collect(toList());

                             mDocuments = mDocumentFutures
                                 .stream()
                                 .map(entryCompletableFuture
                                      -> (Document) entryCompletableFuture.join())
                                 .collect(toList());
                             mSize = mSubFolders.size() + mDocuments.size();
                             return this;
                });
    }
}
