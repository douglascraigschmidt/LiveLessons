package tests;

import folder.Dirent;
import utils.RunTimer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static utils.Options.display;
import static utils.Options.sWORKS;

public class TestDriverSync {
    /**
     * Run the tests synchronously.
     */
    public static void runSyncTests() {
        display("Starting runSyncTests()");

        // Run garbage collector to avoid perturbing the tests.
        System.gc();

        // Clever hack.. ;-)
        Dirent[] dirent = new Dirent[1];

        RunTimer
            // Record the time needed to create a new folder.
            .timeRun(() -> dirent[0] = Tests.createFolder(sWORKS).join(),
                     "sync createFolder()");

        // Run the following tests synchronously (by calling join())
        // and measure how long they take to execute.
        runConsumerSync(dirent[0],
                        folder -> {
                            Tests.countEntries(folder)
                                 .join();
                        },
                        "sync countEntries()");

        runConsumerSync(dirent[0],
                        folder -> Tests
                        .countLines(folder)
                        .join(),
                        "sync countLines()");

        runBiConsumerSync(dirent[0],
                          (rootFolder, searchWord) ->Tests
                          .searchFolders(rootFolder,
                                         searchWord)
                          .join(),
                          "CompletableFuture",
                          "sync searchFolders()");

        display("Ending runSyncTests()");
    }

    /**
     * A factory method that runs a {@link Consumer} synchronously.
     *
     * @param rootFolder The created folder
     * @param consumer The {@link Consumer} to run
     * @param consumerName The name of the {@link Consumer} to run
     */
    private static void runConsumerSync
        (Dirent rootFolder,
         Consumer<Dirent> consumer,
         String consumerName) {
        RunTimer
            // Compute time needed to apply func on
            // rootFolder in common fork-join pool.
            .timeRun(() ->
                     consumer.accept(rootFolder),
                     consumerName);
    }

    /**
     * A factory method that runs a {@link BiConsumer} synchronously.
     *
     * @param rootFolder The created folder
     * @param consumer The {@link BiConsumer} to run
     * @param param The parameter to pass to {@code BiConsumer}
     * @param consumerName The name of the {@link BiConsumer} to run
     */
    private static void runBiConsumerSync
        (Dirent rootFolder,
         BiConsumer<Dirent, String> consumer,
         String param,
         String consumerName) {
        RunTimer
            // Compute time needed to apply func on
            // rootFolder in common fork-join pool.
            .timeRun(() ->
                     consumer.accept(rootFolder, param),
                     consumerName);
    }
}
