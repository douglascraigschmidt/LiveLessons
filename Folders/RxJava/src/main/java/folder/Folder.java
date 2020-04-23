package folder;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import utils.RxUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    Folder(File path) {
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
     * This factory method creates a folder from the given {@code
     * rootPath}.
     *
     * @param rootPath The path of the folder in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return An open folder containing all contents in the {@code rootPath}
     */
    public static Dirent blockingFromDirectory(File rootPath,
                                       boolean parallel) {
        return fromDirectory(rootPath, parallel)
            .blockingGet();
    }

    public static Single<Dirent> fromDirectory(File rootPath,
                                               boolean parallel) {
        return Observable
            // Create a stream of observables from the list of files.
            .fromArray(Objects.requireNonNull(rootPath.listFiles()))

            // This flatMap() idiom is used to (conditionally) run
            // this code concurrently.
            .flatMap(file -> Observable
                     // Create an observable from this file.
                     .just(file)

                     // Conditionally convert to either sequential or
                     // concurrent processing.
                     .compose(RxUtils.concurrentObservableIf(parallel))

                     // Eliminate rootPath to avoid infinite
                     // recursion.
                     .filter(path -> !path.equals(rootPath))

                     .flatMap(path -> Folder
                              // Create and return a Dirent containing
                              // all the contents at the given path.
                              .createEntry(path, parallel)
                              .toObservable()))

            // Collect the results into a folder containing all the
            // entries in stream.
            .collectInto(new Folder(rootPath),
                         Folder::addEntry);
    }

    /**
     * Create a new {@code entry} and return it.
     */
    static Single<Dirent> createEntry(File entry,
                                      boolean parallel) {
        // Add entry to the appropriate list.
        if (entry.isDirectory()) {
            // Recursively create a folder from the entry.
            return Folder.fromDirectory(entry, parallel);
        } else {
            // Create a document from the entry and return it.
            return Document.fromPath(entry);
        }
    }

    /**
     * Add a new {@code entry} to the appropriate list of futures.
     */
    static void addEntry(Dirent parent, Dirent entry) {
        // Cast the parent to a folder.
        Folder folder = (Folder) parent;

        // Add entry to the appropriate list.
        if (entry instanceof Folder) {
            // Add the new folder to the subfolders list.
            folder.mSubFolders.add(entry);

            // Increase the size of this folder by the size of the new
            // folder.
            folder.addToSize(entry.getSize());
        } else {
            // Synchronously create a document from the entry and add
            // the document to the documents list.
            folder.mDocuments.add(entry);

            // Increase the size by 1.
            folder.addToSize(1);
        }
    }
}
