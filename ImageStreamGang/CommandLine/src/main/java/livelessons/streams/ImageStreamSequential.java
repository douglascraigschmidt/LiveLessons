package livelessons.streams;

import static java.util.stream.Collectors.toList;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import livelessons.utils.Image;
import livelessons.utils.StreamsUtils;
import livelessons.filters.Filter;
import livelessons.filters.FilterDecoratorWithImage;

/**
 * Customizes ImageStreamGang to use a Java 8 stream to download, process,
 * and store images sequentially.
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
     * Perform the ImageStreamGang processing, which uses a Java 8 stream
     * to download, process, and store images sequentially.
     */
    @Override
    protected void processStream() {
        List<Image> collect = getInput()
            // Sequentially process each URL in the input List.
            .stream()

            // Use filter() to ignore URLs that are already cached locally,
            // i.e., only download non-cached images.
            .filter(StreamsUtils.not(this::urlCached))

            // Use map() to transform each URL to an image (e.g., download
            // each image via its URL).
            .map(ImageStreamGang::downloadImage)

            // Use flatMap() to create a stream containing multiple filtered
            // versions of each image.
            .flatMap(this::applyFilters)

            // Terminate the stream and collect the results into
            // list of images.
            .collect(toList());

        System.out.println(TAG
                           + ": processing of "
                           + collect.size()
                           + " image(s) is complete");
    }

    /**
     * @return true if the @a url is already in the cache, else false.
     */
    @Override
    protected boolean urlCached(URL url) {
        // Iterate through the list of image filters sequentially and use
        // the filter aggregate operation to exclude those already cached.
        long count = mFilters
            .stream()
            .filter(filter ->
                    urlCached(url, filter.getName()))
            .count();

        // A count > 0 means the url has already been cached.
        return count > 0;
    }

    /**
     * Apply the filters to each @a image sequentially.
     */
    private Stream<Image> applyFilters(Image image) {
        return mFilters
            // Iterate through the list of image filters sequentially and
            // apply each one to the image.
            .stream()

            // Use map() to create an OutputFilterDecorator for each image.
            .map(filter -> makeFilterDecoratorWithImage(filter, image))

            // Use map() to apply the image filter to each image and store
            // it in an output file.
            .map(FilterDecoratorWithImage::run);
    }
}
