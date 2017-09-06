package livelessons.filters;

import livelessons.platspec.PlatSpec;
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
        return PlatSpec.applyFilter(image);
    }
}
