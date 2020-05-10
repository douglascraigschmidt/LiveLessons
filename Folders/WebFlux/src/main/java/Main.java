import folder.Dirent;
import folder.Document;
import folder.Folder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tests.FolderTests;
import tests.FolderTestsParallel;
import utils.Options;
import utils.ReactorUtils;
import utils.RunTimer;
import utils.TestDataFactory;

/**
 * This example shows the use of the Reactor framework to process
 * entries in a recursively-structured directory folder sequentially,
 * concurrently, and in parallel.
 */
public class Main {
    /**
     * The input "works" to process, which is a large recursive folder
     * containing thousands of subfolders and files.
     */
    private static final String sWORKS = "works";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) throws InterruptedException {
        System.out.println("Starting ReactorFolders test");

        // Parse the options.
        Options.getInstance().parseArgs(argv);

        // Run the tests sequentially.
        runTests(false);

        // Run the tests concurrently.
        // runTests(true);

        // Run the tests in parallel.
        // runTestsParallel();

        // Print results sorted by decreasing order of efficiency.
        System.out.println(RunTimer.getTimingResults());

        System.out.println("Ending ReactorFolders test");
    }

    /**
     * Run all the tests, either sequentially or concurrently,
     * depending on the value of {@code concurrent}.
     */
    private static void runTests(boolean concurrent) {
        // Record whether we're running concurrently or sequentially.
        String mode = concurrent ? "concurrently" : "sequentially";

        Options.display("Starting the test " + mode);

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a mono to a Folder.
        Mono<Folder> rootFolderM = RunTimer
            // Compute the time needed to create a new folder
            // asynchronously.
            .timeRun(() -> FolderTests
                     // .createFolder(sWORKS, concurrent),
                     .createRemoteFolder("/folders/works/"),
                     "createFolder() " + mode);

        RunTimer
            // Compute the time taken to synchronously search for a
            // word in all folders starting at the rootFolder.
            .timeRun(() -> FolderTests
                     .searchFolders(rootFolderM,
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

        Options.display("Ending the test " + mode);
    }

    /**
     * Run the tests in parallel via Reactor's ParallelFlux mechanism.
     */
    private static void runTestsParallel() {
        Options.display("Starting the test in parallel");

        // The word to search for while the folder's being
        // constructed.
        final String searchWord = "CompletableFuture";

        // Get a mono to a Folder.
        Mono<Dirent> rootFolderM = RunTimer
            // Compute the time needed to create a new folder
            // asynchronously.
            .timeRun(() -> FolderTestsParallel
                     .createFolderParallel(sWORKS),
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

        Options.display("Ending the test in parallel");
    }
}
