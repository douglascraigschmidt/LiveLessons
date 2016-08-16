package livelessons.filters;

import utils.Image;

/**
 * Command object that associates a filter with an image.
 */
public class FilterDecoratorWithImage {
    /**
     * The filter decorator.
     */
    public FilterDecorator mFilterDecorator;

    /**
     * The image.
     */
    public Image mImage;

    /**
     * Constructor initializes the fields.
     */
    public FilterDecoratorWithImage(FilterDecorator filterDecorator,
                                    Image image) {
        mFilterDecorator = filterDecorator;
        mImage = image;
    }

    /**
     * Run the filter decorator on the image.
     */
    public Image run() {
        return mFilterDecorator.filter(mImage);
    }
}
