package folder;

import utils.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Represents the contents of a document, which is stored as a {@link
 * CharSequence} of bytes.
 */
public class Document 
       extends Dirent {
    /**
     * Contents of the {@link Document}.
     */
    CharSequence mContents;

    /**
     * Constructor sets the fields.
     */
    private Document(CharSequence input,
                     Path path) {
        super(path, 1);
        mContents = input;
    }

    /**
     * @return The contents of this {@link Document}
     */
    @Override
    public CharSequence getContents() {
        return mContents;
    }

    /**
     * Factory method that asynchronously creates a {@link Document}
     * from the file at the given {@link Path}.
     *
     * @param path The {@link Path} of the {@link Document} in the file system
     * @return A {@link CompletableFuture} to the {@link Document}
     *         emitted when its contents are available
     */
    static CompletableFuture<Dirent> fromPath(Path path) {
        // This function gets all bytes from a file.
        Function<Path, byte[]> getBytes = ExceptionUtils
                // This adapter simplifies checked exceptions.
                .rethrowFunction(Files::readAllBytes);

        // Return a CompletableFuture that completes once the
        // Document's contents are available.
        return CompletableFuture
            .supplyAsync(() ->
                         // Create/return a new Document (wrapped in a
                         // CompletableFuture) containing file
                         // contents at the path.
                         new Document(new String(getBytes.apply(path)),
                                      path));
    }
}
