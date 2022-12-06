package tests;

import jdk.incubator.concurrent.StructuredTaskScope;
import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;
import common.Options;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static utils.ExceptionUtils.*;

/**
 * Download, transform, and store {@link Image} objects using the Java
 * structured concurrency framework, which uses the {@link Executors}
 * {@code newVirtualThreadPerTaskExecutor()} factory method to create
 * a new virtual thread for each task.  This implementation just applies
 * Java 7 features, i.e., it doesn't use modern Java features at all.
 */
public class StructuredConcurrencyTest {
    /**
     * This method uses Java structure concurrency to run the test.
     */
    public static void run(String testName) {
        // Call downloadImages() to obtain a List of Future<Image>
        // objects that holds futures to downloaded images.
        var downloadedImages =
            downloadImages(Options.instance().getUrlList());

        // Call transformImages() to obtain a List of Future<Image>
        // objects that holds the results of transformed images.
        var transformedImages =
            transformImages(downloadedImages);

        // Call storeImages() to obtain a List of Future<File> objects
        // that hold the results of stored images.
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
        // only after all tasks complete.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // A List of Future<Image> objects that complete when the
            // images have been downloaded asynchronously.
            List<Future<Image>> downloadedImages
                = new ArrayList<>();

            // Iterate through the List of image URLs.
            for (URL url : urlList)
                downloadedImages
                    // Add each Future the Future<Image> List.
                    .add(scope
                         // submit() starts a virtual thread to
                         // download each image.
                         .fork(() -> FileAndNetUtils
                               // Download each image via its URL
                               // and store it in a File.
                               .downloadImage(url)));

            rethrowRunnable(scope::join);
            // Scope doesn't exit until all concurrent tasks complete.

            // Return the List of downloaded images, which have
            // finished downloading at this point.
            return downloadedImages;
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
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // A List of Future<Image> objects that complete when the
            // images have been transformed asynchronously.
            List<Future<Image>> transformedImages =
                new ArrayList<>();

            // Iterate through the List of imageFutures.
            for (var imageFuture : downloadedImages) {
                transformedImages
                    // Append the transforming images at the end
                    // of the List.
                    .addAll(transformImage(scope,
                                           rethrowSupplier
                                           (imageFuture::get)
                                           .get()));
            }

            rethrowRunnable(scope::join);
            // Scope doesn't exit until all concurrent tasks complete.

            // Return the List of transformed images, which have
            // finished transforming at this point.
            return transformedImages;
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
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // A List of Future<File> objects that complete when the
            // images have been stored asynchronously.
            List<Future<File>> storedFiles =
                new ArrayList<>();

            // Iterate through the List of transformed image futures.
            for (var imageFuture : transformedImages)
                storedFiles
                    // Add each Future the Future<File> List.
                    .add(scope
                             // submit() starts a virtual thread to store
                             // each image.
                             .fork(() -> FileAndNetUtils
                                 // Store each transformed image in a
                                 // file.
                                 .storeImage(rethrowSupplier
                                                 (imageFuture::get)
                                                 .get())));

            rethrowRunnable(scope::join);
            // Scope doesn't exit until all concurrent tasks complete.

            // Return the List of stored images, which have finished
            // storing at this point.
            return storedFiles;
        }
    }

    /**
     * Apply a group of {@link Transform} objects to transform the
     * {@link Image}.
     *
     * @param image The {@link Image} to transform
     * @return A {@link List} of {@link Future} objects to transformed
     *         {@link Image} objects
     */
    private static List<Future<Image>> transformImage
        (StructuredTaskScope.ShutdownOnFailure executor,
         Image image) {

        // A List of Future<Image> objects that complete when the
        // images have been transformed asynchronously.
        List<Future<Image>> transformedImageFutures = 
            new ArrayList<>();

        // Iterate through the List of Transformed objects.
        for (Transform transform : Options.instance().transforms())
            transformedImageFutures
                .add(executor
                     // submit() starts a virtual thread to transform
                     // each image.
                     .fork(() -> transform
                             // Transform each image
                             .transform(image)));

        // Return the List of transforming images, which may still be
        // transforming at this point.
        return transformedImageFutures;
    }
}
