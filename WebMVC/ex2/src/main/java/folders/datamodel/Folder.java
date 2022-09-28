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
     * Add a new {@code entry} to the appropriate list of futures.
     */
    public void addEntry(Dirent entry) {
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
    public Folder merge(Folder folder) {
        // Update the lists.
        getSubFolders().addAll(folder.getSubFolders());
        getDocuments().addAll(folder.getDocuments());

        // Update the size.
        addToSize(folder.getSize());

        // Return this object.
        return this;
    }
}
