package folder;

import java.util.function.Consumer;

/**
 * Defines the interface for visiting Folder or Document directory
 * entries.
 */
public interface EntryVisitor {
    /**
     * Visit the @a folder.
     */
    void visit(Folder folder);

    /**
     * Visit the @a document.
     */
    void visit(Document document);

    /**
     * Factory method that creates an instance of @a EntryVisitor with
     * the given @a folderConsumer and @a documentConsumer.xs
     */
    public static EntryVisitor of(Consumer<Folder> folderConsumer,
                                  Consumer<Document> documentConsumer) {
        return new EntryVisitor() {
            @Override
            /**
             * Vist the @a folder.
             */ 
            public void visit(Folder folder) {
                folderConsumer.accept(folder);
            }

            /**
             * Vist the @a document.
             */ 
            @Override
            public void visit(Document document) {
                documentConsumer.accept(document);
            }
        };
    }
}
