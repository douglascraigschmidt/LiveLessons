package tests;

import transforms.Transform;
import utils.FileAndNetUtils;
import utils.FuturesCollector;
import utils.Image;
import utils.Options;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Download, transform, and store {@link Image} objects using the Java
 * completable futures framework in conjunction with the Java
 * sequential streams framework and the common fork-join framework.
 */
public class CompletableFuturesTests {
    /**
     * This method uses Java completable futures to run the test.
     */
    public static void run() {
        // Get the list of files to the downloaded images.
        CompletableFuture<List<File>> imageFilesFuture = Options.instance()
            // Get the List of URLs.
            .getUrlList()

            // Convert the List to a sequential stream.
            .stream()

            // Transform URL to a File by downloading each image via
            // its URL.
            .map(url -> CompletableFuture
                 // Download each image and store it in a file
                 // asynchronously.
                 .supplyAsync(() -> FileAndNetUtils
                              .downloadImage(url)))

            // Asynchronously apply transforms to each image and
            // flatten the results into a stream.
            .flatMap(CompletableFuturesTests::applyTransforms)

            // Store each image in the stream.
            .map(CompletableFuturesTests::storeImage)

            // Terminate the stream and collect the results into List
            // of File objects when all async processing completes.
            .collect(FuturesCollector.toFuture());

        // Print the statistics for this test run.
        Options.instance().printStats("Completable futures test",
                                      imageFilesFuture.join().size());
    }

    /**
     * Asynchronously store the {@code imageFuture} when it completes and
     * return a {@link File} containing the image.
     *
     * @param imageFuture A {@link CompletableFuture} to an {@link Image}
     * @return A {@link CompletableFuture} to a {@link File}
     */
    private static CompletableFuture<File> storeImage
        (CompletableFuture<Image> imageFuture) {
        return imageFuture
            // Asynchronous store the image.
            .thenApplyAsync(Image::store);
    }

    /**
     * Asynchronously apply a {@link Transform} to the {@code
     * imageFuture} after it finishes downloading and return a {@link
     * Stream} of {@link CompletableFuture} objects to the {@link
     * Image} objects.
     *
     * @param imageFuture A future to an {@link Image} that's being
     *                    downloaded
     * @return A stream of futures to {@link Image} objects that are
     *         being transformed asynchronously
     */
    private static Stream<CompletableFuture<Image>> applyTransforms
        (CompletableFuture<Image> imageFuture) {
        return Options.instance().transforms()
            // Convert the list of transforms to a sequential stream.
            .stream()

            // Use map() to transform each image asynchronously.
            .map(transform -> // Apply transform to the image.
                 imageFuture
                 // Asynchronously apply a transform after the
                 // previous stage completes.
                 .thenApplyAsync(transform::transform));
    }
}
