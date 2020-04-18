package folder;

import utils.ExceptionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
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
                           Folder> {
    /**
     * Indicates whether processing should occur in parallel.
     */
    private boolean mParallel;

    /**
     * Constructor initializes the field.
     */
    private FolderCollector(boolean parallel) {
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
     * accumulation type {@code A} to the final result type {@code
     * R}.
     *
     * @return a function which transforms the intermediate result to
     * the final result
     */
    @Override
    public Function<Folder, Folder> finisher() {
        return Function.identity();
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is UNORDERED and IDENTITY_FINISH.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set characteristics() {
        return Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                                                      Collector.Characteristics.IDENTITY_FINISH));
    }

    /**
     * This static factory method creates a new FolderCollector.
     *
     * @return A new FolderCollector
     */
    public static Collector<Path, Folder, Folder>toFolder(boolean parallel) {
        return new FolderCollector(parallel);
    }
}
