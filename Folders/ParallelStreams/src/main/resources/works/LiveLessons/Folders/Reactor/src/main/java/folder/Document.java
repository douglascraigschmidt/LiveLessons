package folder;

import reactor.core.publisher.Mono;
import utils.ExceptionUtils;

import java.io.File;
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
     * Contents of the document.
     */
    protected CharSequence mContents;

    /**
     * Constructor sets the field.
     */
    private Document(CharSequence input,
                     File path) {
        super(path, 1);
        mContents = input;
    }

    /**
     * @return The contents of this document
     */
    @Override
    public CharSequence getContents() {
        return mContents;
    }

    /**
     * This factory method asynchronously creates a document from the
     * file at the given {@code path}.
     *
     * @param path The path of the document in the file system
     * @return An {@code} Dirent} object containing the document's contents
     */
    static Mono<Dirent> fromPath(File path) {
        // Create an exception adapter.
        Function<Path, byte[]> getBytes = ExceptionUtils
            // make it easier to use check exception.
            .rethrowFunction(Files::readAllBytes);

        // Return a mono to the new document.
        return Mono
            // Create a new document containing all the bytes of the
            // file at the given path.
            .just(new Document(new String(getBytes.apply(path.toPath())),
                               path));
    }
}
