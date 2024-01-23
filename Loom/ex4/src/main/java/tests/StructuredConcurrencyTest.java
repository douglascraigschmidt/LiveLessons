package tests;

import java.util.concurrent.StructuredTaskScope;
import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;
import common.Options;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * Download, transform, and store {@link Image} objects using the Java
 * structured concurrency framework, which uses the {@link
 * StructuredTaskScope} {@code ShutdownOnFailure()} factory method to
 * create a new virtual thread for each task.  This implementation
 * just applies Java 7 features, i.e., it doesn't use modern Java
 * features at all, except for the {@link StructuredTaskScope} class
 * defined in Java 21.
 */
public class StructuredConcurrencyTest {
    /**
     * This method uses Java structure concurrency to run the test.
     */
    public static void run(String testName) {
        // Call downloadImages() to obtain a List of Supplier<Image>
        // objects that holds supplier objects to downloaded images.
        var downloadedImages =
            downloadImages(Options.instance().getUrlList());

        // Call transformImages() to obtain a List of Supplier<Image>
        // objects that holds the results of transformed images.
        var transformedImages =
            transformImages(downloadedImages);

        // Call storeImages() to obtain a List of Supplier<File> objects
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
     * @return A {@link List} of {@link Supplier} objects to downloaded
     *         {@link Image} objects
     */
    static List<Supplier<Image>> downloadImages(List<URL> urlList) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // A List of Supplier<Image> objects that complete when the
            // images have been downloaded asynchronously.
            List<Supplier<Image>> downloadedImages
                = new ArrayList<>();

            // Iterate through the List of image URLs.
            for (URL url : urlList)
                downloadedImages
                    // Add each Supplier the Supplier<Image> List.
                    .add(scope
                         // submit() starts a virtual thread to
                         // download each image.
                         .fork(() -> FileAndNetUtils
                               // Download each image via its URL
                               // and store it in an Image object.
                               .downloadImage(url)));

            // Scope doesn't exit until all concurrent tasks complete
            // or an exception occurs.
            scope.join();

            // Handle any exception that has occurred.
            scope.throwIfFailed();

            // Return the List of downloaded images, which have
            // finished downloading at this point.
            return downloadedImages;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Transform {@code downloadedImages} asynchronously.
     *
     * @param downloadedImages A {@link List} of {@link Supplier}
     *                         objects to images that have been
     *                         downloaded
     * @return A {@link List} of {@link Supplier} objects to transformed
     *         {@link Image} objects
     */
    private static List<Supplier<Image>> transformImages
        (List<Supplier<Image>> downloadedImages) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // A List of Supplier<Image> objects that complete when the
            // images have been transformed asynchronously.
            List<Supplier<Image>> transformedImages =
                new ArrayList<>();

            // Iterate through the List of imageSupplier objects.
            for (var imageSupplier : downloadedImages) {
                transformedImages
                    // Append the transforming images at the end
                    // of the List.
                    .addAll(transformImage(scope,
                                           imageSupplier.get()));
            }

            // Wait until all concurrent tasks complete successfully
            // or throw the exception if one occurs.
            scope.join().throwIfFailed();

            // Return the List of transformed images, which have
            // finished transforming at this point.
            return transformedImages;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Stored the {@code transformedImages} asynchronously.
     *
     * @param transformedImages A {@link List} of {@link Supplier}
     *                          objects to {@link Image} objects that
     *                          have been transformed
     * @return A {@link List} of {@link Supplier} objects to stored
     *         {@link Image} objects
     */
    private static List<Supplier<File>> storeImages
        (List<Supplier<Image>> transformedImages) {
        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete.
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // A List of Supplier<File> objects that complete when the
            // images have been stored asynchronously.
            List<Supplier<File>> storedFiles =
                new ArrayList<>();

            // Iterate through the List of transformed image
            // suppliers.
            for (var imageSupplier : transformedImages)
                storedFiles
                    // Add each Supplier the Supplier<File> List.
                    .add(scope
                             // submit() starts a virtual thread to store
                             // each image.
                             .fork(() -> FileAndNetUtils
                                 // Store each transformed image in a
                                 // file.
                                 .storeImage(imageSupplier.get())));

            // Scope doesn't exit until all concurrent tasks complete
            // or an exception is thrown.
            scope.join();

            // Handle any exception that has occurred.
            scope.throwIfFailed();

            // Return the List of stored images (as File objects),
            // which have finished storing at this point.
            return storedFiles;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Apply a group of {@link Transform} objects to transform the
     * {@link Image}.
     *
     * @param image The {@link Image} to transform
     * @return A {@link List} of {@link Supplier} objects to transformed
     *         {@link Image} objects
     */
    private static List<Supplier<Image>> transformImage
        (StructuredTaskScope.ShutdownOnFailure scope,
         Image image) {

        // A List of Supplier<Image> objects that complete when the
        // images have been transformed asynchronously.
        List<Supplier<Image>> transformedImageSuppliers =
            new ArrayList<>();

        // Iterate through the List of Transformed objects.
        for (Transform transform : Options.instance().transforms())
            transformedImageSuppliers
                .add(scope
                     // submit() starts a virtual thread to transform
                     // each image.
                     .fork(() -> transform
                             // Transform each image
                             .transform(image)));

        // Return the List of transforming images, which may still be
        // transforming at this point.
        return transformedImageSuppliers;
    }
}
