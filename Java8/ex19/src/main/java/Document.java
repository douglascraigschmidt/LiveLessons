import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents the contents of a document, which is stored as a
 * CharSequence of bytes.
 */
public class Document {
    /**
     * Contents of the document.
     */
    private final CharSequence mInput;

    /**
     * Name of the document.
     */
    private String mName;
    
    /**
     * Constructor sets the field.
     */
    private Document(CharSequence input) {
        mInput = input;
    }

    /**
     * Get the contents of the document.
     */
    public CharSequence getContents() {
        return mInput;
    }

    /**
     * Get the name of the document.
     */
    public String getName() {
        return mName;
    }

    /**
     * Factory method that creates a document from the file at the
     * given @a path.
     */
    static Document fromPath(Path path) throws IOException {
        // Create a new document that contains all the bytes
        // of the file at the given path.
        Document document =
            new Document(new String(Files.readAllBytes(path)));

        // Set the name of the document.
        document.mName = path.getFileName().toString();

        // Return the document.
        return document;
    }
}
