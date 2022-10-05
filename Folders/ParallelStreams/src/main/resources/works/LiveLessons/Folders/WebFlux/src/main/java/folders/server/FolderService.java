package folders.server;

import folders.common.FolderOps;
import folders.folder.Dirent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * This class defines implementation methods that are called by the
 * {@link FolderController}. These implementation methods perform
 * operations on recursively-structured folders containing documents
 * and/or sub-folders.  Sequential or parallel Project Reactor flux
 * streams are used based on parameters passed by clients.
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class FolderService {
    /**
     * Memoized copy of the Folder.
     */
    private Mono<Dirent> mMemoizedDirent = null;

    /**
     * This method returns a {@link Mono} that emits all the entries
     * in the folder, starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the folder should be created
     *                   concurrently or not
     * @return A {@link Mono} that emits all the entries in the folder
     *         starting at {@code rootDir}
     */
    public Mono<Dirent> createFolder(String rootDir,
                                     Boolean concurrent) {
        return FolderOps
            // Asynchronously create and return a folder starting at
            // rootDir.
            .createFolder(rootDir, concurrent);
    }

    /**
     * This method returns a {@link Mono} that emits a count of the
     * number of times a {@code word} appears in the folder starting
     * at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for, starting at {@code rootDir}
     * @param concurrent True if the search should be done concurrently or not
     * @return A {@link Mono} that emits a count of the number of
     *         times {@code word} appears in the folder starting at
     *         {@code rootDir}
     */
    public Mono<Long> searchWord(String rootDir,
                                 String word,
                                 Boolean concurrent) {
        Mono<Dirent> rootFolder = FolderOps
            // Asynchronously create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Asynchronously count the # of times 'word' appears in
            // the folder starting at rootFolder.
            .countWordMatches(rootFolder, word, concurrent);
    }

    /**
     * This method returns a {@link Mono} that emits a count of the
     * number of entries in the folder starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently
     *                   or not
     * @return A {@link Mono} that emits a count of the number of
     *         entries in the folder starting at {@code rootDir}
     */
    public Mono<Long> countEntries(String rootDir,
                                   Boolean concurrent) {
        Mono<Dirent> rootFolder = FolderOps
            // Asynchronously create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Asynchronously count the # of entries in the folder
            // starting at rootFolder.
            .countEntries(rootFolder, concurrent);
    }

    /**
     * This method returns {@link Long} that counts the number of
     * lines in entries in the folder starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently
     *                   or not
     * @return A {@link Mono} that emits the number of lines in
     *         entries in the folder starting at {@code rootDir}
     */
    public Mono<Long> countLines(String rootDir,
                                 Boolean concurrent) {
        Mono<Dirent> rootFolder = FolderOps
            // Asynchronously create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Return the # of lines of entries starting at
            // rootFolder.
            .countLines(rootFolder,
                        concurrent);
    }

    /**
     * This method returns a {@link Flux} that emits all the documents
     * where a {@code word} appears in the folder, starting at {@code
     * rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for, starting at {@code rootDir}
     * @param concurrent True if the search should be done
     *                   concurrently or not
     * @return A {@link Flux} that emits all the documents where
     *         {@code word} appears in the folder starting at {@code
     *         rootDir}
     */
    public Flux<Dirent> getDocuments(String rootDir,
                                     String word,
                                     Boolean concurrent) {
        Mono<Dirent> rootFolder = FolderOps
            // Asynchronously create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Asynchronously count the # of times 'word' appears in
            // the folder starting at rootFolder.
            .getDocuments(rootFolder, word, concurrent);
    }
}
