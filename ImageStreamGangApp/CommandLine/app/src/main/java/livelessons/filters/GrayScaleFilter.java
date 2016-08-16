package livelessons.filters;

import java.awt.Color;
import java.awt.image.BufferedImage;

import utils.Image;

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
        BufferedImage originalImage = image.getImage();
        BufferedImage grayScaleImage =
            new BufferedImage
            (originalImage.getColorModel(),
             originalImage.copyData(null),
             originalImage.getColorModel().isAlphaPremultiplied(),
             null);
    
        boolean hasTransparent =
            grayScaleImage.getColorModel().hasAlpha();
        int width = grayScaleImage.getWidth();
        int height = grayScaleImage.getHeight();
    
        // A common pixel-by-pixel grayscale conversion algorithm
        // using values obtained from en.wikipedia.org/wiki/Grayscale.
        for (int i = 0; i < height; ++i) {
            for (int j = 0; j < width; ++j) {
            	
            	// Check if the pixel is transparent in the original.
                if (hasTransparent 
                    && (grayScaleImage.getRGB(j,
                                            i) >> 24) == 0x00) 
                    continue;
                
                // Convert the pixel to grayscale.
                Color c = new Color(grayScaleImage.getRGB(j,
                                                        i));
                int grayConversion =
                    (int) (c.getRed() * 0.299)
                    + (int) (c.getGreen() * 0.587)
                    + (int) (c.getBlue() * 0.114);
                Color grayScale = new Color(grayConversion,
                                            grayConversion,
                                            grayConversion);
                grayScaleImage.setRGB(j, i, grayScale.getRGB());
            }
        }
   	
         return new Image(image.getSourceURL(),
                         grayScaleImage);
    }
}
