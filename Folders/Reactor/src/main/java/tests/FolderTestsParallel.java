package tests;

import folder.Dirent;
import folder.Folder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.Options;
import utils.ReactorUtils;
import utils.TestDataFactory;

import java.util.function.Function;

import static tests.FolderTestsUtils.occurrencesCount;

/**
 * This Java utility class contains methods that run all the tests in
 * parallel using Reactor ParallelFlux mechanisms.
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
     * @param works Name of the directory in the file system containing the works.
     * @return A mono to a folder containing all works in {@code works}
     */
    public static Mono<Dirent> createFolderParallel(String works) {
        // Return a mono to the initialized folder.
        return Folder
            // Asynchronously create a folder with all works in the
            // root directory in parallel.
            .fromDirectoryParallel(TestDataFactory.getRootFolderFile(works))

            // Cache the results so that they won't be re-emitted
            // repeatedly each time.
            .cache();
    }

    /**
     * Count # of lines in the recursively structured directory at
     * {@code rootFolder} in parallel.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     */
    public static void countLinesParallel(Mono<Dirent> rootFolderM) {
        // This function counts the # of lines in a dirent.
        Function<Dirent, Mono<Long>> countLines = document -> FolderTestsUtils.
            // Split the document into lines.
            splitAsFlux(document,
                        "\\r?\\n|\\r")
            
            // Count # of entries in the stream.
            .count();

        rootFolderM
            // This code is called after rootFolder is initialized.
            .flatMap(rootFolder -> ReactorUtils
                     // Convert the contents of rootFolder into a
                     // parallel flux stream.
                     .fromIterableParallel(rootFolder)

                     // Only search documents.
                     .filter(FolderTestsUtils::isDocument)

                     // Count the # of lines in the document in parallel
                     // with other documents in the dirent.
                     .flatMap(countLines)

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L)
                     
                     // Handle successful result.
                     .doOnSuccess(lineCount -> Options.getInstance()
                                  // Display the result.
                                  .display("total number of lines = "
                                           + lineCount)))
                     
            // Block until processing's done.
            .block();
    }

    /**
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} in parallel.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works
     * @param searchWord Word to search for in the folder
     */
    public static void searchFoldersParallel(Mono<Dirent> rootFolderM,
                                             String searchWord) {
        // This function counts # times searchWord appears in dirent.
        Function<Dirent, Mono<Long>> countMatches = document -> FolderTestsUtils
            // Count # of times searchWord appears in the document.
            .occurrencesCount(document, searchWord);

        rootFolderM
            // This code is called after rootFolder is initialized.
            .flatMap(rootFolder -> ReactorUtils
                     // Convert the contents of rootFolder into a
                     // parallel flux stream.
                     .fromIterableParallel(rootFolder)

                     // Only search documents.
                     .filter(FolderTestsUtils::isDocument)

                     // Search document looking for matches.
                     .flatMap(countMatches)

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L)

                     // Handle successful result.
                     .doOnSuccess(wordMatches -> Options.getInstance()
                                  // Display the result.
                                  .display("total matches of \""
                                           + searchWord
                                           + "\" = "
                                           + wordMatches)))

            // Block until processing's done.
            .block();
    }
}
