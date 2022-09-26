package server;

import folder.Dirent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tests.FolderTests;

@Service
public class FolderService {
    /**
     * Memoized copy of the Folder.
     */
    private Mono<Dirent> mMemoizedDirent = null;

    /**
     * This method returns a count of the number of times a {@code
     * word} appears in the folder starting at {@code rootDir}.
     * <p>
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_search
     * endpoint to this method.
     *
     * @param rootDir    The root directory to start the search
     * @param word       The word to search for starting at {@code rootDir}
     * @param concurrent True if the search should be done concurrently or not
     * @return A count of the number of times {@code word} appears in
     * the folder starting at {@code rootDir}
     */
    public Mono<Long> searchWord(String rootDir,
                                 String word,
                                 Boolean concurrent) {
        return FolderTests
                // Asynchronously and concurrently count the # of
                // times word appears in folder starting at rootDir.
                .performCountWordMatches(rootDir, word, concurrent);
    }

    /**
     * This method returns all the documents where a {@code word}
     * appears in the folder starting at {@code rootDir}.
     * <p>
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_search
     * endpoint to this method.
     *
     * @param rootDir    The root directory to start the search
     * @param word       The word to search for starting at {@code rootDir}
     * @param concurrent True if the search should be done concurrently or not
     * @return A flux containing all the documents where {@code word} appears in
     * the folder starting at {@code rootDir}
     */
    public Flux<Dirent> getDocuments(String rootDir,
                                     String word,
                                     Boolean concurrent) {
        return FolderTests
                // Asynchronously and concurrently count the # of
                // times word appears in folder starting at rootDir.
                .performGetDocuments(rootDir, word, concurrent);
    }

    /**
     * This method returns a count of the number of entries in the
     * folder starting at {@code rootDir}.
     * <p>
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_count
     * endpoint to this method.
     *
     * @param rootDir    The root directory to start the search
     * @param concurrent True if the count should be done concurrently or not
     * @return A count of the number of entries in the folder starting
     * at {@code rootDir}
     */
    public Mono<Long> countEntries(String rootDir,
                                   Boolean concurrent) {
        return FolderTests
                // Asynchronously and concurrently count the # of entries
                // in the folder starting at rootDir.
                .performCount(rootDir, concurrent);
    }

    /**
     * This method returns all the entries in the folder starting at
     * {@code rootDir}.
     * <p>
     * WebFlux maps HTTP GET requests sent to the /{rootDir}/_create
     * endpoint to this method.
     *
     * @param rootDir    The root directory to start the search
     * @param memoize    True if the created folder should be cached
     * @param concurrent True if the folder should be created concurrently or not
     * @return Returns all the entries in the folder starting
     * at {@code rootDir}
     */
    public Mono<Dirent> createFolder(String rootDir,
                                     Boolean memoize,
                                     Boolean concurrent) {
        if (memoize) {
            if (mMemoizedDirent != null)
                // Return the cached folder contents.
                return mMemoizedDirent;
            else {
                mMemoizedDirent = FolderTests
                        // Asynchronously and concurrently create and
                        // return a folder starting at rootDir.
                        .createFolder(rootDir, concurrent);
            }
            return mMemoizedDirent;
        } else
            return FolderTests
                    // Asynchronously and concurrently create and return a
                    // folder starting at rootDir.
                    .createFolder(rootDir, true);
    }
}
