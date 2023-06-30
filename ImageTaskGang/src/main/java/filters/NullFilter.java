package filters;

import utils.Image;

/**
 * The {@link NullFilter} will return the {@link Image} as it was
 * downloaded.  Its purpose is to show the original {@link Image}, as
 * well as to exemplify how filters are supposed to work on a basic
 * level.
 *
 * {@link NullFilter} plays the role of the "Concrete Component" in
 * the Decorator pattern and the "Concrete Class" in the Template
 * Method pattern.
 */
public class NullFilter 
       extends Filter {
    /**
     * Default constructor is a no-op.
     */
    public NullFilter() {}
    
    /**
     * Constructors for the {@link NullFilter}. See {@link
     * GrayScaleFilter} for explanation of {@link Filter} naming.
     */
    public NullFilter(String name) {
        super(name);
    }
	
    /**
     * @return A new {@link Image} that does not change the original
     * at all
     */
    @Override
    protected Image applyFilter(Image image) {
        return image;
    }
}
