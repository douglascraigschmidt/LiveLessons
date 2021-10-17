package tests;

import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;
import utils.Options;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static utils.ExceptionUtils.rethrowSupplier;

/**
 * Download, transform, and store {@link Image} objects using the
 * Java structured concurrency framework.
 */
public class StructuredConcurrencyTests {
    /**
     * This method uses Project Loom structure concurrency to run the
     * test.
     */
    public static void run() {
        // Call downloadImages() to obtain a List of Future<Image>
        // objects that holds futures to downloaded images.
        List<Future<Image>> downloadedImages =
            downloadImages(Options.instance().getUrlList());

        // Call transformImages() to obtain a List of Future<File>
        // objects that holds the results of transformed images.
        List<Future<Image>> transformedImages =
            transformImages(downloadedImages);

        // Call storeImages() to obtain a List of Future<File> objects
        // that hold the results of stored images.
        List<Future<File>> storedImages =
            storeImages(transformedImages);

        // Print the statistics for this test run.
        Options.instance().printStats("Structured concurrency test",
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
        // A List of Future<Image> objects that complete when the
        // images have been downloaded asynchronously.
        List<Future<Image>> downloadedImages = new ArrayList<>();

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of image URLs.
            for (URL url : urlList)
                downloadedImages
                    // Add each Future the Future<File> List.
                    .add(executor
                         // submit() starts a virtual thread to
                         // download each image.
                         .submit(() ->
                                 // Download each image via its URL
                                 // and store it in a File.
                                 FileAndNetUtils.downloadImage(url)));

            // Scope doesn't exit until all concurrent tasks complete.
        } 

        // Return the List of downloaded images, which have finished
        // downloading at this point.
        return downloadedImages;
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
        // A List of Future<Image> objects that complete when the
        // images have been transformed asynchronously.
        List<Future<Image>> transformedImages = new ArrayList<>();

        // Create a new scope to execute virtual tasks.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of imageFutures.
            for (Future<Image> image : downloadedImages) {
                transformedImages
                    // ...
                    .addAll(transformImage(executor,
                                           rethrowSupplier(image::get).get()));
            }

            // Scope doesn't exit until all concurrent tasks complete.
        } 

        // Return the List of transformed images, which have finished
        // transforming at this point.
        return transformedImages;
    }

    /**
     * @return
     */
    private static List<Future<File>> storeImages
        (List<Future<Image>> transformedImages) {
        // A List of Future<File> objects that complete when the
        // images have been stored asynchronously.
        List<Future<File>> storedFiles = new ArrayList<>();

        // Create a new scope to execute virtual tasks.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of transforming image futures.
            for (Future<Image> image : transformedImages)
                storedFiles
                    // Add each Future the Future<File> List.
                    .add(executor
                         // submit() starts a virtual thread to store
                         // each image.
                         .submit(() -> FileAndNetUtils
                                 // Store each transformed image in a
                                 // file.
                                 .storeImage(rethrowSupplier(image::get).get())));

            // Scope doesn't exit until all concurrent tasks complete.
        }

        // Return the List of stored images, which have finished
        // storing at this point.
        return storedFiles;
    }

    /**
     * This method applies a group of {@link Transform} objects to
     * transform the {@link Image}.
     *
     * @param image The {@link Image} to transform
     * @return A {@link List} of {@link Future} objects to transformed
     *         {@link Image} objects
     */
    private static List<Future<Image>> transformImage
        (ExecutorService executor,
         Image image) {
        List<Future<Image>> transformedImageFutures = 
            new ArrayList<>();

        for (Transform transform : Options.instance().transforms())
            transformedImageFutures
                .add(executor
                     // submit() starts a virtual thread to transform each
                     // image.
                     .submit(() ->
                             // Transform each image
                             transform.transform(image)));

        return transformedImageFutures;
    }
}
