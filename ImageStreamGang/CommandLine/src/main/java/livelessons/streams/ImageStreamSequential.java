package livelessons.streams;

import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import livelessons.utils.Image;
import livelessons.filters.Filter;

/**
 * This implementation strategy customizes ImageStreamGang to use a
 * Java stream to download, process, and store images sequentially.
 */
public class ImageStreamSequential 
       extends ImageStreamGang {
    /**
     * Constructor initializes the superclass.
     */
    public ImageStreamSequential(Filter[] filters,
                                 Iterator<List<URL>> urlListIterator) {
        super(filters, urlListIterator);
    }

    /**
     * This hook method uses a Java stream to download, process, and
     * store images sequentially.
     */
    @Override
    protected void processStream() {
        // Get the input URLs.
        List<URL> urls = getInput();

        List<Image> filteredImages = urls
            // Convert the URLs in the input list into a stream and
            // process them sequentially.
            .stream()

            // Use filter() to ignore URLs that are already cached locally,
            // i.e., only download non-cached images.
            .filter(Predicate.not(this::urlCached))

            // Use map() to transform each URL to an image (i.e.,
            // synchronously download each image via its URL).
            .map(this::downloadImage)

            // Use flatMap() to create a stream containing multiple
            // filtered versions of each image.
            .flatMap(this::applyFilters)

            // Terminate the stream and collect the results into
            // list of images.
            .toList();

        System.out.println(TAG
                           + ": processing of "
                           + filteredImages.size()
                           + " image(s) from "
                           + urls.size() 
                           + " urls is complete");
    }

    /**
     * Apply the image filters to each @a image sequentially.
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters
            // Iterate through the list of image filters sequentially and
            // apply each one to the image.
            .stream()

            // Use map() to create an OutputFilterDecorator for each
            // image and run it to filter each image and store it in an
            // output file.
            .map(filter -> 
                 makeFilterDecoratorWithImage(filter, image).run());
    }
}
