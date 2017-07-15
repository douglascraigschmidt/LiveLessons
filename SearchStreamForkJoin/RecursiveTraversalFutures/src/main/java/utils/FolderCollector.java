package utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Implements a custom collector that converts a stream of File
 * objects into a single Folder object.
 */
public class FolderCollector
      implements Collector<File,
                           Folder,
                           Folder> {
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
     * A function that folds a File into the mutable result container.
     *
     * @return a function which folds a File into a mutable result container
     */
    @Override
    public BiConsumer<Folder, File> accumulator() {
        Function<File, Folder> getFolders =
                ExceptionUtils.rethrowFunction(Folder::fromDirectory);
        Function<File, Document> getDocuments =
                ExceptionUtils.rethrowFunction(Document::fromFile);

        return (Folder folder, File entry) -> {
                if (entry.isDirectory())
                    folder.getSubFolders().add(getFolders.apply(entry));
                else
                    folder.getDocuments().add(getDocuments.apply(entry));
        };
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
        return (Folder one, Folder another) -> {
            one.getSubFolders().addAll(another.getSubFolders());
            one.getDocuments().addAll(another.getDocuments());
            return one; 
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
    public Function<Folder, Folder> finisher() {
        return Function.identity();
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
    public static Collector<File, Folder, Folder> toFolder() {
        return new FolderCollector();
    }
}
