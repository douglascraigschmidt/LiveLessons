package tests;

import folder.Dirent;
import folder.Folder;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;
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
     * Increase the max size for the buffer transfers!
     */
    private static final ExchangeStrategies sExchangeStrategies = ExchangeStrategies
        .builder()
        // Increase the memory size.
        .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(10 * 1024 * 1024))
        // Build the strategy.
        .build();

    /**
     * Asynchronously and locally create an in-memory folder
     * containing all the works.
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
     * Asynchronously and remotely create an in-memory folder
     * containing all the works.
     *
     * @param hostname Name of the host containing the works.
     * @param uri Name of the uri in the file system on the host containing the works
     * @param memoize Flag indicating whether to have the server memoize the result or not
     * @param concurrent Flag indicating whether to run the test concurrently or not
     * @return A mono to a folder containing all works in {@code works}
     */
    public static Mono<Dirent> createRemoteFolder(String hostname,
                                                  String uri,
                                                  boolean memoize,
                                                  boolean concurrent) {
        // Create a webclient.
        WebClient webClient = WebClient
            .builder()
            // The URL where the server is running.
            .baseUrl(hostname)

            // Increase the max buffer size.
            .exchangeStrategies(sExchangeStrategies)
            .build();

        // Return a mono to the folder initialized remotely.
        return webClient
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(uri)

            // Retrieve the response.
            .retrieve()

            // Convert it to a Folder object.
            .bodyToMono(Dirent.class);
     }

    /**
     * Count the number of entries in the folder starting at {@code
     * rootFolderM}.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     * @param concurrent Flag indicating whether to run the tests concurrent or not
     * @return Returns a count of the number of entries in the folder
     * starting at {@code rootFolderM}
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
     * Count the number of entries in the folder starting at {@code
     * rootFolderM}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent Flag indicating whether to run the tests concurrent or not
     * @return Returns a count of the number of entries in the folder
     * starting at {@code rootFolderM}
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
     * using a flux stream.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works
     * @param word Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                     concurrent or not
     * @return A mono containing the number of times {@code word} appears
     *         in the root folder
     */
    public static Mono<Long> searchFolders(Mono<Dirent> rootFolderM,
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
     * Find all occurrences of {@code word} in {@code rootFolder}
     * using a stream.
     *
     * @param rootDir The root directory to start the search
     * @param word Word to search for in the folder
     * @param concurrent Flag indicating whether to run the tests
     *                     concurrent or not
     * @return A mono containing the number of times {@code word}
     *         appears in the root folder
     */
    public static Mono<Long> performFolderSearch(String rootDir,
                                                 String word,
                                                 boolean concurrent) {
        Mono<Dirent> rootFolderM = FolderTests
            // Asynchronously and concurrently create a folder
            // starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderTests
            // Return a mono containing the number of times word
            // appears in the root folder.
            .searchFolders(rootFolderM, word, concurrent);
    }

    /**
     * Count # of lines in the recursively-structured folder starting
     * at {@code rootFolderM}.
     *
     * @param rootFolderM A mono to an in-memory folder containing the works.
     * @param concurrent Flag indicating whether to run the tests concurrent or not
     * @return Returns a count of the number of lines in the folder
     * starting at {@code rootFolderM}
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

    public static void print(Mono<Dirent> rootFolderM) {
        rootFolderM
            .doOnSuccess(rootFolder -> {
                System.out.println("Starting print with rootFolder = " + rootFolder.getSize());

                Flux
                        .fromIterable(rootFolder)
                        .doOnNext(item -> System.out.println(item.getName()))
                        .subscribe();
            })
            .block();
    }
}
