package tests;

import common.Options;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;

import java.net.URL;
import java.util.stream.Collectors;

import static utils.FileAndNetUtils.downloadContent;

/**
 * Download, transform, and store {@link Image} objects using the
 * RxJava framework and the {@link ParallelFlowable} class.
 */
public class RxJavaTest {
    /**
     * This method uses Project Reactor {@link ParallelFlowable} reactive
     * programming features to run the test.
     */
    public static void run(String testName) {
        // Store the list of downloaded/transformed images.
        var imageFiles = Flowable
            // Convert the List of URLs into a Flowable stream.
            .fromIterable(Options.instance().getUrlList())

            // Convert Flowable into a ParallelFlowable.
            .parallel()

            // Run computations in the "io" thread pool.
            .runOn(Schedulers.io())

            // Transform URL to an image by downloading each image via
            // its URL.
            .map(RxJavaTest::downloadImage)

            // Apply transforms to all images, yielding a stream of
            // stream of images.
            .flatMap(RxJavaTest::transformImage)

            // Store the images.
            .map(FileAndNetUtils::storeImage)

            // Convert the ParallelFlowable back into a Flowable.
            .sequential()

            // Terminate the stream and collect the results into List
            // of images.
            .collect(Collectors.toList())
            
            // Block until all parallel computations complete.
            .blockingGet();

        // Print the statistics for this test run.
        Options.instance().printStats(testName,
                                      imageFiles.size());
    }

    /**
     * This method applies a group of {@link Transform} objects to
     * transform the {@link Image}.
     *
     * @param image The {@link Image} to transform
     * @return A {@link Flowable} of transformed {@link Image} objects
     */
    private static Flowable<Image> transformImage(Image image) {
        return Flowable
            // Convert the List of transforms into a Flowable stream.
            .fromIterable(Options.instance().transforms())

            // Convert Flowable into a ParallelFlowable.
            .parallel()

            // Run computations in the "bounded elastic" thread pool.
            .runOn(Schedulers.io())

            // Apply each transform to the original image to produce a
            // transformed image.
            .map(transform -> transform.transform(image))

            // Convert ParallelFlowable back to a Flowable.
            .sequential();
    }

    /**
     * Transform a {@link URL} to an {@link Image} by downloading the
     * contents of the {@code url}.
     *
     * @param url The {@link URL} of the image to download
     * @return An {@link Image} containing the image contents
     */
    public static Image downloadImage(URL url) {
        return
                // Perform a blocking image download.
                new Image(url,
                          downloadContent(url));
    }
}
