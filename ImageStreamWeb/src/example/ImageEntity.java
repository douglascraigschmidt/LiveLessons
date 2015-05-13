package example;

import java.net.URL;

import filters.Filter;

/**
 * @class ImageEntity
 *
 * @brief Stores meta-data about an Image and also provides methods
 *        for common image- and file-related tasks, such as decoding
 *        raw byte arrays into an Image and setting/getting filter and
 *        file names.
 */
public class ImageEntity {
    /**
     * The Image our ImageEntity stores.
     */
    private Image mImage;

    /**
     * The source URL from which the result was downloaded.
     */
    protected URL mSourceUrl;

    /**
     * The name of the filter that was applied to this result.
     */
    protected String mFilterName;
    
    /**
     * Keeps track of whether operations on this ImageEntity succeed.
     */
    protected boolean mSucceeded;

    /**
     * Construct an ImageEntity from a byte array of @a imageData
     * downloaded from a URL @a source.
     */
    public ImageEntity(URL sourceURL,
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
     * Construct a new ImageEntity from an @a Image.
     */
    public ImageEntity(URL sourceURL,
                       Image image) {
        // Set the URL.
        mSourceUrl = sourceURL;

        // Initialize other data members.
        mFilterName = null;
        mSucceeded = true;

        // Store the image in the data member.
        mImage = image;
    }

    /**
     * Decodes a byte[] into an @a Image that can be used in the rest
     * of the application.
     */
    public void setImage(byte[] imageData) {
        mImage = PlatformStrategy.instance().makeImage(imageData);
    }

    /**
     * Returns the @a Image stored by this ImageEntity.
     */
    public Image getImage() {
        return mImage;
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
     * Sets whether operations on the ImageEntity succeeded or failed.
     */
    public void setSucceeded(boolean succeeded) {
        mSucceeded = succeeded;
    }

    /**
     * Returns true if operations on the ImageEntity succeeded, else
     * false.
     */
    public boolean getSucceeded() {
        return mSucceeded;
    }

    /**
     * Returns the file name from the URL this ImageEntity was
     * constructed from.
     */
    public String getFileName() {
        return mSourceUrl.getFile().substring
            (mSourceUrl.getFile().lastIndexOf('/'));
    }

    /**
     * Returns the format of the image from the URL in string form.
     */
    public String getFormatName() {
        String format =
            mSourceUrl.getFile().substring
            (mSourceUrl.getFile().lastIndexOf('.') + 1);
        format = format.equalsIgnoreCase("jpeg") ? "jpg" : format;
        return format;
    }
}
