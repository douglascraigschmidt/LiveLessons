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
 * Implements a custom collector that converts a stream of Path
 * objects into a completable future to single Folder object that
 * forms the root of a recursive directory structure.
 */
public class FolderCollector
      implements Collector<Path,
                           Folder,
                           CompletableFuture<Folder>> {
    /**
     * Path for the folder.
     */
    private final Path mPath;

    /**
     * Constructor initializes the field.
     */
    FolderCollector(Path path) {
        mPath = path;
    }

    /**
     * This factory method returns a supplier that creates and returns
     * a new mutable result container that holds all the documents and
     * subfolders in the stream.
     *
     * @return a supplier that returns a new, mutable result container
     */
    @Override
    public Supplier<Folder> supplier() {
        return () -> new Folder(mPath);
    }
    
    /**
     * This factory method returns a biconsumer that adds a path to
     * the mutable result container.
     *
     * @return a biconsumer that adds a path to the mutable result container
     */
    @Override
    public BiConsumer<Folder, Path> accumulator() {
        // Return a biconsumer that adds a path to the mutable result
        // container.
        return Folder::addEntry;
    }

    /**
     * This factory method merges the contents of two subfolders into
     * a single folder.
     *
     * @return a BinaryOperation that merges two subfolders
     *         into a combined folder result
     */
    @Override
    public BinaryOperator<Folder> combiner() {
        // Merge contents of two subfolders.
        return Folder::merge;
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
        return folder -> {
            // Create an array containing all the futures for
            // subfolders and documents.
            var futures =
                ListAndArrayUtils.concat(folder.mSubFolderFutures,
                                         folder.mDocumentFutures);

            if (futures == null) {
                // This is an empty folder (i.e., with no subfolders
                // or documents) so we're done.
                return CompletableFuture.completedFuture(folder);
            } else {
                return CompletableFuture
                    // Create a future that will complete when all the
                    // other futures have completed.
                    .allOf(Objects.requireNonNull(futures))

                    // Return a future to this folder after first
                    // initializing its subfolder/document fields.
                    .thenApply(folder::whenComplete);
            }
        };
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is UNORDERED
     */
    @Override
    public Set<Collector.Characteristics> characteristics() {
        // Return an immutable set of collector characteristics, which
        // in this case is UNORDERED.
        return Collections
            .unmodifiableSet(EnumSet.of(Collector.Characteristics.UNORDERED));
    }

    /**
     * This static factory method creates a new FolderCollector.
     *
     * @return A new FolderCollector
     */
    public static Collector<Path, Folder, CompletableFuture<Folder>> 
        toFolder(Path path) {
        // Return a new folder collector.        
        return new FolderCollector(path);
    }
}
