package folder;

import utils.ArrayUtils;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;

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
     * This factory method returns a supplier that creates and returns
     * a new mutable result container that holds all the documents and
     * subfolders in the stream.
     *
     * @return a supplier that returns a new, mutable result container
     */
    @Override
    public Supplier<Folder> supplier() {
        return Folder::new;
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
            CompletableFuture<Dirent>[] futures =
                ArrayUtils.concat(folder.mSubFolderFutures,
                                  folder.mDocumentFutures);

            if (futures == null) {
                // This is an empty folder (i.e., with no subfolders or
                // documents) so we're done.
                return CompletableFuture.completedFuture(folder);
            } else {
                return CompletableFuture
                    // Create a future that will complete when all the
                    // other futures have completed.
                    .allOf(Objects.requireNonNull(futures))

                    // Return a future to this folder after first
                    // initializing its subfolder/document fields
                    // after allDoneFuture completes.
                    .thenApply(v -> {
                            // Initialize all the subfolders.
                            folder.mSubFolders =
                                collectToList(folder.mSubFolderFutures);

                            // Initialize all the documents.
                            folder.mDocuments =
                                collectToList(folder.mDocumentFutures);

                            // Initialize the size.
                            folder.mSize = folder.mSubFolders.size()
                                + folder.mDocuments.size();

                            // Return this folder, which is converted
                            // to a future to a folder.
                            return folder;
                        });
            }
        };
    }

    /**
     * Converts a list of completable futures to dirents into a list
     * of dirents by joining them, which won't block.
     *
     * @param listOfFutures The list of completable futures to dirents
     *                      to convert
     * @return A list of dirents
     */
    private List<Dirent> collectToList
            (List<CompletableFuture<Dirent>> listOfFutures) {
        return listOfFutures
            // Convert the list into a stream.
            .stream()

            // Convert the future to a directory entry (join() won't
            // block since all the futures have completed by this
            // point).
            .map(CompletableFuture::join)

            // Trigger intermediate processing and return
            // a list.
            .collect(toList());
    }

    /**
     * Returns a {@code Set} of {@code Collector.Characteristics}
     * indicating the characteristics of this Collector.  This set
     * should be immutable.
     *
     * @return An immutable set of collector characteristics, which in
     * this case is simply UNORDERED
     */
    @Override
    public Set<Collector.Characteristics> characteristics() {
        return Collections.emptySet();
    }

    /**
     * This static factory method creates a new FolderCollector.
     *
     * @return A new FolderCollector
     */
    public static Collector<Path, Folder, CompletableFuture<Folder>>toFolder() {
        return new FolderCollector();
    }
}
