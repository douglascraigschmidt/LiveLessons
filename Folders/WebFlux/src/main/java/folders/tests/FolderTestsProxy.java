package folders.tests;

import folders.folder.Dirent;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static folders.common.Constants.EndPoint.*;
import static folders.common.Constants.SERVER_BASE_URL;

/**
 * This class is a proxy to the FolderApplication micro-service.
 */
public final class FolderTestsProxy {
    /**
     * A Java utility class should have a private constructor.
     */
    private FolderTestsProxy() {}

    /**
     * A URI to the input "works" to process, which is a large
     * recursive folder containing thousands of subfolders and files.
     */
    private static final String sCreateURI =
        FOLDERS + ROOT_DIR + CREATE_FOLDER;

    /**
     * A URI to count the entries in the root folder.
     */
    private static final String sCountURI =
        FOLDERS + ROOT_DIR + COUNT_DOCUMENTS;

    /**
     * A URI to count the number of times a word appears in the root folder.
     */
    private static final String sSearchURI =
        FOLDERS + ROOT_DIR + SEARCH;

    /**
     * A URI to return all documents that include a word match in the root folder.
     */
    private static final String sGetDocumentsURI =
        FOLDERS + ROOT_DIR + GET_DOCUMENTS;

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
     * Create a webclient that acts as a proxy to the {@code Folder}
     * micro-service.
     */
    private static final WebClient sFolderProxy = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(SERVER_BASE_URL)

        // Increase the max buffer size.
        .exchangeStrategies(sExchangeStrategies)

        // Build the webclient.
        .build();

    /**
     * Asynchronously and remotely create an in-memory folder
     * containing all the works.
     *
     * @param memoize Flag indicating whether to have the server
     *                memoize the result or not
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Mono} that emits a folder containing all the
     *         works
     */
    public static Mono<Dirent> createRemoteFolder(boolean memoize,
                                                  boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return sFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(sCreateURI)
                 .queryParam("memoize", memoize)
                 .queryParam("concurrent", concurrent)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a mono to a dirent.
            .bodyToMono(Dirent.class);
    }

    /**
     * Asynchronously and remotely count the number of entries in the folder.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A completable future to a count of the number of
     *         entries in the folder
     */
    public static CompletableFuture<Long> countEntriesAsync(boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return sFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(sCountURI)
                 .queryParam("concurrent", concurrent)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a mono to a long.
            .bodyToMono(Long.class)

            // Convert to a completable future.
            .toFuture();
    }

    /**
     * Asynchronously and remotely count the number of entries in the folder.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Mono} that emits a count of the number of
     *         entries in the folder
     */
    public static Mono<Long> countEntries(boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return sFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(sCountURI)
                 .queryParam("concurrent", concurrent)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a mono to a long object.
            .bodyToMono(Long.class);
    }

    /**
     * Asynchronously and remotely count the number of times that
     * {@code word} appears in the folder.
     *
     * @param word The word to search for
     * @param concurrent True if the search should be done
     *                   concurrently else false
     * @return A {@link Mono} that emits a count of the number of
     *         entries in the folder
     */
    public static Mono<Long> searchWord(String word,
                                        boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return sFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(sSearchURI)
                 .queryParam("word", word)
                 .queryParam("concurrent", concurrent)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a mono to a long.
            .bodyToMono(Long.class);
    }

    /**
     * Returns a {@link Stream} of documents that match the {@code word}
     *
     * @param word The word to search for
     * @param concurrent True if the search should run concurrently, else false
     * @return A {@link Stream} containing documents that match the {@code word}
     */
    public static Stream<Dirent> getDocumentsWithWordMatches(String word,
                                                             boolean concurrent) {
        // Return a Stream to the folder initialized remotely.
        return sFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(sGetDocumentsURI)
                 .queryParam("word", word)
                 .queryParam("concurrent", concurrent)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a flux of dirents.
            .bodyToFlux(Dirent.class)
            
            // Convert to a stream.
            .toStream();
    }
}
