package folder;

import utils.ExceptionUtils;
import utils.Options;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
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
     * The list of subfolders contained in this folder.
     */
    private final List<Dirent> mSubFolders;

    /**
     * The list of documents contained in this folder.
     */
    private final List<Dirent> mDocuments;

    /**
     * Constructor initializes the fields.
     */
    Folder(Path path) {
        super(path, 1);

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
        if (Options.getInstance().useRecursiveSpliterator())
            return new RecursiveFolderSpliterator(this);
        else
            return new BatchFolderSpliterator(this);
    }

    /**
     * Create a parallel or sequential stream to process the contents
     * of this {@link Dirent}.
     *
     * @param parallel True if creating a parallel stream, else false
     *                 if creating a sequential stream.
     * @return A stream containing all elements rooted at this
     *         directory entry
     */
    @Override
    public Stream<Dirent> stream(boolean parallel) {
        return StreamSupport.stream(spliterator(),
                                    parallel);
    }

    /*
     * The following factory methods are used by clients of this
     * class.
     */

    /**
     * This factory method creates a folder from the given {@code file}.
     *
     * @param file The file associated with the folder in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return An open document
     */
    public static Dirent fromDirectory(File file,
                                       boolean parallel) {
        return fromDirectory(file.toPath(),
                             parallel);
    }

    /**
     * This factory method creates a folder from the given {@code
     * rootPath}.
     *
     * @param rootPath The path of the folder in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return An open folder containing all contents in the {@code rootPath}
     */
    public static Dirent fromDirectory(Path rootPath,
                                       boolean parallel) {
        // This function creates a stream containing all contents at
        // the given rootPath.
        Function<Path, Stream<Path>> getStream = ExceptionUtils
            // An adapter that simplifies checked exceptions.
            .rethrowFunction(path -> Files
                             // Stream all contents in this path.
                             .walk(path,
                                   // Limit to just this folder.
                                   1));

        // Create a stream containing all the contents at the given
        // rootPath.
        Stream<Path> pathStream = getStream.apply(rootPath);

        // Convert the stream to parallel if directed.
        if (parallel)
            //noinspection ResultOfMethodCallIgnored
            pathStream.parallel();

        // Create and return a folder containing all the contents at
        // the given rootPath.
        return pathStream
            // Eliminate rootPath to avoid infinite recursion.
            .filter(path -> !path.equals(rootPath))

            // Terminate the stream and create a folder containing all
            // entries in this folder.
            .collect(FolderCollector.toFolder(parallel, rootPath));
    }

    /*
     * The methods below are used by the FolderCollector.
     */

    /**
     * Add a new {@code entry} to the appropriate list of futures.
     */
    void addEntry(Path entry,
                  boolean parallel) {
        // Add entry to the appropriate list.
        if (Files.isDirectory(entry)) {
            // Synchronously (and recursively) create a folder from the
            // entry.
            Dirent folder = Folder.fromDirectory(entry, parallel);

            // Add the new folder to the subfolders list.
            mSubFolders.add(folder);

            // Increase the size of this folder by the size of the new
            // folder.
            addToSize(folder.getSize());
        } else {
            // Synchronously create a document from the entry and add
            // the document to the documents list.
            mDocuments.add(Document.fromPath(entry));

            // Increase the size by 1.
            addToSize(1);
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
        mSubFolders.addAll(folder.mSubFolders);
        mDocuments.addAll(folder.mDocuments);

        // Update the size.
        addToSize(folder.getSize());

        // Return this object.
        return this;
    }
}
