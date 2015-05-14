package filters;

import example.ImageEntity;

/**
 * @class Filter
 *
 * @brief An abstract class that defines a means to apply filtering
 *        operations to an Image. It has a name and an abstract Filter
 *        method whose implementation must be overridden by a
 *        subclass.  Plays the role of the "Abstract Class" in the
 *        Template Method pattern and the role of the "Component" in
 *        the Decorator pattern.
 */
public abstract class Filter {
    /**
     * The name of the filter. Defaults to the Canonical Name of the
     * derived filter.
     */
    protected String mName;

    /**
     * Constructs the filter with the default name.
     */
    public Filter() {
    	String baseName = this.getClass().getCanonicalName();
        mName = baseName.substring(baseName.lastIndexOf(".") + 1);
    }

    /**
     * Constructs the filter with a custom name.
     */
    public Filter(String name) {
        mName = name;
    }

    /**
     * Sets the name of the filter.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Gets the name of the filter.
     */
    public String getName() {
        return mName;
    }

    /**
     * This template method calls the applyFilter() hook method (which
     * must be defined by a subclass) to filter the @a imageEntity
     * parameter and sets the filterName of the result to the name of
     * the filter.
     */
    public ImageEntity filter(ImageEntity imageEntity) {
        // Call the applyFilter() hook method.
        ImageEntity filteredResult = applyFilter(imageEntity);
        filteredResult.setFilterName(this);
        return filteredResult;
    }

    /**
     * This abstract hook method must be overridden by a subclass to
     * define the logic for processing the given @a imageEntity.
     */
    protected abstract ImageEntity applyFilter(ImageEntity imageEntity);
}
