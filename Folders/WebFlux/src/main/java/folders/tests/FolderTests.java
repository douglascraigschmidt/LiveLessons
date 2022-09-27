package folders.tests;

import folders.folder.Dirent;
import folders.folder.Folder;
import folders.utils.Options;
import folders.utils.ReactorUtils;
import folders.utils.TestDataFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static folders.tests.FolderTestsUtils.sWORKS;

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
     * Asynchronously and locally create an in-memory folder
     * containing all the works.
     *
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Mono} that emits a folder containing all works
     *         in {@code works}
     */
    public static Mono<Dirent> createFolder(boolean concurrent) {
        return FolderTests
            // Return a mono to the initialized folder.
            .createFolder(sWORKS, concurrent);
    }

    /**
     * Asynchronously and locally create an in-memory folder
     * containing all the works.
     *
     * @param works The path to the directory containing the works.
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Mono} that emits a folder containing all works
     *         in {@code works}
     */
    public static Mono<Dirent> createFolder(String works,
                                            boolean concurrent) {
        // Return a Mono to the initialized folder.
        return Folder
                // Asynchronously create a folder with all works in
                // the root directory.
                .fromDirectory(TestDataFactory.getRootFolderFile(works),
                               concurrent)

                // Cache the results so that they won't be re-emitted
                // repeatedly each time.
                .cache();
    }

    /**
     * Count the number of entries in the folder emitted by {@code
     * rootFolderM}.
     *
     * @param rootFolderM A {@link Mono} to an in-memory folder
     *                    containing the works
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Mono} that emits a count of the number of
     *         entries in the folder emitted by {@code rootFolderM}
     */
    public static Mono<Long> countEntries(Mono<Dirent> rootFolderM,
                                          boolean concurrent) {
        // Return a count of the # of entries starting at rootDir.
        return rootFolderM
            // Count the # of entries in the folder.
            .flatMap(rootFolder -> ReactorUtils
                     // Create a stream of dirents that run either
                     // concurrent or sequentially.
                     .fromIterableConcurrentIf(rootFolder, concurrent)
										             
                     // Count the number of dirents in the stream.
                     .count())

            // Process results
            .doOnSuccess(entryCount -> Options
                         // Display the result.
                         .debug("number of entries in the folder = "
                                + entryCount)); 
    }

    /**
     * Count the number of entries in the folder emitted by {@code
     * rootFolderM}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Mono} that emits a count of the number of
     *         entries in the folder emitted by {@code rootFolderM}
     */
    public static Mono<Long> performCount(String rootDir,
                                          boolean concurrent) {
        Mono<Dirent> rootFolderM = FolderTests
            // Asynchronously and concurrently create a folder
            // starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderTests
            // Asynchronously return a count of the # of entries
            // starting at rootDir.
            .countEntries(rootFolderM,
                          concurrent);
    }

    /**
     * Find all occurrences of {@code word} in {@code rootFolderM}
     * return the number of matches.
     *
     * @param rootFolderM A {@link Mono} to an in-memory folder that
     *                    emits the contents
     * @param word Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                     concurrently or not
     * @return A {@link Mono} containing the number of times {@code
     *         word} appears the folder emitted by {@code rootFolderM}
     */
    public static Mono<Long> countWordMatches(Mono<Dirent> rootFolderM,
                                              String word,
                                              boolean concurrent) {
        // This function counts # of searchWord matches in a dirent.
        Function<Dirent, Flux<Long>> countMatches = dirent -> ReactorUtils
            // Emit concurrent or sequentially.
            .justConcurrentIf(dirent, concurrent)

            // Only search documents.
            .filter(FolderTestsUtils::isDocument)

            // Search document looking for matches.
            .flatMap(document -> FolderTestsUtils
                     // Count # of times word matches in document.
                     .occurrencesCount(document,
                                       word));

        return rootFolderM
            // This code is called after rootFolder initialization
            // complete to count all word matches in the folder.
            .flatMap(rootFolder -> Flux
                     // Create a stream of dirents from rootFolder.
                     .fromIterable(rootFolder)

                     // Use the Reactor flatMap() idiom to count the #
                     // of times word matches in the folder.
                     .flatMap(countMatches)

                     // Sum all the counts.
                     .reduce(Long::sum)

                     // Return 0 if empty.
                     .defaultIfEmpty(0L)

                     // Process results.
                     .doOnSuccess(wordMatches -> Options
                                  // Display the result.
                                  .debug("total matches of \""
                                         + word
                                         + "\" = "
                                         + wordMatches)));
    }

    /**
     * Find all occurrences of {@code word} in {@code rootDir} and
     * return the number of matches.
     *
     * @param rootDir The root directory to start the search
     * @param word Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                     concurrently or not
     * @return A {@link Mono} that emits the number of times {@code
     *         word} appears in folder starting at {@code rootDir}
     */
    public static Mono<Long> performCountWordMatches(String rootDir,
                                                     String word,
                                                     boolean concurrent) {
        Mono<Dirent> rootFolderM = FolderTests
            // Asynchronously and concurrently create a folder
            // starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderTests
            // Return a mono containing the number of times word
            // appears in the root folder.
            .countWordMatches(rootFolderM, word, concurrent);
    }

    /**
     * This method returns all the documents where a {@code
     * searchWord} appears in the folder emitted by {@code
     * rootFolderM}.
     *
     * @param rootFolderM A {@link Mono} to an in-memory folder
     *                    containing the works
     * @param searchWord Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Flux} that emits all the documents where
     *         {@code searchWord} appears in the folder emitted by
     *         {@code rootFolderM}
     */
    public static Flux<Dirent> getDocuments(Mono<Dirent> rootFolderM,
                                            String searchWord,
                                            boolean concurrent) {
        // This function returns a Flux stream of documents that
        // contain the search searchWord.
        Function<Dirent, Flux<Dirent>> getMatchingDocuments = dirent -> ReactorUtils
            // Emit concurrent or sequentially.
            .justConcurrentIf(dirent, concurrent)

            // Only consider documents.
            .filter(FolderTestsUtils::isDocument)

            // Only consider documents containing the search
            // searchWord (non-blocking!).
            .filterWhen(document -> FolderTestsUtils
                        .wordIsInDocument(document,
                                          searchWord));

        // Return a flux containing all documents where searchWord
        // appears in the folder starting at the root directory.
        return rootFolderM
            // This code is called after rootFolder initialization
            // complete to get the documents containing searchWord
            // matches starting in the folder.
            .map(rootFolder -> Flux
                 // Create a stream of dirents from rootFolder.
                 .fromIterable(rootFolder)

                 // Perform the flatMap() idiom to conditionally run
                 // this code sequentially or concurrently.
                 .flatMap(getMatchingDocuments))

            // Convert the Mono<Flux<Dirent>> to a Flux<Dirent>.
            .flatMapMany(Function.identity())

            // Print some debugging information.
            .doOnNext(document -> Options
                      // Display the result.
                      .debug("document "
                             + document.getPath().toString()
                             + " matches \""
                             + searchWord)); 
    }

    /**
     * This method returns all the documents where a {@code
     * searchWord} appears in the folder starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param searchWord Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Flux} that emits all the documents where
     *         {@code searchWord} appears in the folder starting at
     *         {@code rootDir}
     */
    public static Flux<Dirent> performGetDocuments(String rootDir,
                                                   String searchWord,
                                                   boolean concurrent) {
        Mono<Dirent> rootFolderM = FolderTests
                // Asynchronously and concurrently create a folder
                // starting at rootDir.
                .createFolder(rootDir, concurrent);

        return FolderTests
                // Return a flux containing all documents where the
                // search searchWord appears starting from the root folder.
                .getDocuments(rootFolderM, searchWord, concurrent);
    }

    /**
     * Count the number of lines in the recursively-structured folder
     * starting at {@code rootFolderM}.
     *
     * @param rootFolderM A {@link Mono} that emits an in-memory
     *                    folder containing the works
     * @param concurrent Flag indicating whether to run the tests
     *                   concurrently or not
     * @return A {@link Mono} that emits a count of the number of
     *         lines in the folder starting at {@code rootFolderM}
     */
    public static Mono<Long> countLines(Mono<Dirent> rootFolderM,
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

        // Function counts # of lines in all dirents in rootFolder.
        Function<Dirent, Mono<Long>> countAllDirentLines = rootFolder -> Flux
            // Create a stream of dirents from rootFolder.
            .fromIterable(rootFolder)

            // Use the Reactor flatMap() idiom to count the # of lines
            // in the folder either sequentially or concurrently.
            .flatMap(countOneDirentLines)

            // Sum all the counts.
            .reduce(Long::sum)

            // Return 0 if empty.
            .defaultIfEmpty(0L);

        return rootFolderM
            // This code is called after rootFolder initialization
            // completes to count all the lines in the folder.
            .flatMap(countAllDirentLines)

            // Process results.
            .doOnSuccess(lineCount -> Options
                         // Display the result.
                         .debug("total number of lines = "
                                + lineCount));
    }

    /**
     * Print the contents of the recursively-structured folder
     * starting at {@code rootFolderM}.
     *
     * @param rootFolderM A {@link Mono} that emits a
     *        recursively-structured folder
     */
    public static void print(Mono<Dirent> rootFolderM) {
        rootFolderM
            // Action operator.
            .doOnSuccess(rootFolder -> Flux
                         // Create a Flux from rootFolder.
                         .fromIterable(rootFolder)

                         // Print the results.
                         .doOnNext(item -> 
                                   System.out.println(item.getName()))

                         // Trigger the stream.
                         .subscribe())

            // Block until we're done.
            .block();
    }
}
