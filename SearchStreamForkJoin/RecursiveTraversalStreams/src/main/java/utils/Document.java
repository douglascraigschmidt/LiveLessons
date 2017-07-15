package utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
     * Factory method that creates a document from the given @a file.
     */
    static Document fromFile(File file) throws IOException {
        return new Document(new String(Files.readAllBytes(file.toPath())));
    }
}

