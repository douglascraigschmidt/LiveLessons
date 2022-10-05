package folders.datamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;
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
    private List<Dirent> SubFolders;

    /**
     * The list of documents contained in this folder.
     */
    private List<Dirent> Documents;

    /**
     * Constructor initializes the fields.
     */
    Folder() {
        super(new File(""), 0);

        SubFolders = new ArrayList<>();
        Documents = new ArrayList<>();
    }

    /**
     * Constructor initializes the fields.
     */
    public Folder(File path) {
        super(path, 1);

        SubFolders = new ArrayList<>();
        Documents = new ArrayList<>();
    }
    
    /**
     * @return The list of subfolders in this folder
     */
    @Override
    public List<Dirent> getSubFolders() { 
        return SubFolders; 
    }

    /**
     * Set the subfolders field.
     */
    @Override
    public void setSubFolders(List<Dirent> subFolders) {
        SubFolders = subFolders;
    }

    /**
     * @return The {@link List} of documents in this folder
     */
    @Override
    public List<Dirent> getDocuments() {
        return Documents;
    }

    /**
     * Set the documents field.
     */
    @Override
    public void setDocuments(List<Dirent> documents) {
        Documents = documents;
    }

    /**
     * This factory method returns a {@link Dirent} that's
     * either a {@link Document} or a {@link Folder}.
     *
     * @param entry A {@link File} entry
     * @param parallel True if
     * @return A new {@link Dirent} that's either a
     *         {@link Document} or a {@link Folder}
     */
    private static Dirent createEntry(File entry,
                                      boolean parallel) {
        return entry.isDirectory()
            // Recursively create a folder from the entry.
            ? Folder.fromDirectory(entry, parallel)

            // Create a document from the entry and return it.
            : Document.fromPath(entry);
    }

    /**
     * This factory method creates a {@link Dirent }folder from the
     * given {@code rootFile}.
     *
     * @param rootFile A root file in the file system
     * @param parallel A flag that indicates whether to create the
     *                 folder sequentially or in parallel
     * @return A {@link Dirent} folder containing all contents in the
     *         {@code rootFile}
     */
    public static Dirent fromDirectory(File rootFile,
                                       boolean parallel) {
        return StreamSupport
            // Create a parallel stream.
            .stream(Arrays
                    // Convert the array of File objects
                    // into a List.
                    .asList(Objects
                            .requireNonNull(rootFile
                                            .listFiles()))

                    // Convert the List into a parallel stream.
                    .spliterator(), parallel)

            // Eliminate rootPath to avoid infinite recursion.
            .filter(path -> !path.equals(rootFile))

            // Create a stream of Dirent objects.
            .map(path -> Folder
                 // Create and return a Dirent containing all the
                 // contents at the given path.
                 .createEntry(path, parallel))

            // Collect the results into a Folder containing all the
            // entries in stream.
            .collect(Collector
                     // Create a custom collector.
                     .of(() -> new Folder(rootFile),
                         Folder::addEntry,
                         Folder::merge));
    }

    /**
     * Add a new {@code entry} to the appropriate list of futures.
     */
    private void addEntry(Dirent entry) {
        // Add entry to the appropriate list.
        if (entry instanceof Folder) {
            // Add the new folder to the subfolders list.
            getSubFolders().add(entry);

            // Increase the size of this folder by the size of the new
            // folder.
            addToSize(entry.getSize());
        } else {
            // Synchronously create a document from the entry and add
            // the document to the documents list.
            getDocuments().add(entry);

            // Increase the size by 1.
            addToSize(1);
        }
    }

    /**
     * Merge contents of {@code folder} into contents of this folder.
     *
     * @param folder The {@link Folder} to merge from
     * @return The merged {@link Folder}
     */
    private Folder merge(Folder folder) {
        // Update the lists.
        getSubFolders().addAll(folder.getSubFolders());
        getDocuments().addAll(folder.getDocuments());

        // Update the size.
        addToSize(folder.getSize());

        // Return this object.
        return this;
    }
}
