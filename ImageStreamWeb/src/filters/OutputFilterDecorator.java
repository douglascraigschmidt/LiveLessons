package filters;

import example.ImageEntity;
import example.PlatformStrategy;

/**
 * @class OutputFilterDecorator
 *
 * @brief A Decorator that applies the filter passed to its
 *        constructor and then writes the results to an output file.
 *        Plays the role of the "Concrete Decorator" in the Decorator
 *        pattern.
 */
public class OutputFilterDecorator extends FilterDecorator {
    /**
     * Constructs the filter decorator with the @a filter to apply.
     */
    public OutputFilterDecorator(Filter filter) {
    	super(filter);
    }

    /**
     * The hook method that is called on the ImageEntity once it has
     * been filtered with mFilter.  This method stores the filtered
     * ImageEntity in a file by delegating the storing to the
     * platform- specific implementation of storeImage(...).
     */
    @Override
    protected ImageEntity decorate(ImageEntity imageEntity) {
    	// Store the filtered image as its filename (which is derived
        // from its URL), within the appropriate filter directory to
        // organize the filtered results and write the image to 
    	// the file in the appropriate directory.
        PlatformStrategy.instance()
        	.storeExternalImage(this.getName(),
        						imageEntity.getFileName(), 
        						imageEntity.getImage());

        return imageEntity;
    }
}
