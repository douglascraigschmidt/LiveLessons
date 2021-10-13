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
        // A List of Future<Image> objects that holds futures to
        // downloading images.
        List<Future<Image>> downloadingImageFutures = new ArrayList<>();

        // Create a new scope to execute virtual tasks, which exits
        // only after all tasks complete by using the new AutoClosable
        // feature of ExecutorService in conjunction with a
        // try-with-resources block.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of image URLs.
            for (URL url : Options.instance().getUrlList())
                downloadingImageFutures
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

        // A List of Future<Image> objects that holds futures to
        // transforming images.
        List<Future<Image>> transformingImageFutures = new ArrayList<>();

        // Create a new scope to execute virtual tasks.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of imageFutures.
            for (Future<Image> image : downloadingImageFutures) {
                transformingImageFutures
                    // ...
                    .addAll(transformImage(executor,
                                           rethrowSupplier(image::get).get()));
            }

            // Scope doesn't exit until all concurrent tasks complete.
        } 

        // A List of Future<File> objects that holds the results of
        // storing images.
        List<Future<File>> fileFutures = new ArrayList<>();

        // Create a new scope to execute virtual tasks.
        try (ExecutorService executor = Executors.newVirtualThreadExecutor()) {
            // Iterate through the List of transforming image futures.
            for (Future<Image> image : transformingImageFutures)
                fileFutures
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

        // Print the statistics for this test run.
        Options.instance().printStats("Structured concurrency test",
                                      fileFutures.size());
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
