package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
     * Factory method that creates a folder from the given @a rootPath.
     */
    static Folder fromDirectory(Path rootPath,
                                boolean parallel) throws IOException {
        // Create a stream containing all the contents at the given
        // rootPath.
        Stream<Path> pathStream = Files
            // Obtain a listing of all the subfolders and documents in
            // just this folder.
            .walk(rootPath, 1);

        if (parallel)
            pathStream.parallel();

        // Create a folder containing all the contents at the given
        // rootPath.
        Folder folder = pathStream
            // Eliminate ourselves!
            .filter(path -> !path.equals(rootPath))

            // Terminate the stream and create a Folder containing all
            // the subfolders and documents in this folder.
            .collect(FolderCollector.toFolder(parallel));

        // Set the name of the folder.
        folder.mName = rootPath.getFileName().toString();

        // Return a Folder containing the contents of this directory.
        return folder;
    }
}
