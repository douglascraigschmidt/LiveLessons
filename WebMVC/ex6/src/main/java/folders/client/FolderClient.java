package folders.client;

import folders.common.FolderOps;
import folders.datamodel.Dirent;
import folders.server.FolderController;
import folders.common.Options;
import folders.utils.RunTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This client uses Spring WebMVC features to perform synchronous
 * remote method invocations on the {@link FolderController} web
 * service to process entries in a recursively-structured directory
 * folder sequentially, concurrently, and in parallel in a
 * client/server environment.
 *
 * The {@code @Component} annotation allows Spring to automatically
 * detect custom beans, i.e., Spring will scan the application for
 * classes annotated with {@code @Component}, instantiate them, and
 * inject the specified dependencies into them without having to write
 * any explicit code.
 */
@Component
public class FolderClient {
    /**
     * This object connects {@link FolderClient} to the {@code
     * FolderProxy}.  The {@code @Autowired} annotation ensures this
     * field is initialized via Spring dependency injection, where an
     * object receives another object it depends on (e.g., by creating
     * a {@link FolderProxy}).
     */
    @Autowired
    private FolderProxy mFolderProxy;

    /**
     * Create a folder either sequentially or concurrently (depending
     * on the value of {@code concurrent}).
     *
     * @param concurrent True if call should run concurrently, false
     *                   if it should run sequentially
     * @param mode A string that's either "concurrent" or "sequential"
     *             depending on the value of {@code concurrent}
     *
     * @return A {@link Dirent} that contains an initialized folder
     *         from the remote server
     */
    private Dirent createFolder(boolean concurrent,
                                String mode) {
        // Return a Dirent to a remote folder.
        return RunTimer
            // Compute the time needed to create a new remote
            // folder synchronously.
            .timeRun(() -> mFolderProxy
                     .createRemoteFolder(concurrent),
                     "createFolder() remote " + mode);
    }

    /**
     * Run the tests either sequentially or concurrently (depending on
     * the value of {@code concurrent}).
     */
    public void runTests(boolean concurrent) {
        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";

        Options.print("Starting the mostly local tests " + mode);

        // Get a folder from the remote server.
        Dirent rootFolder = createFolder(concurrent,
                                         mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        var matches = RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders in the local rootFolder.
            .timeRun(() -> FolderOps
                     .countWordMatches(rootFolder,
                                       searchWord,
                                       concurrent),
                     "searchFolders() local " + mode);

        Options.debug(searchWord + " matched " + matches + " times");

        var entries = RunTimer
            // Compute the time taken to count the entries in the
            // local folder.
            .timeRun(() -> FolderOps
                     .countEntries(rootFolder, concurrent),
                     "countEntries() local " + mode);

        Options.debug("The number of entries = " + entries);

        var lines = RunTimer
            // Compute the time taken to count the # of lines in the
            // local folder.
            .timeRun(() -> FolderOps
                     .countLines(rootFolder, concurrent),
                     "countLines() local " + mode);

        Options.debug("The number of lines = " + lines);
        
        var documents = RunTimer
            // Compute the time taken to determine how many documents
            // the search word appeared in the local folder.
            .timeRun(() -> FolderOps
                     .getDocuments(rootFolder,
                                   "CompletableFuture",
                                   concurrent),
                     "getDocuments() local " + mode);

        Options.debug(searchWord + " was found in " + documents.size() + " documents");

        Options.print("Ending the mostly local tests " + mode);
    }

    /**
     * Run the remote tests either sequentially or concurrently
     * (depending on the value of {@code concurrent}).
     */
    public void runRemoteTests(boolean concurrent) {
        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";

        Options.print("Starting the remote tests " + mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        var matches = RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders in the remote rootFolder.
            .timeRun(() -> mFolderProxy
                     .searchWord(searchWord,
                                 concurrent),
                     "searchWord remote " + mode);

        Options.debug(searchWord + " matched " + matches + " times");

        var entries = RunTimer
            // Compute the time taken to count the entries in the
            // remote folder.
            .timeRun(() -> mFolderProxy
                     .countEntries(concurrent),
                     "countEntries remote " + mode);

        System.out.println("The number of entries = " + entries);

        var lines = RunTimer
            // Compute the time taken to count the # of lines in the
            // local folder.
            .timeRun(() -> mFolderProxy
                     .countLines(concurrent),
                     "countLines() remote " + mode);

        Options.debug("The number of lines = " + lines);

        var documents = RunTimer
            // Compute the time taken to determine how many documents
            // the search word appeared in the remote folder.
            .timeRun(() -> mFolderProxy
                     .getDocuments(searchWord,
                                   concurrent),
                     "getDocuments remote " + mode);
        
        Options.debug(searchWord + " was found in " + documents.size() + " documents");

        Options.print("Ending the remote tests");
    }

    /**
     * Run tests that evoke exceptions from the server.
     */
    public void runExceptionTests() {
        try {
            // Get a folder from the remote server.
            Dirent rootFolder = mFolderProxy
                .createRemoteFolderError();
        } catch (Exception exception) {
            System.out.println("Exception = "
                                + exception.getMessage());
        }
    }
}
