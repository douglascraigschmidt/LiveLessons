package folders.client;

import folders.folder.Dirent;
import folders.tests.FolderTests;
import folders.tests.FolderTestsParallel;
import folders.tests.FolderTestsProxy;
import folders.utils.Options;
import folders.utils.RunTimer;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class FolderClient {
    /**
     * Create a folder either sequentially or concurrently (depending
     * on the value of {@code concurrent}) and locally or remotely
     * (depending on the value of {@code remote}).
     *
     * @return Return A {@link Mono} that emits a folder
     */
    private static Mono<Dirent> createFolder(boolean concurrent,
                                             boolean remote,
                                             String mode) {
        if (remote)
            // Return a mono to a remote Folder.
            return RunTimer
                // Compute the time needed to create a new remote
                // folder asynchronously.
                .timeRun(() -> FolderTestsProxy
                         .createRemoteFolder(Options.getInstance().memoize(),
                                             concurrent),
                         "createFolder() " + mode);
        else
            // Return a mono to a local Folder.
            return RunTimer
                    // Compute the time needed to create a new local
                    // folder asynchronously.
                .timeRun(() -> FolderTests
                         .createFolder(concurrent),
                         "createFolder() " + mode);
    }

    /**
     * Run the tests either sequentially or concurrently (depending on
     * the value of {@code concurrent}) and locally or remotely
     * (depending on the value of {@code remote}).
     */
    public static void runTests(boolean concurrent, boolean remote) {
        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";
        mode += remote ? " and remotely" : " and locally";

        Options.print("Starting the test " + mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a mono to a Folder.
        Mono<Dirent> rootFolderM =
            createFolder(concurrent,
                         remote,
                         mode);

        RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders starting at the rootFolder.
            .timeRun(() -> FolderTests
                     .countWordMatches(rootFolderM,
                                       searchWord,
                                       concurrent)
                     .block(),
                     "searchFolders() " + mode);

        RunTimer
            // Compute the time taken to count the entries in the
            // folder.
            .timeRun(() -> FolderTests
                     .countEntries(rootFolderM, concurrent)
                     .block(),
                     "countEntries() " + mode);

        RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> FolderTests
                     .countLines(rootFolderM, concurrent)
                     .block(),
                     "countLines() " + mode);

        RunTimer
            // Compute the time taken to count the # of lines in the
            // folder.
            .timeRun(() -> FolderTests
                     .getDocuments(rootFolderM,
                                   "CompletableFuture",
                                   concurrent)
                     .collectList().block(),
                     "getDocuments() " + mode);

        Options.print("Ending the test " + mode);
    }

    /**
     * Run the tests in parallel via Reactor's ParallelFlux mechanism.
     */
    public static void runTestsParallel() {
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
     * Run the remote tests.
     */
    public static void runRemoteTests() {
        Options.print("Starting the remote tests");

        CompletableFuture<Long> countF = FolderTestsProxy
            .countEntriesAsync(Options.getInstance().concurrent());

        System.out.println("Count of dirent entries (as CompletableFuture) = "
                           + countF.join());

        Mono<Long> countM = FolderTestsProxy
            .countEntries(Options.getInstance().concurrent());

        System.out.println("Count of dirent entries (as Mono) = "
                           + countM.block());

        Mono<Long> searchM = FolderTestsProxy
            .searchWord("CompletableFuture",
                        Options.getInstance().concurrent());

        System.out.println("Count # of times \"CompletableFuture\" appears (as Mono) = "
                           + searchM.block());

        Stream<Dirent> results = FolderTestsProxy
            .getDocumentsWithWordMatches("CompletableFuture",
                                         Options.getInstance().concurrent());

        System.out.println("Count # of documents \"CompletableFuture\" appears (as stream) = "
                           // Count the # of documents that match.
                           + results.count());

        Options.print("Ending the remote tests");
    }
}
