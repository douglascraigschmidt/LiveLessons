package folders.client;

import folders.common.FolderOps;
import folders.folder.Dirent;
import folders.server.FolderController;
import folders.tests.FolderTestsParallel;
import folders.common.Options;
import folders.utils.RunTimer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static folders.common.Constants.sWORKS;

/**
 * This client uses Spring WebFlux features to perform asynchronous
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
     * on the value of {@code concurrent}) and locally or remotely
     * (depending on the value of {@code remote}).
     *
     * @return A {@link Mono} that emits a {@link Dirent} folder
     */
    private Mono<Dirent> createFolder(boolean concurrent,
                                      boolean remote,
                                      String mode) {
        return remote
            // Return a Mono that emits a remote Folder.
            ? RunTimer
            // Compute the time needed to create a new remote Folder
            // asynchronously.
            .timeRun(() -> mFolderProxy
                     .createRemoteFolder(concurrent),
                     "createFolder() " + mode)
            : // Return a Mono that emits a local Folder.
            RunTimer
            // Compute the time needed to create a new local Folder
            // asynchronously.
            .timeRun(() -> FolderOps
                     .createFolder(sWORKS, concurrent),
                     "createFolder() " + mode);
    }

    /**
     * Run the tests either sequentially or concurrently (depending on
     * the value of {@code concurrent}) and locally or remotely
     * (depending on the value of {@code remote}).
     */
    public void runTests(boolean concurrent, boolean remote) {
        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";
        mode += remote ? " and remotely" : " and locally";

        Options.print("Starting the test " + mode);

        // Get a Mono to a Folder.
        Mono<Dirent> rootFolderM =
            createFolder(concurrent,
                         remote,
                         mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        var matches = RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders starting at the rootFolder.
            .timeRun(() -> FolderOps
                     .countWordMatches(rootFolderM,
                                       searchWord,
                                       concurrent)
                     .block(),
                     "searchFolders() " + mode);

        Options.debug(searchWord + " matched " + matches + " times");

        var entries = RunTimer
            // Compute the time taken to count the entries in the
            // folder.
            .timeRun(() -> FolderOps
                     .countEntries(rootFolderM, concurrent)
                     .block(),
                     "countEntries() " + mode);

        Options.debug("The number of entries = " + entries);

        var lines = RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> FolderOps
                     .countLines(rootFolderM, concurrent)
                     .block(),
                     "countLines() " + mode);

        Options.debug("The number of lines = " + lines);

        var documents = RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> FolderOps
                     .getDocuments(rootFolderM,
                                   "CompletableFuture",
                                   concurrent)
                     .collectList().block(),
                     "getDocuments() " + mode);

        Options.debug(searchWord + " was found in " + documents.size() + " documents");

        Options.print("Ending the test " + mode);
    }

    /**
     * Run the tests in parallel via Reactor's ParallelFlux mechanism.
     */
    public void runTestsParallel() {
        Options.print("Starting the test in parallel");

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a mono to a Folder.
        Mono<Dirent> rootFolderM = RunTimer
            // Compute the time needed to create a new folder
            // asynchronously.
            .timeRun(FolderTestsParallel::createFolderParallel,
                     "createFolderParallel() in parallel");

        RunTimer
            // Compute the time taken to synchronously search for
            // a word in all folders starting at the rootFolder.
            .timeRun(() -> FolderTestsParallel
                     .searchFoldersParallel(rootFolderM,
                                            searchWord)
                     .block(),
                     "searchFolders() in parallel");

        RunTimer
            // Compute the time taken to count the # of lines in
            // the folder.
            .timeRun(() -> FolderTestsParallel
                     .countLinesParallel(rootFolderM)
                     .block(),
                     "countLines() in parallel");

        Options.print("Ending the test in parallel");
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
                                 concurrent).block(),
                     "searchWord remote " + mode);

        Options.debug(searchWord + " matched " 
                      + matches + " times");

        var entries = RunTimer
            // Compute the time taken to count the entries in the
            // remote folder.
            .timeRun(() -> mFolderProxy
                     .countEntriesAsync(concurrent).join(),
                     "countEntries remote " + mode);

        System.out.println("The number of entries (as Mono) = " 
                           + entries);

        entries = RunTimer
            // Compute the time taken to count the entries in the
            // remote folder.
            .timeRun(() -> mFolderProxy
                     .countEntries(concurrent).block(),
                     "countEntries remote " + mode);

        System.out.println("The number of entries (as Mono) = " 
                           + entries);

        var lines = RunTimer
            // Compute the time taken to count the # of lines in the
            // local folder.
            .timeRun(() -> mFolderProxy
                     .countLines(concurrent),
                     "countLines() remote " + mode);

        Options.debug("The number of lines = " 
                      + lines.block());

        var documents = RunTimer
            // Compute the time taken to determine how many documents
            // the search word appeared in the remote folder.
            .timeRun(() -> mFolderProxy
                     .getDocuments(searchWord,
                                   concurrent),
                     "getDocuments remote " + mode);
        
        Options.debug(searchWord + " was found in " 
                      + documents.count() + " documents");

        Options.print("Ending the remote tests");
    }
}
