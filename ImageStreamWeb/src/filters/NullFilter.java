package filters;

import example.ImageEntity;

/**
 * @class NullFilter
 *
 * @brief The NullFilter will return the image as it was downloaded.
 *        It's main purpose is to show the "Control" image, and to
 *        exemplify how filters are supposed to work on a basic level.
 */
public class NullFilter extends Filter {
    /**
     * Default constructor is a no-op.
     */
    public NullFilter() {}
    
    /**
     * Constructors for the NullFilter. See GrayScaleFilter for
     * explanation of filter naming.
     */
    public NullFilter(String name) {
        super(name);
    }
	
    /**
     * Constructs a new ImageEntity that does not change the original
     * at all.
     */
    @Override
    protected ImageEntity applyFilter(ImageEntity imageEntity) {
        return imageEntity;
    }
}
