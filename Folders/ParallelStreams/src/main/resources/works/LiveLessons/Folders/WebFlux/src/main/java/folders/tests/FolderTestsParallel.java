package folders.tests;

import folders.common.FolderOps;
import folders.folder.Dirent;
import folders.folder.Folder;
import folders.common.Options;
import folders.utils.ReactorUtils;
import folders.utils.TestDataFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

import java.util.function.Function;

import static folders.common.Constants.sWORKS;

/**
 * This Java utility class contains methods that run all the tests in
 * parallel using {@code Reactor} {@link ParallelFlux} mechanisms.
 */
public final class FolderTestsParallel {
    /**
     * A Java utility class should have a private constructor.
     */
    private FolderTestsParallel() {}

    /**
     * Asynchronously create an in-memory folder containing all the
     * works in parallel.
     *
     * @return A {@link Mono} that emits a folder containing all the
     * works
     */
    public static Mono<Dirent> createFolderParallel() {
        // Return a Mono to the initialized folder.
        return Folder
            // Asynchronously create a folder with all works in the
            // root directory in parallel.
            .fromDirectoryParallel(TestDataFactory.getRootFolderFile(sWORKS))

            // Cache the results so that they won't be re-emitted
            // repeatedly each time.
            .cache();
    }

    /**
     * Count the number of lines in the recursively structured
     * directory at {@code rootFolder} in parallel.
     *
     * @param rootFolderM A {@link Mono} to an in-memory folder
     *                    containing the works
     * @return A {@link Mono} that emits a count of the number of
     *         lines in the folder emitted by {@code rootFolderM}
     */
    public static Mono<Long> countLinesParallel(Mono<Dirent> rootFolderM) {
        // This function counts the # of lines in a dirent.
        Function<Dirent, Mono<Long>> countLines = document -> FolderOps.
            // Split the document into lines.
            splitAsFlux(document, "\\r?\\n")
            
            // Count # of entries in the stream.
            .count();

        return rootFolderM
            // This code is called after rootFolder is initialized.
            .flatMap(rootFolder -> ReactorUtils
                     // Convert the contents of rootFolder into a
                     // parallel flux stream.
                     .fromIterableParallel(rootFolder)

                     // Only search documents.
                     .filter(FolderOps::isDocument)

                     // Count the # of lines in the document in parallel
                     // with other documents in the dirent.
                     .flatMap(countLines)

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L)
                     
                     // Handle successful result.
                     .doOnSuccess(lineCount -> Options
                                  // Display the result.
                                  .debug("total number of lines = "
                                           + lineCount)));
    }

    /**
     * Find all occurrences of {@code word} in {@code rootFolder} in
     * parallel.
     *
     * @param rootFolderM A {@link Mono} that emits an in-memory
     *                    folder containing the works
     * @param word Word to search for in the folder
     * @return A {@link Mono} containing a count of the number of
     *         lines in the folder emitted by {@code rootFolderM}
     */
    public static Mono<Long> searchFoldersParallel(Mono<Dirent> rootFolderM,
                                                   String word) {
        // This function counts # times word appears in dirent.
        Function<Dirent, Mono<Long>> countMatches = document -> FolderOps
            // Count # of times word appears in the document.
            .occurrencesCount(document, word);

        return rootFolderM
            // This code is called after rootFolder is initialized.
            .flatMap(rootFolder -> ReactorUtils
                     // Convert the contents of rootFolder into a
                     // parallel flux stream.
                     .fromIterableParallel(rootFolder)

                     // Only search documents.
                     .filter(FolderOps::isDocument)

                     // Search document looking for matches.
                     .flatMap(countMatches)

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L)

                     // Handle successful result.
                     .doOnSuccess(wordMatches -> Options
                                  // Display the result.
                                  .debug("total matches of \""
                                           + word
                                           + "\" = "
                                           + wordMatches)));
    }
}
