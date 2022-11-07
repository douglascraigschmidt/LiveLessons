package example.imagetaskgang;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @class BitmapImage
 *
 * @brief Encapsulates the Android Bitmap class by extending
 *        the platform-independent Image interface.
 */
class BitmapImage implements Image {
    /**
     * An Android Bitmap object.
     */
    public Bitmap mBitmap;
    
    /**
     * Dimensions representing how large the scaled
     * image should be
     */
    private static final int IMAGE_WIDTH = 250;
    private static final int IMAGE_HEIGHT = 250;

    /**
     * Constructor that converts an @a imageData raw byte array into
     * an Android Bitmap.
     */
    public BitmapImage(byte[] imageData) {
        mBitmap = decodeSampledBitmapFromByteArray(imageData,
                                                   IMAGE_WIDTH,
                                                   IMAGE_HEIGHT);
    }

    /**
     * Constructor that stores the @a bitmap parameter into the data
     * member.
     */
    public BitmapImage (Bitmap bitmap) {
    	mBitmap = bitmap;
    }
    
    /**
     * Decodes and scales a bitmap from a byte array.  Adapted from
     * developer.android.com/training/displaying-bitmaps/load-bitmap.html
     */
    private Bitmap decodeSampledBitmapFromByteArray(byte[] imageData,
                                                    int reqWidth, int reqHeight) {

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
