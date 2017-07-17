package folder;

import java.util.function.Consumer;

public interface EntryVisitor {
    void visit(Folder folder);

    void visit(Document document);

    public static EntryVisitor of(Consumer<Folder> folderConsumer,
                                  Consumer<Document> documentConsumer) {
        return new EntryVisitor() {
            @Override
            public void visit(Folder folder) {
                folderConsumer.accept(folder);
            }

            @Override
            public void visit(Document document) {
                documentConsumer.accept(document);
            }
        };
    }
}
