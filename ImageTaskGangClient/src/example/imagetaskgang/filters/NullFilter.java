package example.imagetaskgang.filters;

import example.imagetaskgang.ImageEntity;

/**
 * @class NullFilter
 *
 * @brief The NullFilter will return the image as it was downloaded.
 *        It's purpose is to show the original image, as well as to
 *        exemplify how filters are supposed to work on a basic level.
 *        It plays the role of the "Concrete Component" in the
 *        Decorator pattern and the "Concrete Class" in the Template
 *        Method pattern.
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
