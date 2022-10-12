package folders.client;

import folders.datamodel.Dirent;
import folders.server.FolderApplication;
import folders.server.FolderController;
import folders.utils.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.stream.Stream;

import static folders.common.Constants.EndPoint.*;

/**
 * This class is a proxy to the {@link FolderApplication} service and
 * the {@link FolderController}.
 */
@SuppressWarnings("FieldCanBeLocal")
@Component
public class FolderProxy {
    /**
     * This auto-wired field connects the {@link
     * FolderProxy} to the {@link RestTemplate}
     * that performs HTTP requests synchronously.
     */
    @Autowired
    private RestTemplate mRestTemplate;

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
     * A URI to return all documents that include a word match in the root folder.
     */
    private final String mGetDocumentsURI =
        FOLDERS + ROOT_DIR + GET_DOCUMENTS;

    /**
     * Intentionally provide an invalid root folder to
     * trigger an {@link InvalidFolderException}.
     */
    public Dirent createRemoteFolderError() {
        var INVALID_ROOT_FOLDER = "/verks";

        return WebUtils
                // Create and send an HTTP GET request.
                .makeGetRequest(mRestTemplate,
                        // Encode the URL.
                        UriComponentsBuilder
                                .fromPath(FOLDERS + INVALID_ROOT_FOLDER + CREATE_FOLDER)
                                .queryParam("concurrent", false)
                                .build()
                                .toString(),
                        // Indicate the return type.
                        Dirent.class);
    }

    /**
     * Synchronously and remotely create an in-memory folder
     * containing all the works.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Dirent} that holds a folder containing all the
     *         works
     */
    public Dirent createRemoteFolder(boolean concurrent) {
        // Return a Dirent containing the remote folder.
        return WebUtils
            // Create and send an HTTP GET request.
            .makeGetRequest(mRestTemplate,
                            // Encode the URL.
                            UriComponentsBuilder
                            .fromPath(mCreateURI)
                            .queryParam("concurrent", concurrent)
                            .build()
                            .toString(),
                            // Indicate the return type.
                            Dirent.class);
    }

    /**
     * Synchronously and remotely count the number of entries in the
     * folder.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Long} that counts the number of entries in the
     *         remote folder
     */
    public Long countEntries(boolean concurrent) {
        // Return a count of # the entries in the remote folder.
        return WebUtils
            // Create and send an HTTP GET request.
            .makeGetRequest(mRestTemplate,
                            // Encode the URL.
                            UriComponentsBuilder
                            .fromPath(mCountEntriesURI)
                            .queryParam("concurrent", concurrent)
                            .build()
                            .toString(),
                            // Indicate the return type.
                            Long.class);
    }

    /**
     * Synchronously and remotely count the number lines in entries in
     * the remote folder.
     *
     * @param concurrent Flag indicating whether to run the test
     *                   concurrently or not
     * @return A {@link Long} that counts the number of lines in
     *         entries in the folder
     */
    public Long countLines(boolean concurrent) {
        // Return a count of the number of lines in the remote folder.
        return WebUtils
            // Create and send an HTTP GET request.
            .makeGetRequest(mRestTemplate,
                            // Encode the URL.
                            UriComponentsBuilder
                            .fromPath(mCountEntriesURI)
                            .queryParam("concurrent", concurrent)
                            .build()
                            .toString(),
                            // Indicate the return type.
                            Long.class);
    }

    /**
     * Synchronously and remotely count the number of times that
     * {@code word} appears in the folder.
     *
     * @param word The word to search for
     * @param concurrent True if the search should be done
     *                   concurrently else false
     * @return A {@link Long} that counts the number of times {@code}
     *         appears in the remote folder
     */
    public Long searchWord(String word,
                           boolean concurrent) {
        // Return a count of the number of times 'word' appears in the
        // remote folder.
        return WebUtils
            // Create and send an HTTP GET request.
            .makeGetRequest(mRestTemplate,
                            // Encode the URL.
                            UriComponentsBuilder
                            .fromPath(mSearchURI)
                            .queryParam("word", word)
                            .queryParam("concurrent", concurrent)
                            .build()
                            .toString(),
                            // Indicate the return type.
                            Long.class);
    }

    /**
     * Synchronously and remotely get a {@link Stream} of documents
     * that match the {@code word}
     *
     * @param word The word to search for
     * @param concurrent True if the search should run concurrently,
     *                   else false
     * @return A {@link List<Dirent>} containing documents that match
     *         the {@code word} in the remote folder.
     */
    public List<Dirent> getDocuments(String word,
                                     boolean concurrent) {
        // Return a List of documents that match 'word' in the remote
        // folder.
        return WebUtils
            // Create and send an HTTP GET request.
            .makeGetRequestList(mRestTemplate,
                                // Encode the URL.
                                UriComponentsBuilder
                                .fromPath(mGetDocumentsURI)
                                .queryParam("word", word)
                                .queryParam("concurrent", concurrent)
                                .build()
                                .toString(),
                                // Indicate the return type.
                                Dirent[].class);
    }
}
