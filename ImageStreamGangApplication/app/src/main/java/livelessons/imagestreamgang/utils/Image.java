package livelessons.imagestreamgang.utils;

import java.net.URL;

import livelessons.imagestreamgang.filters.Filter;

/**
 * Stores meta-data about an Image and also provides methods for
 * common image- and file-related tasks, such as decoding raw byte
 * arrays into an Image and setting/getting filter and file names.
 */
public class Image {
    /**
     * The Bitmap our Image stores.
     */
    private Bitmap mImage;

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
     * Construct a new Image from an @a Image.
     */
    public Image(URL sourceURL,
                 Bitmap image) {
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
        mImage = decodeSampledBitmapFromByteArray(imageData,
                                                  IMAGE_WIDTH,
                                                  IMAGE_HEIGHT);
    }

    /**
     * Returns the @a Bitmap stored by this Image.
     */
    public Bitmap getImage() {
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

    /**
     * Decodes and scales a bitmap from a byte array.  Adapted from
     * developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    private Bitmap decodeSampledBitmapFromByteArray(byte[] imageData,
                                                    int reqWidth,
                                                    int reqHeight) {

        // First decode with inJustDecodeBounds=true to check
        // dimensions.
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(imageData,
                                      0,
                                      imageData.length,
                                      options);

        // Calculate inSampleSize.
        options.inSampleSize = calculateInSampleSize(options,
                                                     reqWidth,
                                                     reqHeight);

        // Decode bitmap with inSampleSize set.
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(imageData,
                                             0,
                                             imageData.length,
                                             options);
    }
    
    /**
     * Calculates the Bitmap's sampling rate to fit the given
     * dimensions. Adapted from
     * developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth,
                                      int reqHeight) {
        // Raw height and width of image.
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
	
        if (height > reqHeight || width > reqWidth) {
	
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
	
            // Calculate the largest inSampleSize value that is a
            // power of 2 and keeps both height and width larger than
            // the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                   && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
	
        return inSampleSize;
    }
}
