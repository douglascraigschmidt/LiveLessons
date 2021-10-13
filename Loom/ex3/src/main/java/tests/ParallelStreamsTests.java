package tests;

import transforms.Transform;
import utils.FileAndNetUtils;
import utils.Image;
import utils.Options;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Download, transform, and store {@link Image} objects using the
 * Java parallel streams framework.
 */
public class ParallelStreamsTests {
    /**
     * This method uses Java parallel streams with reduce()/concat()
     * to run the test.
     */
    public static void runReduceConcat() {
        // Store the list of downloaded/tranformed images.
        List<File> imageFiles = Options.instance()
            // Get the List of URLs.
            .getUrlList()

            // Convert List into a parallel stream.
            .parallelStream()

            // Transform URL to an image by downloading each image via
            // its URL.
            .map(FileAndNetUtils::downloadImage)

            // Apply transforms to all images, yielding a stream
            // of streams of images.
            .map(ParallelStreamsTests::transformImage)

            // Convert the stream of streams of images into a stream
            // of images without using flatMap().
            .reduce(Stream::concat).orElse(Stream.empty())

            // Store the images.
            .map(FileAndNetUtils::storeImage)

            // Terminate the stream and collect the results into list
            // of images.
            .collect(Collectors.toList());

        // Print the statistics for this test run.
        Options.instance().printStats("Parallel streams reduce()/concat() test",
                                      imageFiles.size());
    }

    /**
     * This method applies a group of {@link Transform} objects to
     * transform the {@link Image}.
     *
     * @param image The {@link Image} to transform
     * @return A {@link Stream} of transformed {@link Image} objects
     */
    private static Stream<Image> transformImage(Image image) {
        return Options.instance().transforms()
            // Convert the List of transforms to a parallel stream.
            .parallelStream()

            // .peek(i -> System.out.println(Thread.currentThread().getName()))

            // Apply each transform to the original image to produce a
            // transformed image.
            .map(transform -> transform.transform(image));
    }
}
