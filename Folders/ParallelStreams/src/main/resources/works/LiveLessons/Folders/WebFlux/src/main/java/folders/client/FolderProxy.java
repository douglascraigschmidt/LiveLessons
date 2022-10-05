package folders.client;

import folders.folder.Dirent;
import folders.server.FolderApplication;
import folders.server.FolderController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import static folders.common.Constants.EndPoint.*;
import static folders.common.Constants.SERVER_BASE_URL;

/**
 * This class is a proxy to the {@link FolderApplication} service and
 * the {@link FolderController}.
 */
@SuppressWarnings("FieldCanBeLocal")
@Component
public class FolderProxy {
    /**
     * This auto-wired field connects the {@link FolderProxy} to the
     * {@link WebClient} that performs HTTP requests asynchronously.
     */
    @Autowired
    private WebClient mFolderProxy;

    /**
     * A URI to the input "works" to process, which is a large
     * recursive folder containing thousands of subfolders and files.
     */
    private final String mCreateURI =
        FOLDERS + ROOT_DIR + CREATE_FOLDER;

    /**
     * A URI to count the entries in the root folder.
     */
    private final String mCountEntriesURI =
        FOLDERS + ROOT_DIR + COUNT_DOCUMENTS;

    /**
     * A URI to count the lines in entries in the root folder.
     */
    private final String mCountLinesURI =
        FOLDERS + ROOT_DIR + COUNT_LINES;

    /**
     * A URI to count the number of times a word appears in the root folder.
     */
    private final String mSearchURI =
        FOLDERS + ROOT_DIR + SEARCH;

    /**
     * A URI to return all documents that include a word match in the
     * root folder.
     */
    private final String mGetDocumentsURI =
        FOLDERS + ROOT_DIR + GET_DOCUMENTS;

    /**
     * Asynchronously and remotely create an in-memory folder
     * containing all the works.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Mono} that emits a folder containing all the
     *         works
     */
    public Mono<Dirent> createRemoteFolder(boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return mFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mCreateURI)
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
     * @return A {@link CompletableFuture} to a count of the number of
     *         entries in the folder
     */
    public CompletableFuture<Long> countEntriesAsync(boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return mFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mCountEntriesURI)
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
    public Mono<Long> countEntries(boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return mFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mCountEntriesURI)
                 .queryParam("concurrent", concurrent)
                 .build()
                 .toString())

            // Retrieve the response.
            .retrieve()

            // Convert it to a mono to a long object.
            .bodyToMono(Long.class);
    }

    /**
     * Asynchronously and remotely count the number of entries in the folder.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Mono} that emits a count of the number of
     *         entries in the folder
     */
    public Mono<Long> countLines(boolean concurrent) {
        // Return a mono 
        return mFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mCountLinesURI)
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
    public  Mono<Long> searchWord(String word,
                                  boolean concurrent) {
        // Return a mono to the folder initialized remotely.
        return mFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mSearchURI)
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
    public Stream<Dirent> getDocuments(String word,
                                       boolean concurrent) {
        // Return a Stream to the folder initialized remotely.
        return mFolderProxy
            // Create an HTTP GET request.
            .get()

            // Add the uri to the baseUrl.
            .uri(UriComponentsBuilder
                 .fromPath(mGetDocumentsURI)
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
