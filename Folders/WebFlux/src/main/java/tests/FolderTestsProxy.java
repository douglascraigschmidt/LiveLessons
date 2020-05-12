package tests;

import folder.Dirent;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

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
    private static final String sCreateURI = "/folders/works/_create";

    /**
     * A URI to count the entries in the root folder.
     */
    private static final String sCountURI = "/folders/works/_count";

    /**
     * A URI to count the number of times a word appears in the root folder.
     */
    private static final String sSearchURI = "/folders/works/_search";

    /**
     * Host/post where the server resides.
     */
    private static final String sSERVER_BASE_URL =
        "http://localhost:8080";

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
     * Create a webclient that acts as a proxy to the folder micro-service.
     */
    private static final WebClient sFolderProxy = WebClient
        // Start building.
        .builder()

        // The URL where the server is running.
        .baseUrl(sSERVER_BASE_URL)

        // Increase the max buffer size.
        .exchangeStrategies(sExchangeStrategies)

        // Build the webclient.
        .build();

    /**
     * Asynchronously and remotely create an in-memory folder
     * containing all the works.
     *
     * @param memoize Flag indicating whether to have the server memoize the result or not
     * @param concurrent Flag indicating whether to run the test concurrently or not
     * @return A mono to a folder containing all works in {@code works}
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

            // Convert it to a Folder object.
            .bodyToMono(Dirent.class);
    }

    /**
     * Asynchronously and remotely count the number of entries in the folder.
     *
     * @param concurrent Flag indicating whether to run the test concurrently or not
     * @return A completable future to a count of the number of entries in the folder
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

            // Convert it to a Folder object.
            .bodyToMono(Long.class)

            // Convert to a completable future.
            .toFuture();
    }

    /**
     * Asynchronously and remotely count the number of entries in the folder.
     *
     * @param concurrent Flag indicating whether to run the test concurrently or not
     * @return A mono to a count of the number of entries in the folder
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

            // Convert it to a Folder object.
            .bodyToMono(Long.class);
    }

    /**
     * Asynchronously and remotely count the number of times that
     * {@code word} appears in the folder.
     *
     * @param word The word to search for.
     * @param concurrent True if the search should be done concurrently else false
     * @return A mono to a count of the number of entries in the folder
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

            // Convert it to a Folder object.
            .bodyToMono(Long.class);
    }
}
