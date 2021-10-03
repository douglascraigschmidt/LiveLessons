package tests;

import utils.FileAndNetUtils;
import utils.FuturesCollector;
import utils.Image;
import utils.Options;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class CompletableFuturesTests {
    /**
     * This method uses Java completable futures to run the test.
     */
    private static void run() {
        // Get the list of files to the downloaded images.
        CompletableFuture<List<Image>> imageFilesFuture = Options.instance()
            // Get the List of URLs.
            .getUrlList()

            // Convert the List to a stream.
            .stream()

            // Transform URL to a File by downloading each image via
            // its URL.
            .map(url -> CompletableFuture
                 // Download each image and store it in a file
                 // asynchronously.
                 .supplyAsync(() ->
                              FileAndNetUtils.downloadImage(url)))

            .flatMap(CompletableFuturesTests::applyFiltersCF)

            // Terminate the stream and collect the results into list
            // of images.
            .collect(FuturesCollector.toFuture());

        // Print the statistics for this test run.
        Options.instance().printStats("Completable futures test",
                                      imageFilesFuture.join().size());
    }

    /**
     * Apply completable future sto asynchronously apply filters to
     * the {@code imageFuture} after it finishes downloading and store
     * the results in output files on the local computer.
     *
     * @param imageFuture A future to an {@link Image} that's being
     *                    downloaded
     * @return A stream of futures to {@link Image} objects that are
     *         being filtered/stored
     */
    private static Stream<CompletableFuture<Image>> applyFiltersCF
        (CompletableFuture<Image> imageFuture) {
        return null;
        /*
        return sTransforms
            // Convert the list of filters to a sequential stream.
            .stream()

            // Use map() to filter each image asynchronously.
            .map(filter -> imageFuture
                 // Asynchronously apply a filter action after the
                 // previous stage completes.
                 .thenApplyAsync(image ->
                                 // Create and apply the filter to the
                                 // image.
                                 makeFilterDecoratorWithImage(filter,
                                                              image).run()));

         */
    }
}
