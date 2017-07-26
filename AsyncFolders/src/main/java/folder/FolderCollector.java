package folder;

import utils.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Implements a custom collector that converts a stream of Path
 * objects into a single Folder object that forms the root of a
 * recursive directory structure.
 */
public class FolderCollector
      implements Collector<Path,
                           Folder,
                           CompletableFuture<Folder>> {
    /**
     * The hook that's invoked whenever processing of a folder or a
     * document has completed.
     */
    private EntryVisitor mEntryVisitor;

    /**
     * Indicates whether processing should occur in parallel.
     */
    private boolean mParallel;

    /**
     * Constructor initializes the field.
     */
    private FolderCollector(EntryVisitor entryVisitor,
                            boolean parallel) {
        mEntryVisitor = entryVisitor;
        mParallel = parallel;
    }

    /**
     * A function that creates and returns a new mutable result
     * container that will hold all the documents and subfolders in
     * the stream.
     *
     * @return a function which returns a new, mutable result container
     */
    @Override
    public Supplier<Folder> supplier() {
        return Folder::new;
    }
    
    /**
     * A function that folds a Path into the mutable result container.
     *
     * @return a function which folds a Path into a mutable result container
     */
    @Override
    public BiConsumer<Folder, Path> accumulator() {
        return (Folder folder, Path entry) 
            -> folder.addEntry(entry,
                               mEntryVisitor,
                               mParallel);
    }

    /**
     * A function that accepts two partial results and merges them.
     * The combiner function may fold state from one argument into the
     * other and return that, or may return a new result container.
     *
     * @return a function which combines two partial results into a combined
     * result
     */
    @Override
    public BinaryOperator<Folder> combiner() {
        return Folder::addAll;
    }

    /**
     * Perform the final transformation from the intermediate
     * accumulation type {@code A} to the final result type {@code R},
     * which is a Folder object.
     *
     * @return a function which transforms the intermediate result to
     * the final result, which is a Folder object
     */
    @Override
    public Function<Folder, CompletableFuture<Folder>> finisher() {
        return Folder::joinAll;
    }
    
    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is simply UNORDERED
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set characteristics() {
        return Collections.emptySet();
    }

    /**
     * This static factory method creates a new FolderCollector.
     *
     * @return A new FolderCollector
     */
    public static Collector<Path, Folder, CompletableFuture<Folder>>
                      toFolder(EntryVisitor entryVisitor,
                               boolean parallel) {
        return new FolderCollector(entryVisitor,
                                   parallel);
    }
}
