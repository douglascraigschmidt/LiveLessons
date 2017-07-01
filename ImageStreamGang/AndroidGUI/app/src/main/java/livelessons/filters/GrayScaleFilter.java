package livelessons.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

import livelessons.utils.Image;

/**
 * A Filter sublcass that converts a downloaded image to grayscale.
 */
public class GrayScaleFilter 
       extends Filter {
    /**
     * Constructs a default GrayScaleFilter.
     */
    public GrayScaleFilter() {}

    /**
     * Constructs a Grayscale filter with the given name. This
     * constructor can be used to specify the output directory the
     * grayscale filtered images should be stored in. This naming
     * functionality would also be useful for filters to which
     * parameters are passed. For example, a 5x5 box filter and a 3x3
     * box filter could reuse identical code, but be stored in
     * different directories after processing.
     */
    public GrayScaleFilter(String name) {
        super(name);
    }

    /**
     * Uses the common color transformation values for grayscale
     * conversion using a pixel-by-pixel coloring algorithm.
     */
    @Override
    protected Image applyFilter(Image image) {
        // Forward to the platform-specific implementation of this
        // filter.
        Bitmap bitmap = image.getImage();
        Bitmap originalImage = bitmap;

        Bitmap grayScaleImage =
            originalImage.copy(originalImage.getConfig(), true);

        boolean hasTransparent = grayScaleImage.hasAlpha();
        int width = grayScaleImage.getWidth();
        int height = grayScaleImage.getHeight();

        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
            	
            	// Check if the pixel is transparent in the original
            	// by checking if the alpha is 0
                if (hasTransparent 
                    && ((grayScaleImage.getPixel(j, i) & 0xff000000) >> 24) == 0) {
                    continue;
                }
                
                // Convert the pixel to grayscale.
                int pixel = grayScaleImage.getPixel(j, i);
                int grayScale = 
                    (int) (Color.red(pixel) * .299
                           + Color.green(pixel) * .587
                           + Color.blue(pixel) * .114);
                grayScaleImage.setPixel(j, i, 
                                        Color.rgb(grayScale, grayScale, grayScale)
                                        );
            }
        }

        // Return an Image containing the filtered image.
        return new Image(image.getSourceURL(),
                         grayScaleImage);
    }
}
