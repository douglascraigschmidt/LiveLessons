package folder;

import utils.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
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
     * @return A future to the document that will be complete when the
     *         contents of the document are available
     */
    static CompletableFuture<Dirent> fromPath(Path path) {
        // Return a future that completes once the document's contents
        // are available.
        return CompletableFuture
            .supplyAsync(() -> {
                    Function<Path, byte[]> getBytes = ExceptionUtils
                        .rethrowFunction(Files::readAllBytes);

                    // Create/return a new document (wrapped in a
                    // future) containing file contents at the path.
                    return new Document
                        (new String(getBytes.apply(path)),
                         path);
                });
    }
}
