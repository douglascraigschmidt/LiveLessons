package folders.folder;

import folders.utils.ExceptionUtils;
import reactor.core.publisher.Mono;

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
    private Document(CharSequence input,
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
     * This factory method asynchronously creates a document from the
     * file at the given {@code path}.
     *
     * @param path The path of the document in the file system
     * @return An {@link Dirent} object containing the document's contents
     */
    static Dirent fromPath(File path) {
        // Create an exception adapter.
        Function<Path, byte[]> getBytes = ExceptionUtils
            // mMake it easier to use a checked exception.
            .rethrowFunction(Files::readAllBytes);

        // Create a new document containing all the bytes of the
        // file at the given path.
        return new Document(new String(getBytes.apply(path.toPath())),
                            path);
    }

    /**
     * @return True if {@code dirent} is a document, else false
     */
    public static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        // return dirent instanceof Document;
        return dirent instanceof Document;
    }
}
