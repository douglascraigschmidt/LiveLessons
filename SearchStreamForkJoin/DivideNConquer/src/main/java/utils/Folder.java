package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents the contents of a folder, which can be subfolders or
 * documents.
 */
public class Folder {
    /**
     * The list of subfolders contained in this folder.
     */
    private final List<Folder> mSubFolders;

    /**
     * The list of documents contained in this folder.
     */
    private final List<Document> mDocuments;

    /**
     * Name of the folder.
     */
    private String mName;

    /**
     * Constructor initializes the fields.
     */
    Folder() {
        mSubFolders = new ArrayList<>();
        mDocuments = new ArrayList<>();
    }
    
    /**
     * @return Name of the folder
     */
    public String getName() {
        return mName;
    }

    /**
     * Return the list of subfolders in this folder.
     */
    public List<Folder> getSubFolders() {
        return mSubFolders;
    }
    
    /**
     * Return the list of documents in this folder.
     */
    public List<Document> getDocuments() {
        return mDocuments;
    }
    
    /**
     * Factory method that creates a folder from the given @a dir.
     */
    public static Folder fromDirectory(File dir,
                                       boolean parallel) throws IOException {
        //noinspection ConstantConditions
        Stream<File> folderStream = Stream
            // Obtain a listing of all the subfolders and documents in
            // this folder.
            .of(dir.listFiles());

        if (parallel)
            folderStream.parallel();

        Folder folder = folderStream
            // Terminate the stream and create a Folder containing all
            // the subfolders and documents in this folder.
            .collect(FolderCollector.toFolder(parallel));

        // Set the name of the folder.
        folder.mName = dir.getName();

        // Return a Folder containing the contents of this directory.
        return folder;
    }
}

