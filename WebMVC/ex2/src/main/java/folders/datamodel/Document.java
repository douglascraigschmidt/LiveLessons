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
}
