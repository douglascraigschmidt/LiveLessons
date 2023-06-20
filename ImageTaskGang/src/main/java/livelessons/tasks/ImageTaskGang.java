package livelessons.tasks;

import livelessons.filters.Filter;
import livelessons.utils.Image;
import livelessons.utils.FileAndNetUtils;

import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Customizes the {@link TaskGang} framework for images and defines
 * methods that are reused by various subclasses.
 */
public abstract class ImageTaskGang 
       extends TaskGang<URL> {
    /**
     * An iterator of input URLs that are used to download images.
     */
    protected Iterator<List<URL>> mUrlListIterator;

    /**
     * The {@link List} of filters to apply to the downloaded images.
     */
    protected List<Filter> mFilters;

    /**
     * Constructor initializes the superclass and fields.
     */
    public ImageTaskGang(Filter[] filters,
                         List<List<URL>> urlLists) {
        // Store the Filters to apply as a List.
        mFilters = Arrays.asList(filters);

        // Create an Iterator for the array of URLs to download.
        mUrlListIterator = urlLists.iterator();
    }

    /**
     * Factory method that returns the next List of URLs to download
     * and process concurrently by the {@link ImageTaskGang}.
     */
    @Override
    protected List<URL> getNextInput() {
        if (mUrlListIterator.hasNext()) {
            // Note that we're starting a new cycle.
            incrementCycle();

            // Return a List containing the URLs to download
            // concurrently.
            return mUrlListIterator.next();
        }
        else
            // Indicate that we're done.
            return null;
    }

    /**
     * Factory method that retrieves the image associated with the
     * {@code url} and creates an Image to encapsulate it.
     */
    protected Image getOrDownloadImage(URL url) {
        return new Image(url,
                         FileAndNetUtils.downloadContent(url));
    }
}
