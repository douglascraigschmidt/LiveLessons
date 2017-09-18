package folder;

import utils.ArrayUtils;

import java.nio.file.Path;
import java.util.Collections;
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
 * objects into a single Folder object that forms the root of a
 * recursive directory structure.
 */
public class FolderCollector
      implements Collector<Path,
                           Folder,
                           CompletableFuture<Folder>> {
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
            -> folder.addEntry(entry);
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
        return (folder1, folder2) -> {
                folder1.mSubFolderFutures.addAll(folder2.mSubFolderFutures);
                folder1.mDocumentFutures.addAll(folder2.mDocumentFutures);
                return folder1;
            };
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
            // Create an array containing all the futures for subfolders
            // and documents.
            CompletableFuture[] futures =
                    ArrayUtils.concat(folder.mSubFolderFutures,
                            folder.mDocumentFutures);

            // Create a future that will complete when all the other
            // futures have completed.
            CompletableFuture<Void> allDoneFuture =
                    CompletableFuture.allOf(futures);

            // Return a future to this folder after first initializing its
            // subfolder/document fields after allDoneFuture completes.
            return allDoneFuture
                    .thenApply(v -> {
                        // Initialize all the subfolders.
                        folder.mSubFolders = folder.mSubFolderFutures
                                // Convert the list into a stream.
                                .stream()

                                // Convert the future to a directory entry.
                                // Note that join() won't block since all the
                                // futures have completed by this point.
                                .map(CompletableFuture::join)

                                // Trigger intermediate processing and return
                                // a list.
                                .collect(toList());

                        // Initialize all the documents.
                        folder.mDocuments = folder.mDocumentFutures
                                // Convert the list into a stream.
                                .stream()


                                // Convert the future to a directory entry.
                                // Note that join() won't block since all the
                                // futures have completed by this point.
                                .map(CompletableFuture::join)

                                // Trigger intermediate processing and return
                                // a list.
                                .collect(toList());

                        // Initialize the size.
                        folder.mSize = folder.mSubFolders.size() + folder.mDocuments.size();

                        // Return this folder, which is converted to a
                        // future to a folder.
                        return folder;
                    });
        };
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
    public static Collector<Path, Folder, CompletableFuture<Folder>>toFolder() {
        return new FolderCollector();
    }
}
