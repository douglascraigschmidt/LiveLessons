package livelessons.platspec;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Stores platform-specific meta-data about an Image and also provides
 * methods for common image- and file-related tasks.  This
 * implementation is specific to the Android platform.
 */
public class ImageBase {
    /**
     * The Bitmap our Image stores.
     */
    protected Bitmap mImage;

    /**
     * Dimensions representing how large the scaled image should be.
     */
    private static final int IMAGE_WIDTH = 250;
    private static final int IMAGE_HEIGHT = 250;

    /**
     * Returns the @a Bitmap stored by this Image.
     */
    public Bitmap getImage() {
        return mImage;
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
     * Set the Image from the @a image object.
     */
    public void setImage(Object image) {
        mImage = (Bitmap) image;
    }

    /**
     * Decodes and scales a bitmap from a byte array.  Adapted from
     * developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    protected Bitmap decodeSampledBitmapFromByteArray(byte[] imageData,
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
    protected int calculateInSampleSize(BitmapFactory.Options options,
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
