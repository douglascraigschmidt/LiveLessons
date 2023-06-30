package filters;

import utils.Image;

/**
 * Allows the addition of behavior to a {@link Filter} object
 * transparently without affecting the behavior of other {@link
 * Filter} objects it encapsulates.
 *
 * {@link FilterDecorator} plays the role of the "Decorator" in the
 * Decorator pattern and the role of the "Abstract Class" in the
 * Template Method pattern.
 */
public abstract class FilterDecorator 
       extends Filter {
    /**
     * The {@link Filter} that's being decorated.
     */
    protected Filter mFilter;

    /**
     * Constructor initializes superclass and data member with the
     * given {@link Filter}.
     */
    public FilterDecorator(Filter filter) {
        super(filter.getName());
        mFilter = filter;
    }

    /**
     * An abstract hook method that "decorates" the {@link Filter}
     * data member by being applied to the {@link Image} after it's
     * been filtered (must be supplied by a subclass).
     */
    protected abstract Image decorate(Image imageEntity);

    /**
     * This hook method is also a template method that forwards to the
     * decorated filter to filter the {@link Image} parameter.
     *
     * @return the filtered {@link Image}
     */
    @Override
    protected Image applyFilter(Image image) {
        return decorate(mFilter.filter(image));
    }
}
