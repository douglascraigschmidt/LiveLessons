package folder;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

/**
 * This super class defines the common capabilities provided by a
 * directory entry.  It is inherited by the {@link Folder} and {@link
 * Document} subclasses.
 */
public abstract class Dirent {
    /**
     * The {@link Path} of this {@link Dirent}.
     */
    private Path mPath;

    /**
     * The total number of entries in this recursively structured
     * folder.
     */
    long mSize;

    /**
     * Default constructor.
     */
    Dirent() {
    }

    /**
     * Constructor initializes the fields.
     */
    Dirent(Path path, long size) {
        mPath = path;
        mSize = size;
    }

    /**
     * @return Name of the {@link Dirent}
     */
    public String getName() {
        return mPath.getFileName().toString();
    }

    /**
     * Set {@link Path} of the {@link Dirent}.
     */
    public void setPath(Path path) {
        mPath = path;
    }

    /**
     * @return {@link Path} of the {@link Dirent}
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
     * @return The contents of this {@link Dirent}
     */
    public CharSequence getContents() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return The total number of entries in this recursively
     * structured dirent.
     */
    public long getSize() {
        return mSize;
    }

    /**
     * Set the total number of entries in this recursively
     * structured dirent.
     */
    public void setSize(long size) {
        mSize = size;
    }

    /**
     * Add {@code size} to the current size.
     */
    public void addToSize(long size) {
        mSize += size;
    }

    /**
     * @return A sequential stream containing all elements rooted at
     *         this directory entry
     */
    public Stream<Dirent> stream() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return A parallel stream containing all elements rooted at
     *         this directory entry
     */
    public Stream<Dirent> parallelStream() {
        throw new UnsupportedOperationException();
    }
}
