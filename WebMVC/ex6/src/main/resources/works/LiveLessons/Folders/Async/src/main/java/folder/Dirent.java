package folder;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * This super class defines the common capabilities provided by a
 * directory entry.  It is inherited by the Folder and Document
 * subclasses.
 */
public abstract class Dirent {
    /**
     * Path of the document.
     */
    private Path mPath;

    /**
     * Contents of the document.
     */
    CharSequence mContents;

    /**
     * Default constructor.
     */
    Dirent() {
    }

    /**
     * Constructor initializes the field.
     */
    Dirent(Path path) {
        mPath = path;
    }

    /**
     * @return Name of the dirent
     */
    public String getName() {
        return mPath.getFileName().toString();
    }

    /**
     * Set path of the dirent.
     */
    public void setPath(Path path) {
        mPath = path;
    }

    /**
     * @return Path of the dirent
     */
    public Path getPath() {
        return mPath;
    }

    /**
     * @return The list of subfolders in this folder
     */
    public List<Dirent> getSubFolders() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * @return The list of documents in this folder
     */
    public List<Dirent> getDocuments() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return The contents of this Dirent
     */
    public CharSequence getContents() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return A sequential stream containing all elements rooted at
     * this directory entry
     */
    public Stream<Dirent> stream() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return A parallel stream containing all elements rooted at
     * this directory entry
     */
    public Stream<Dirent> parallelStream() {
        throw new UnsupportedOperationException();
    }
}
