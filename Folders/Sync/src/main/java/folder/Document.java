package folder;

import utils.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Represents the contents of a document, which is stored as a
 * CharSequence of bytes.
 */
public class Document 
       extends Dirent {
    /**
     * Constructor sets the field.
     */
    private Document(CharSequence input,
                     Path path) {
        super(path);
        mContents = input;
    }

    /**
     * @return The contents of this Document
     */
    @Override
    public CharSequence getContents() {
        return mContents;
    }

    /**
     * Factory method that asynchronously creates a document from the
     * file at the given @a path.
     *
     * @param path The path of the document in the file system
     * @return An open document
     */
    static Dirent fromPath(Path path) {
        // Create an exception adapter.
        Function<Path, byte[]> getBytes = ExceptionUtils
            .rethrowFunction(Files::readAllBytes);

        // Create and return a new document containing all bytes of
        // the file at the given path.
        return new Document(new String(getBytes.apply(path)),
                            path);
    }
}
