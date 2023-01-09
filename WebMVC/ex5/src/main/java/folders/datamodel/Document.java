package folders.datamodel;

import folders.utils.ExceptionUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * Represents the contents of a document, which is stored as a
 * {@link CharSequence} of bytes.
 */
public class Document 
       extends Dirent {
    /**
     * Stores the contents of the file.
     */
    private CharSequence Contents;

    /**
     * Constructor initializes the fields.
     */
    public Document() {
        super(new File(""), 0);
    }

    /**
     * Constructor sets the field.
     */
    public Document(CharSequence input,
                    File path) {
        super(path, 1);
        
        Contents = input;
    }

    /**
     * @return The contents of this document
     */
    @Override
    public CharSequence getContents() {
        return Contents;
    }

    /**
     * Set the contents of this document.
     */
    public void setContents(CharSequence contents) {
        Contents = contents;
    }

    /**
     * This factory method creates a {@link Dirent} document from the
     * file at the given {@code path}.
     *
     * @param path The path of the document in the file system
     * @return A {@link Dirent} document containing the file's
     *         contents at the given {@code path}
     */
    public static Dirent fromDocument(File path) {
        // Create an exception adapter.
        Function<Path, byte[]> getBytes = ExceptionUtils
            // mMake it easier to use a checked exception.
            .rethrowFunction(Files::readAllBytes);

        // Create a new document containing all the bytes of the
        // file at the given path.
        return new Document(new String(getBytes.apply(path.toPath())),
                            path);
    }
}
