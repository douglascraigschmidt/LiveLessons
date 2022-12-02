package tests;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;
import common.Options;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

/**
 * Download, transform, and store {@link Image} objects using the
 * Project Reactor framework.
 */
public class ProjectReactorTest {
    /**
     * This method uses Project Reactor {@link ParallelFlux} reactive
     * programming features to run the test.
     */
    public static void run(String testName) {
        // Store the list of downloaded/tranformed images.
        var imageFiles = Flux
            // Convert the List of URLs into a Flux stream.
            .fromIterable(Options.instance().getUrlList())

            // Convert Flux into a ParallelFlux.
            .parallel()

            // Run computations in the "bounded elastic" thread pool.
            .runOn(Schedulers.boundedElastic())

            // Transform URL to an image by downloading each image via
            // its URL.
            .map(FileAndNetUtils::downloadImage)

            // Apply transforms to all images, yielding a stream of
            // stream of images.
            .flatMap(ProjectReactorTest::transformImage)

            // Store the images.
            .map(FileAndNetUtils::storeImage)

            // Convert the ParallelFlux back into a Flux.
            .sequential()

            // Terminate the stream and collect the results into List
            // of images.
            .collectList()
            
            // Block until all parallel computations complete.
            .block();

        // Print the statistics for this test run.
        assert imageFiles != null;

        Options.instance().printStats(testName,
                                      imageFiles.size());
    }

    /**
     * This method applies a group of {@link Transform} objects to
     * transform the {@link Image}.
     *
     * @param image The {@link Image} to transform
     * @return A {@link Stream} of transformed {@link Image} objects
     */
    private static ParallelFlux<Image> transformImage(Image image) {
        return Flux
            // Convert the List of transforms into a Flux stream.
            .fromIterable(Options.instance().transforms())

            // Convert Flux into a ParallelFlux.
            .parallel()

            // Run computations in the "bounded elastic" thread pool.
            .runOn(Schedulers.boundedElastic())

            // .peek(i -> System.out.println(Thread.currentThread().getName()))

            // Apply each transform to the original image to produce a
            // transformed image.
            .map(transform -> transform.transform(image));
    }
}
