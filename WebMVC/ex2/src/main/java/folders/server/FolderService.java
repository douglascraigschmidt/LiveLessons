package folders.server;

import folders.common.FolderOps;
import folders.folder.Dirent;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * This class defines implementation methods that are called by the
 * {@link FolderController}. These implementation methods perform
 * operations on recursively-structured folders containing documents
 * and/or sub-folders.  A sequential or parallel Java stream is used
 * based on parameters passed by clients.
 *
 * This class is annotated as a Spring {@code @Service}, which
 * indicates this class implements "business logic" and enables the
 * auto-detection and wiring of dependent implementation classes via
 * classpath scanning.
 */
@Service
public class FolderService {
    /**
     * This method returns a {@link Dirent} that contains all the
     * entries in the folder, starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the folder should be created
     *                   concurrently or not
     * @return A {@link Dirent} that contains all the entries in the
     *         folder starting at {@code rootDir}
     */
    public Dirent createFolder(String rootDir,
                               Boolean concurrent) {
        // Return a Dirent containing the initialized folder.
        return FolderOps
            // Create a folder with all works in the root
            // directory.
            .createFolder(rootDir,
                          concurrent);
    }

    /**
     * This method returns a {@link Long} that counts the number of
     * entries in the folder starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently
     *                   or not
     * @return A {@link Long} that counts the number of entries in the
     *         folder starting at {@code rootDir}
     */
    public Long countEntries(String rootDir,
                             Boolean concurrent) {
        Dirent rootFolder = FolderOps
            // Create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Return the # of entries starting at rootDir.
            .countEntries(rootFolder,
                          concurrent);
    }

    /**
     * This method returns {@link Long} that counts the number of
     * lines in entries in the folder starting at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param concurrent True if the count should be done concurrently
     *                   or not
     * @return A {@link Long} that counts the number of lines in
     *         entries in the folder starting at {@code rootDir}
     */
    public Long countLines(String rootDir,
                           Boolean concurrent) {
        Dirent rootFolder = FolderOps
            // Create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Return the # of lines of entries starting at rootDir.
            .countLines(rootFolder,
                        concurrent);
    }

    /**
     * This method returns a {@link Long} that emits a count of the
     * number of times a {@code word} appears in the folder starting
     * at {@code rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for, starting at {@code rootDir}
     * @param concurrent True if the search should be done concurrently or not
     * @return A {@link Long} that counts the number of times {@code
     *         word} appears in the folder starting at {@code rootDir}
     */
    public Long searchWord(String rootDir,
                           String word,
                           Boolean concurrent) {
        Dirent rootFolder = this
            // Synchronously create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Return the number of times word appears in the root
            // folder.
            .countWordMatches(rootFolder, word, concurrent);
    }

    /**
     * This method returns a {@link List} containing all the documents
     * where a {@code word} appears in the folder, starting at {@code
     * rootDir}.
     *
     * @param rootDir The root directory to start the search
     * @param word The word to search for, starting at {@code rootDir}
     * @param concurrent True if the search should be done
     *                   concurrently or not
     * @return A {@link List} containing all the documents where
     *         {@code word} appears in the folder starting at {@code
     *         rootDir}
     */
    public List<Dirent> getDocuments(String rootDir,
                                     String word,
                                     Boolean concurrent) {
        Dirent rootFolder = this
            // Synchronously create a folder starting at rootDir.
            .createFolder(rootDir, concurrent);

        return FolderOps
            // Return a List containing all documents where the search
            // word appears starting from the root folder.
            .getDocuments(rootFolder, word, concurrent);
    }

}
