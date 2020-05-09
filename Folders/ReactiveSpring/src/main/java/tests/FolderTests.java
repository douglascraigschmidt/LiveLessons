package tests;

import folder.Dirent;
import folder.Folder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import utils.Options;
import utils.ReactorUtils;
import utils.TestDataFactory;

import java.util.function.Function;

/**
 * This Java utility class contains Reactor-based methods that run all
 * the tests, either sequentially or concurrently, depending on the
 * value of the {@code concurrent} parameter.
 */
public final class FolderTests {
    /**
     * A Java utility class should have a private constructor.
     */
    private FolderTests() {}

    /**
     * Asynchronously create an in-memory folder containing all the
     * works.
     *
     * @param works Name of the directory in the file system containing the works.
     * @param concurrent Flag indicating whether to run the tests concurrent or not
     * @return A mono to a folder containing all works in {@code works}
     */
    public static Mono<Dirent> createFolder(String works,
                                            boolean concurrent) {
        // Return a mono to the initialized folder.
        return Folder
            // Asynchronously create a folder with all works in the
            // root directory.
            .fromDirectory(TestDataFactory.getRootFolderFile(works),
                           concurrent)

            // Cache the results so that they won't be re-emitted
            // repeatedly each time.
            .cache();
    }

    /**
     * Count the # of entries in the {@code rootFolder}.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     * @param concurrent Flag indicating whether to run the tests concurrent or not
     */
    public static void countEntries(Mono<Dirent> rootFolderM,
                                     boolean concurrent) {
        Mono<Long> countM = performCount(rootFolderM, concurrent);
		countM

            // Process results
            .doOnSuccess(entryCount -> Options.getInstance()
                         // Display the result.
                         .display("number of entries in the folder = "
                                  + entryCount))

            // Block until processing's done.
            .block();
    }

	public static Mono<Long> performCount(Mono<Dirent> rootFolderM, boolean concurrent) {
		Mono<Long> countM = rootFolderM
										    // This code is called after rootFolder is initialized and
										    // counts the # of entries in the folder.
										    .flatMap(rootFolder -> ReactorUtils
										             // Create a stream of dirents that run either concurrent
										             // or sequentially.
										             .fromIterableConcurrentIf(rootFolder, concurrent)
										             
										             // Count the number of dirents in the stream.
										             .count());
		return countM;
	}

    /**
     * Find all occurrences of {@code searchWord} in {@code
     * rootFolder} using a stream.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works
     * @param searchWord Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                     concurrent or not
     */
    public static void searchFolders(Mono<Dirent> rootFolderM,
                                     String searchWord,
                                     boolean concurrent) {
        Mono<Long> postSearch = performFolderSearch(rootFolderM, searchWord, concurrent);
		postSearch

                // Block until processing's done.
                .block();
    }

	public static Mono<Long> performFolderSearch(Mono<Dirent> rootFolderM, String searchWord, boolean concurrent) {
		// This function counts # of searchWord matches in a dirent.
        Function<Dirent, Flux<Long>> countMatches = dirent -> ReactorUtils
                // Emit concurrent or sequentially.
                .justConcurrentIf(dirent, concurrent)

                // Only search documents.
                .filter(FolderTestsUtils::isDocument)

                // Search document looking for matches.
                .flatMap(document -> FolderTestsUtils
                        // Count # of times searchWord matches in
                        // document.
                        .occurrencesCount(document,
                                          searchWord));

        Mono<Long> postSearch = rootFolderM
                // This code is called after rootFolder initialization
                // complete to count all searchWord matches in the folder.
                .flatMap(rootFolder -> Flux
                        // Create a stream of dirents from rootFolder.
                        .fromIterable(rootFolder)

                        // Use the Reactor flatMap() idiom to count the #
                        // of times searchWord matches in the folder.
                        .flatMap(countMatches)

                        // Sum all the counts.
                        .reduce(Long::sum)

                        // Return 0 if empty.
                        .defaultIfEmpty(0L)

                        // Process results.
                        .doOnSuccess(wordMatches -> Options.getInstance()
                                // Display the result.
                                .display("total matches of \""
                                        + searchWord
                                        + "\" = "
                                        + wordMatches)));
		return postSearch;
	}

    /**
     * Count # of lines in the recursively structured directory at
     * {@code rootFolder}.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     * @param concurrent Flag indicating whether to run the tests concurrent or not
     */
    public static void countLines(Mono<Dirent> rootFolderM,
                                  boolean concurrent) {
        // This function counts the # of lines in one dirent.
        Function<Dirent, Mono<Long>> countOneDirentLines = dirent -> ReactorUtils
            // Emit direct concurrent or sequentially.
            .justConcurrentIf(dirent, concurrent)

            // Only search documents.
            .filter(FolderTestsUtils::isDocument)

            // Process each document.
            .flatMap(document -> FolderTestsUtils
                     // Split the document into lines.
                     .splitAsFlux(document, "\\r?\\n"))

            // Count the number of lines in the document.
            .count();

        // This function counts # of lines in all dirents in the
        // rootFolder.
        Function<Dirent, Mono<Long>> countAllDirentLines = rootFolder -> Flux
            // Create a stream of dirents from rootFolder.
            .fromIterable(rootFolder)

            // Use the Reactor flatMap() idiom to count the #
            // of lines in the folder either sequentially or concurrently.
            .flatMap(countOneDirentLines)

            // Sum all the counts.
            .reduce(Long::sum)

            // Return 0 if empty.
            .defaultIfEmpty(0L);

        rootFolderM
            // This code is called after rootFolder initialization
            // completes to count all the lines in the folder.
            .flatMap(countAllDirentLines)

            // Process results.
            .doOnSuccess(lineCount -> Options.getInstance()
                         // Display the result.
                         .display("total number of lines = "
                                  + lineCount))

            // Block until processing's done.
            .block();
    }
}
