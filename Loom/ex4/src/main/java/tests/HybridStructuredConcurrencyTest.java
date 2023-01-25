package tests;

import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;
import common.Options;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

/**
 * Download, transform, and store {@link Image} objects using the Java
 * structured concurrency framework, which uses the Java 19 {@link
 * Executors} {@code newVirtualThreadPerTaskExecutor()} factory method
 * to create a new virtual thread for each task.  This implementation
 * combines Java's structure concurrency features with modern Java
 * features, such as Java sequential streams, to provide a hybrid
 * solution.
 */
public class HybridStructuredConcurrencyTest {
    /**
     * This method combines Java structure concurrency with Java
     * sequential streams to run the test.
     */
    public static void run(String testName) {
        // Call downloadImages() to obtain a List of Future<Image>
        // objects that holds completed results of downloaded images.
        var downloadedImages =
            downloadImages(Options.instance().getUrlList());

        // Call transformImages() to obtain a List of Future<Image>
        // objects that holds the completed results of transformed images.
        var transformedImages =
            transformImages(downloadedImages);

        // Call storeImages() to obtain a List of Future<File> objects
        // that hold the completed results of stored images.
        var storedImages =
            storeImages(transformedImages);

        Options.instance()
            // Print the statistics for this test run.
            .printStats(testName,
                        storedImages.size());
    }

    /**
     * Download the {@code urlList} asynchronously.
     *
     * @param urlList A {@link List} of {@link URL} objects to
     *                download
     * @return A {@link List} of {@link Future} objects to downloaded
     *         {@link Image} objects
     */
    static List<Future<Image>> downloadImages(List<URL> urlList) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (var executor = Executors
             .newVirtualThreadPerTaskExecutor()) {
            // Return a List of Future<Image> objects that have all
            // completed by the time the List is returned.
            return urlList
                // Convert List to a Stream.
                .stream()

                // Map each URL to a Future<Image> object.
                .map(url -> executor
                     // submit() starts a virtual thread to
                     // download each image.
                     .submit(() -> FileAndNetUtils
                             // Download each image via its URL and
                             // store it in an Image object.
                             .downloadImage(url)))

                // Trigger intermediate processing and collect the
                // results into a List.
                .toList();

            // Scope doesn't exit until all concurrent virtual threads
            // complete or an exception occurs.
        }
    }

    /**
     * Transform {@code downloadedImages} asynchronously.
     *
     * @param downloadedImages A {@link List} of {@link Future}
     *                         objects to images that have been
     *                         downloaded
     * @return A {@link List} of {@link Future} objects to transformed
     *         {@link Image} objects
     */
    private static List<Future<Image>> transformImages
        (List<Future<Image>> downloadedImages) {
        // Create a new scope to execute virtual tasks.
        try (var executor = Executors
             .newVirtualThreadPerTaskExecutor()) {
            // Return a List of transformed images, which have
            // finished transforming at this point.
            return downloadedImages
                // Convert the List to a sequential Stream.
                .stream()

                // Map each Image Future to transformed Image Future.
                .flatMap(imageFuture ->
                         transformImage(executor,
                                        imageFuture.resultNow()))

                // Trigger intermediate processing and collect the
                // results into a List.
                .toList();

            // Scope doesn't exit until all concurrent virtual threads
            // complete or an exception occurs.
        }
    }

    /**
     * Stored the {@code transformedImages} asynchronously.
     *
     * @param transformedImages A {@link List} of {@link Future}
     *                          objects to {@link Image} objects that
     *                          have been transformed
     * @return A {@link List} of {@link Future} objects to stored
     *         {@link Image} objects
     */
    private static List<Future<File>> storeImages
        (List<Future<Image>> transformedImages) {
        // Create a new scope to execute virtual tasks.
        try (var executor = Executors
             .newVirtualThreadPerTaskExecutor()) {
            // Return the List of stored images, which have finished
            // storing at this point.
            return transformedImages
                // Convert the List into a sequential Stream.
                .stream()

                // Map each transformed Image into a stored Image.
                .map(transformedImage -> executor
                     // submit() starts a virtual thread to store each
                     // image.
                     .submit(() -> FileAndNetUtils
                             // Store each transformed image in a
                             // file.
                             .storeImage(transformedImage
                                         .resultNow())))

                // Trigger intermediate processing and collect the
                // results into a List.
                .toList();

            // Scope doesn't exit until all concurrent virtual threads
            // complete or an exception occurs.
        }
    }

    /**
     * Apply a group of {@link Transform} objects to transform the
     * {@link Image} concurrently.
     *
     * @param image The {@link Image} to transform
     * @return A {@link Stream} of {@link Future} objects to {@link
     *         Image} objects that are transforming concurrently
     */
    private static Stream<Future<Image>> transformImage
        (ExecutorService executor,
         Image image) {
        // Return the List of transforming images, which may still be
        // transforming at this point.
        return Options.instance()
            // Get the List of Transforms.
            .transforms()
            
            // Convert the List to a sequential Stream.
            .stream()

            // Apply each Transform to create a Future<Image>.
            .map(transform ->executor
                 // submit() starts a virtual thread to transform each
                 // image concurrently.
                 .submit(() ->
                         // Transform the image.
                         transform.transform(image)));
    }
}
