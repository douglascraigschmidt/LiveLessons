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
     *
     */
    @Override
    public void accept(EntryVisitor entryVisitor) {
        entryVisitor.visit(this);
    }

    /**
     * Factory method that creates a document from the file at the
     * given @a path.
     */
    static CompletableFuture<Dirent> fromPath(Path path,
                                              final EntryVisitor entryVisitor) {

        // Return a future that completes once the document is read and the
        // callback is made.
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
                                 // Invoke the callback.
                                 document.accept(entryVisitor);

                             return document;
                         });
    }
}
