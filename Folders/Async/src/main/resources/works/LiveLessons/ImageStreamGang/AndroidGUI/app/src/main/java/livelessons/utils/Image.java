package livelessons.utils;

import livelessons.filters.Filter;
import livelessons.platspec.ImageBase;

import java.net.URL;

/**
 * Stores platform-independent meta-data about an Image and also
 * provides methods for common image- and file-related tasks, such as
 * decoding raw byte arrays into an Image and setting/getting filter
 * and file names.
 */
public class Image 
       extends ImageBase {
    /**
     * The source URL from which the result was downloaded.
     */
    private URL mSourceUrl;

    /**
     * The name of the filter that was applied to this result.
     */
    private String mFilterName;
    
    /**
     * Keeps track of whether operations on this Image succeed.
     */
    private boolean mSucceeded;

    /**
     * Dimensions representing how large the scaled image should be.
     */
    private static final int IMAGE_WIDTH = 250;
    private static final int IMAGE_HEIGHT = 250;

    /**
     * Construct an Image from a byte array of @a imageData
     * downloaded from a URL @a source.
     */
    public Image(URL sourceURL,
                 byte[] imageData) {
        // Set the URL.
        mSourceUrl = sourceURL;

        // Initialize other data members.
        mFilterName = null;
        mSucceeded = true;
        
        // Decode the imageData into a Bitmap.
        setImage(imageData);
    }

    /**
     * Construct an Image from an @a image created from URL @a source.
     */
    public Image(URL sourceURL,
                 Object image) {
        // Set the URL.
        mSourceUrl = sourceURL;

        // Initialize other data members.
        mFilterName = null;
        mSucceeded = true;
        
        // Decode the imageData into a Bitmap.
        setImage(image);
    }

    /**
     * Modifies the source URL of this result. Necessary for when the
     * result is constructed before it is associated with data.
     */
    public void setSourceURL(URL url) {
        mSourceUrl = url;
    }

    /**
     * Returns the source URL this result was constructed from.
     */
    public URL getSourceURL() {
        return mSourceUrl;
    }

    /**
     * Sets the name of the filter applied to this result.
     */
    public void setFilterName(Filter filter) {
        mFilterName = filter.getName();
    }

    /**
     * Returns the name of the filter applied to this result.
     */
    public String getFilterName() {
        return mFilterName;
    }

    /**
     * Sets whether operations on the Image succeeded or failed.
     */
    public void setSucceeded(boolean succeeded) {
        mSucceeded = succeeded;
    }

    /**
     * Returns true if operations on the Image succeeded, else
     * false.
     */
    public boolean getSucceeded() {
        return mSucceeded;
    }

    /**
     * Returns the file name from the URL this Image was
     * constructed from.
     */
    public String getFileName() {
        return NetUtils.getFileNameForUrl(mSourceUrl);
    }

    /**
     * Returns the format of the image from the URL in string form.
     */
    public String getFormatName() {
        String format =
            mSourceUrl.getFile().substring
            (mSourceUrl.getFile().lastIndexOf('.') + 1);
        return format.equalsIgnoreCase("jpeg") ? "jpg" : format;
    }
}
