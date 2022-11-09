package folder;

import utils.ListAndArrayUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This custom {@link Collector} converts a stream of {@link Path}
 * objects into a {@link CompletableFuture} to single {@link Folder}
 * that forms the root of a recursive directory structure.
 */
public class FolderCollector
      implements Collector<Path,
                           Folder,
                           CompletableFuture<Folder>> {
    /**
     * Path for the {@link Folder}.
     */
    private final Path mPath;

    /**
     * Constructor initializes the field.
     */
    FolderCollector(Path path) {
        mPath = path;
    }

    /**
     * This factory method returns a {@link Supplier} that creates and
     * returns a new mutable result container that holds all the
     * documents and subfolders in the stream.
     *
     * @return A {@link Supplier} that creates a mutable result
     *         container
     */
    @Override
    public Supplier<Folder> supplier() {
        return () -> new Folder(mPath);
    }
    
    /**
     * This factory method returns a {@link BiConsumer} that adds a
     * {@link Path} to the mutable result container.
     *
     * @return A {@link BiConsumer} that adds a {@link Path} to the
     *         mutable result container
     */
    @Override
    public BiConsumer<Folder, Path> accumulator() {
        // Return a BiConsumer that adds a Path to the mutable result
        // container.
        return Folder::addEntry;
    }

    /**
     * This factory method merges the contents of two subfolders into
     * a single {@link Folder}.
     *
     * @return A {link BinaryOperation} that merges two subfolders
     *         into a combined {@link Folder} result
     */
    @Override
    public BinaryOperator<Folder> combiner() {
        // Merge contents of two subfolders.
        return Folder::merge;
    }

    /**
     * Perform the final transformation from the intermediate
     * accumulation type {@link Folder} to the final result type
     * {@link CompletableFuture<Folder>}.
     *
     * @return A {@link Function} that transforms the intermediate
     *         result to the final result, which is a {@link Folder}
     */
    @Override
    public Function<Folder, CompletableFuture<Folder>> finisher() {
        return folder -> {
            // Create an array containing all the futures for
            // subfolders and documents.
            var futures =
                ListAndArrayUtils.concat(folder.mSubFolderFutures,
                                         folder.mDocumentFutures);

            if (futures == null) {
                // This is an empty Folder (i.e., with no subfolders
                // or documents), so we're done.
                return CompletableFuture.completedFuture(folder);
            } else {
                return CompletableFuture
                    // Create a CompletableFuture that emits a Folder
                    // when all the other CompletableFuture objects
                    // complete.
                    .allOf(Objects.requireNonNull(futures))

                    // Return a CompletableFuture to this Folder after
                    // first initializing its subfolder/document
                    // fields.
                    .thenApply(folder::whenComplete);
            }
        };
    }

    /**
     * Returns an immutable {@code Set} of {@link
     * Collector.Characteristics} indicating the characteristics of
     * this {@link Collector}.
     *
     * @return An immutable {@link Set} of {@link
     * Collector.Characteristics}, which in this case is {@code
     * UNORDERED}
     */
    @Override
    public Set<Collector.Characteristics> characteristics() {
        // Return an immutable set of collector characteristics, which
        // in this case is UNORDERED.
        return Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED));
    }

    /**
     * This static factory method creates a new {@link FolderCollector}.
     *
     * @return A new {@link FolderCollector}
     */
    public static Collector<Path, Folder, CompletableFuture<Folder>> 
        toFolder(Path path) {
        // Return a new FolderCollector.
        return new FolderCollector(path);
    }
}
