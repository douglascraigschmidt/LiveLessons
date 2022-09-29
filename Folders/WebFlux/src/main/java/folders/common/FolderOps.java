package folders.common;

import folders.folder.Dirent;
import folders.folder.Document;
import folders.folder.Folder;
import folders.utils.ReactorUtils;
import folders.utils.TestDataFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * This Java utility class contains methods used on both the
 * client and the server.
 */
public final class FolderOps {
    /**
     * A Java utility class should have a private constructor.
     */
    private FolderOps() {}

    /**
     * @return True if {@code dirent} is a document, else false
     */
    public static boolean isDocument(Dirent dirent) {
        // Return true if dirent is a document, else false.
        // return dirent instanceof Document;
        return dirent instanceof Document;
    }

    /**
     * Count the number of times {@code searchWord} appears in the
     * {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @return A {@link Mono} that emits the # of times {@code
     *         searchWord} appears in {@code document}
     */
    public static Mono<Long> occurrencesCount(Dirent document,
                                              String searchWord) {
        // Return a mono that counts the # of times searchWord appears
        // in the document.
        return 
            // Split the document into a stream of words.
            splitAsFlux(document,
                        "\\W+")

            // Only consider words that match.
            .filter(searchWord::equals)

            // Count # of times searchWord appears in the stream.
            .count();
    }

    /**
     * Check if the {@code searchWord} appears in the {@code document}.
     *
     * @param document In-memory document containing text
     * @param searchWord Word to search for in the document
     * @return A {@link Mono} that emits true if {@code searchWord}
     *         appears in {@code document}, else false
     */
    public static Mono<Boolean> wordIsInDocument(Dirent document,
                                                 String searchWord) {
        // Split the document into a stream of words.
        return splitAsFlux(document,
                          "\\W+")

            // Emit a single boolean true if any values of this Flux
            // match the predicate.
            .any(searchWord::equals);
    }

    /**
     * Creates a {@link Flux} from the {@code document} around matches
     * of this {@code regex} pattern.
     *
     * @param document The document to be split
     * @param regex The regular expression to compile
     * @return A {@link Flux} of {@link String} objects computed by
     *         splitting the {@code document} around matches of this
     *         {@code regex} pattern
     */
    public static Flux<String> splitAsFlux(Dirent document,
                                           String regex) {
        // Return a Flux of String objects.
        return Flux
            // Create an stream from an array of strings.
            .fromArray(document
                       // Create a string containing the document's
                       // contents.
                       .getContents().toString()

                       // Split the string into an array of strings
                       // based on the regular expression.
                       .split(regex));
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
        // Count the # of entries starting at rootFolderM.
        return rootFolderM
            // Count the # of entries in the folder.
            .flatMap(rootFolder -> ReactorUtils
                     // Create a stream of dirents that run either
                     // concurrent or sequentially.
                     .fromIterableConcurrentIf(rootFolder, concurrent)
										             
                     // Count the number of dirents in the stream.
                     .count());
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
     * @return A {@link Mono} that emits the number of times {@code
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
            .filter(FolderOps::isDocument)

            // Search document looking for matches.
            .flatMap(document -> FolderOps
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
                     .defaultIfEmpty(0L));
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
        Function<Dirent, Flux<Dirent>> getMatchingDocuments =
            dirent -> ReactorUtils
            // Emit concurrent or sequentially.
            .justConcurrentIf(dirent, concurrent)

            // Only consider documents.
            .filter(FolderOps::isDocument)

            // Only consider documents containing the search
            // searchWord (non-blocking!).
            .filterWhen(document -> FolderOps
                        .wordIsInDocument(document,
                                          searchWord));

        // Return a Flux containing all documents where searchWord
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
            .flatMapMany(Function.identity());
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
            .filter(FolderOps::isDocument)

            // Process each document.
            .flatMap(document -> FolderOps
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
            .flatMap(countAllDirentLines);
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
