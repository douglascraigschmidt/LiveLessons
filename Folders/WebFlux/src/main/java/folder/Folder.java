package folder;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import utils.ReactorUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

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
    Folder() {
        super(new File(""), 1);

        mSubFolders = new ArrayList<>();
        mDocuments = new ArrayList<>();
    }

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
     * rootFile}.
     *
     * @param rootFile The root file in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     *
     * @return An open folder containing all contents in the {@code rootFile}
     */
    public static Mono<Dirent> fromDirectory(File rootFile,
                                             boolean parallel) {
        return Flux
            // Create a flux stream from the list of files.
            .fromArray(Objects.requireNonNull(rootFile.listFiles()))

            // Use the Reactor flatMap() idiom to (conditionally) run
            // this code concurrently or sequentially.
            .flatMap(file -> ReactorUtils
                     // Create a flux from this file either
                     // concurrently or sequentially.
                     .justConcurrentIf(file, parallel)

                     // Eliminate rootPath to avoid infinite
                     // recursion.
                     .filter(path -> !path.equals(rootFile))

                     // Create a stream of dirents.
                     .flatMap(path -> Folder
                              // Create and return a dirent containing
                              // all the contents at the given path.
                              .createEntry(path, parallel)))

            // Collect the results into a folder containing all the
            // entries in stream.
            .collect(Collector
                     // Create a custom collector.
                     .of(() -> new Folder(rootFile),
                         Folder::addEntry,
                         Folder::merge));
    }

    /**
     * This factory method creates a folder from the given {@code
     * rootFile} in parallel.
     *
     * @param rootFile The root file in the file system
     * @return An open folder containing all contents in the {@code rootFile}
     */
    public static Mono<Dirent> fromDirectoryParallel(File rootFile) {
        // Create and return a dirent containing all the contents at
        // the given path.
        return ReactorUtils
            // Create a parallel flux from the list of files.
            .fromArrayParallel
            (Objects.requireNonNull(rootFile.listFiles()))

            // Eliminate rootPath to avoid infinite recursion.
            .filter(path -> !path.equals(rootFile))

            // Create and process each entry in parallel.
            .flatMap(Folder::createEntryParallel)

            // Convert parallel flux back to flux.
            .sequential()

            // Collect the results into a folder containing all
            // entries in the stream.
            .collect(Collector
                     // Create a custom collector.
                     .of(() -> new Folder(rootFile),
                         Folder::addEntry,
                         Folder::merge));
    }

    /**
     * Create a new {@code entry} and return it.
     */
    static Mono<Dirent> createEntry(File entry,
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
     * Create a new {@code entry} and return it.
     */
    static Mono<Dirent> createEntryParallel(File entry) {
        // Add entry to the appropriate list.
        if (entry.isDirectory()) {
            // Recursively create a folder from the entry.
            return Folder.fromDirectoryParallel(entry);
        } else {
            // Create a document from the entry and return it.
            return Document.fromPath(entry);
        }
    }

    /**
     * Add a new {@code entry} to the appropriate list of futures.
     */
    void addEntry(Dirent entry) {
        // Add entry to the appropriate list.
        if (entry instanceof Folder) {
            // Add the new folder to the subfolders list.
            mSubFolders.add(entry);

            // Increase the size of this folder by the size of the new
            // folder.
            addToSize(entry.getSize());
        } else {
            // Synchronously create a document from the entry and add
            // the document to the documents list.
            mDocuments.add(entry);

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
