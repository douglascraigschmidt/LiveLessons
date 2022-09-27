package folders.client;

import folders.common.FolderOps;
import folders.folder.Dirent;
import folders.folder.Document;
import folders.server.FolderController;
import folders.utils.Options;
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
    @Autowired
    private FolderProxy mFolderTestsProxy;

    /**
     * Create a folder either sequentially or concurrently (depending
     * on the value of {@code concurrent}).
     *
     * @return Return A {@link Dirent} that contains a folder
     */
    private Dirent createFolder(boolean concurrent,
                                String mode) {
            // Return a Dirent to a remote folder.
            return RunTimer
                // Compute the time needed to create a new remote
                // folder synchronously.
                .timeRun(() -> mFolderTestsProxy
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

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a folder from the server.
        Dirent rootFolder = createFolder(concurrent,
                                         mode);

        var matches = RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders starting at the rootFolder.
            .timeRun(() -> FolderOps
                     .countWordMatches(rootFolder,
                                       searchWord,
                                       concurrent),
                     "searchFolders() local " + mode);

        Options.debug(searchWord + " matched " + matches + " times");

        var entries = RunTimer
            // Compute the time taken to count the entries in the
            // folder.
            .timeRun(() -> FolderOps
                     .countEntries(rootFolder, concurrent),
                     "countEntries() local " + mode);

        Options.debug("The number of entries = " + entries);

        var lines = RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> FolderOps
                     .countLines(rootFolder, concurrent),
                     "countLines() local " + mode);

        Options.debug("The number of lines = " + lines);
        
        var documents = RunTimer
            // Compute the time taken to determine how many documents
            // the search word appeared in.
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
        Options.print("Starting the remote tests");

        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";

        var count = RunTimer
            .timeRun(() -> mFolderTestsProxy
                     .countEntries(concurrent),
                     "countEntries remote " + mode);

        System.out.println("Count of dirent entries = "
                           + count);

        var search = RunTimer
            .timeRun(() -> mFolderTestsProxy
                     .searchWord("CompletableFuture",
                                 concurrent),
                     "searchWord remote " + mode);

        System.out.println("Count # of times \"CompletableFuture\" appears = "
                           + search);

        var results = RunTimer
            .timeRun(() -> mFolderTestsProxy
                     .getDocuments("CompletableFuture",
                                   concurrent),
                     "getDocuments remote " + mode);
        
        System.out.println("Count # of documents \"CompletableFuture\" appears = "
                           // Count the # of documents that match.
                           + results.size());

        Options.print("Ending the remote tests");
    }
}
