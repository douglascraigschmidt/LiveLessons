package filters;

import utils.Image;

/**
 * A command object that associates a {@link Filter{} with an {@link
 * Image}.
 *
 * {@link FilterDecoratorWithImage} plays the role of the "Command" in
 * the Command pattern.
 */
public class FilterDecoratorWithImage {
    /**
     * The {@link Filter} decorator.
     */
    public FilterDecorator mFilterDecorator;

    /**
     * The {@link Image}.
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
     * Run the {@link Filter} decorator on the {@link Image}.
     */
    public Image run() {
        return mFilterDecorator.filter(mImage);
    }
}
