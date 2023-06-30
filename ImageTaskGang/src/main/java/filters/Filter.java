package filters;

import utils.Image;

/**
 * An abstract class that defines an interface for applying filtering
 * operations to an {@link Image}. Each {@link Filter} has a name and
 * an abstract method whose implementation must be overridden by a
 * subclass.  
 * 
 * {@link Filter} plays the role of the "Abstract Class" in the
 * Template Method pattern and the role of the "Component" in the
 * Decorator pattern.
 */
public abstract class Filter {
    /**
     * Debug logging tag.
     */
    protected static final String TAG = "Filter";

    /**
     * The name of the {@link Filter}, which defaults to the
     * "canonical name" of the subclass {@link Filter} instance.
     */
    protected String mName;

    /**
     * Constructs the {@link Filter} with the default name.
     */
    public Filter() {
        // Default uses the class name without the package prefix.
    	mName = this.getClass().getSimpleName();
    }

    /**
     * Constructs the {@link Filter} with a custom name.
     */
    public Filter(String filterName) {
        mName = filterName;
    }

    /**
     * This abstract hook method must be overridden by a subclass to
     * define the logic for processing the given {@link Image}.
     */
    protected abstract Image applyFilter(Image imageEntity);

    /**
     * This template method calls the {@code applyFilter()} hook
     * method (which must be defined by a subclass) to filter the
     * {@link Image} parameter and sets the {@code filterName} of the
     * result to the name of the filter.
     */
    public Image filter(Image image) {
        // Call the applyFilter() hook method.
        Image filteredResult = applyFilter(image);
        filteredResult.setFilterName(this);
        return filteredResult;
    }

    /**
     * Sets the name of the {@link Filter}.
     */
    public void setName(String filterName) {
        mName = filterName;
    }

    /**
     * @return The name of the {@link Filter}
     */
    public String getName() {
        return mName;
    }
}
