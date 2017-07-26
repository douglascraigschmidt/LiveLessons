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
     * Accept the @a entryVisitor in accordance with the Visitor pattern.
     */
    @Override
    public void accept(EntryVisitor entryVisitor) {
        entryVisitor.visit(this);
    }

    /**
     * Factory method that asynchronously creates a document from the
     * file at the given @a path.
     *
     * @param path The path of the document in the file system
     * @param entryVisitor A callback that's invoked once the 
     *                     document's contents are available
     *
     * @return A future to the document that will be complete when the
     *         contents of the document are available
     */
    static CompletableFuture<Dirent> fromPath(Path path,
                                              final EntryVisitor entryVisitor) {

        // Return a future that completes once the document's contents
        // are available and the entryVisitor callback is made.
        return CompletableFuture
            .supplyAsync(() 
                         -> {
                             Function<Path, byte[]> getBytes = ExceptionUtils
                                 .rethrowFunction(Files::readAllBytes);

                             // Create a new document containing all
                             // bytes of the file at the given path.
                             Dirent document = new Document
                                 (new String(getBytes.apply(path)),
                                  path);

                             if (entryVisitor != null)
                                 // Invoke the visitor callback.
                                 document.accept(entryVisitor);

                            // Return the document, which is wrapped
                            // in a future.
                             return document;
                         });
    }
}
