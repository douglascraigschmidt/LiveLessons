package livelessons.imagestreamgang.streams;

import android.util.Log;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.utils.Image;

/**
 * Customizes ImageStream to use a Java 8 stream to download, process,
 * and store images sequentially.
 */
public class ImageStreamSequential 
       extends ImageStream {
    /**
     * Constructor initializes the superclass and data members.
     */
    public ImageStreamSequential(Filter[] filters,
                                 Iterator<List<URL>> urlListIterator,
                                 Runnable completionHook) {
        super(filters, urlListIterator, completionHook);
    }

    /**
     * Perform the ImageStream processing, which uses a Java 8 stream
     * to download, process, and store images concurrently.
     */
    @Override
    protected void processStream() {
        getInput()
            // Sequentially process each URL in the input List.
            .stream()

            // Filter out URLs that are already cached.
            .filter(this::urlNotCached)

            // Transform URL -> Image (download each image via
            // its URL).
            .map(this::makeImage)

            // Each all filters to each image sequentially (similar to
            // a nested for loop).
            .forEach(this::applyFilters);
    }

    /**
     * @return false if the @a url is already in the cache, else true;
     */
    @Override
    protected boolean urlNotCached(URL url) {
        // Iterate through the list of filters and sequentially check
        // to see which ones are already cached.
        long count = mFilters
            .stream()
            .filter(filter ->
                    urlNotCached(url, filter.getName()))
            .count();

        // A count > 0 means the url was not already in the cache.
        return count > 0;
    }

    /**
     * Apply the filters to each @a image sequencially.
     */
    private void applyFilters(Image image) {
        mFilters
            // Iterate through the list of filters and apply each
            // filter sequentially.
            .stream()

            // Create an OutputDecoratedFilter for each image.
            .map(this::makeFilterDecorator)

            // Filter the image and store it in an output file.
            .forEach(decoratedFilter -> 
                     decoratedFilter.filter(image));
    }
}
