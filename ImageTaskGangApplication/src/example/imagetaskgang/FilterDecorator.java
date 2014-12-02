package example.imagetaskgang;

/**
 * @class FilterDecorator
 *
 * @brief Allows the addition of behavior to an object dynamically
 *        without affecting the behavior of other objects from the
 *        same class.  Plays the role of the "Decorator" in the
 *        Decorator pattern.
 */
public abstract class FilterDecorator extends Filter {
    /**
     * The Filter that's being decorated.
     */
    protected Filter mFilter;

    /**
     * Constructor initializes superclass and data member with the
     * given @a filter.
     */
    public FilterDecorator(Filter filter) {
        super(filter.getName());
        mFilter = filter;
    }

    /**
     * This hook method forwards to the decorated filter to filter the
     * @a inputEntity parameter.
     */
    @Override
    protected InputEntity applyFilter(InputEntity inputEntity) {
        return decorate(mFilter.filter(inputEntity));
    }
    
    /**
     * An abstract hook method that "decorates" the data member
     * filter, which is applied to the inputEntity after it's been
     * filtered.
     */
    protected abstract InputEntity decorate(InputEntity inputEntity);
}
