import folder.Dirent;
import reactor.core.publisher.Mono;
import tests.FolderTests;
import tests.FolderTestsParallel;
import tests.FolderTestsProxy;
import utils.Options;
import utils.RunTimer;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * This example shows the use of a Spring WebFlux micro-service to
 * apply reactive streams top-to-bottom and end-to-end to process
 * entries in a recursively-structured directory folder sequentially,
 * concurrently, and in parallel in a distributed environment.  This example
 * encode/decode complex objects that use inheritance relationships and
 * transmit them between processes.
 */
public class Main {
    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        System.out.println("Starting ReactorFolders test");

        // Parse the options.
        Options.getInstance().parseArgs(argv);

        if (Options.getInstance().sequential()
            && Options.getInstance().local())
            // Run the tests sequentially and locally.
            runTests(false, false);

        if (Options.getInstance().sequential()
            && Options.getInstance().remote())
            // Run the tests sequentially and remotely.
            runTests(false, true);

        if (Options.getInstance().concurrent()
            && Options.getInstance().local())
            // Run the tests concurrently and locally.
            runTests(true, false);

        if (Options.getInstance().concurrent()
            && Options.getInstance().remote())
            // Run the tests concurrently and remotely.
            runTests(true, true);

        if (Options.getInstance().parallel())
            // Run the tests in parallel.
            runTestsParallel();
        
        if (Options.getInstance().remote())
            runRemoteTests();

        // Print results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending ReactorFolders test");
    }

    /**
     * Run the tests either sequentially or concurrently (depending on
     * the value of {@code concurrent}) and locally or remotely
     * (depending on the value of {@code remote}).
     */
    private static void runTests(boolean concurrent, boolean remote) {
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
     * Create a folder either sequentially or concurrently (depending on
     * the value of {@code concurrent}) and locally or remotely
     * (depending on the value of {@code remote}).
     * @return Return a mono to a folder.
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
     * Run the tests in parallel via Reactor's ParallelFlux mechanism.
     */
    private static void runTestsParallel() {
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
    private static void runRemoteTests() {
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
