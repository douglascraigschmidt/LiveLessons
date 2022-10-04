package folder;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This custom {@link Collector} recursively converts a stream of
 * {@link Path} objects into a single {@link Folder} object that forms
 * the root of a hierarchical directory structure.
 */
public class FolderCollector
      implements Collector<Path,
                           Folder,
                           Folder> {
    /**
     * Indicates whether processing should occur in parallel.
     */
    private final boolean mParallel;

    /**
     * Path for the folder.
     */
    private final Path mPath;

    /**
     * Constructor initializes the field.
     */
    private FolderCollector(boolean parallel,
                            Path path) {
        mParallel = parallel;
        mPath = path;
    }

    /**
     * This factory method returns a {@link Supplier} that creates and
     * returns a new mutable result container that holds all the
     * documents and subfolders in the stream.
     *
     * @return A {@link Supplier} that returns a new, mutable result
     *         container
     */
    @Override
    public Supplier<Folder> supplier() {
        return () -> new Folder(mPath);
    }
    
    /**
     * This factory method returns a {@link BiConsumer} that adds a
     * path to the mutable result container.
     *
     * @return A {@link BiConsumer} that adds a path to the mutable
     *         result container
     */
    @Override
    public BiConsumer<Folder, Path> accumulator() {
        // Return a biconsumer that adds a path to the mutable result
        // container.

        return (folder, entry) ->
                // Add the entry to the folder.
                folder.addEntry(entry, mParallel);
    }

    /**
     * This factory method merges the contents of two subfolders into
     * a single folder.
     *
     * @return A {@link BinaryOperation} that merges two subfolders
     *         into a combined folder result
     */
    @Override
    public BinaryOperator<Folder> combiner() {
        // Merge contents of two subfolders.
        return Folder::merge;
    }

    /**
     * This factory method returns a function that performs the final
     * transformation of the mutable result container type {@link
     * Folder} to the final result type {@link Folder}.
     *
     * @return A {@link Function} that transforms the intermediate
     *         result to the final result
     */
    @Override
    public Function<Folder, Folder> finisher() {
        // This is a no-op since IDENTITY_FINISH is set.
        return null;
    }

    /**
     * Returns a {@link Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable {@link Set} of collector characteristics,
     *         which in this case is {@code UNORDERED} and {@code
     *         IDENTITY_FINISH}
     */
    @Override
    public Set<Collector.Characteristics> characteristics() {
        // Return an immutable set of collector characteristics, which
        // in this case is UNORDERED and IDENTITY_FINISH.
        return Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED,
                                        Characteristics.IDENTITY_FINISH));
    }

    /**
     * This factory method creates a new {@link FolderCollector} that
     * encapsulates the contents at the {@code path}.
     *
     * @param parallel True if processing should run in parallel, else
     *                 false
     * @param path The pathname for the folder to encapsulate
     * @return A {@link FolderCollector}
     */
    public static Collector<Path, Folder, Folder> toFolder(boolean parallel,
                                                           Path path) {
        // Return a new folder collector.
        return new FolderCollector(parallel, path);
    }
}
