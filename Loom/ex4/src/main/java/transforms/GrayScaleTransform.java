package transforms;

import utils.Image;

/**
 * A Transform subclass that converts a downloaded image to grayscale.
 * It plays the role of the "Concrete Component" in the Decorator
 * pattern and the "Concrete Class" in the Template Method pattern.
 */
public class GrayScaleTransform
       extends Transform {
    /**
     * Only available to Factory inner class to constructs a Transform
     * with the default name (simple class name).
     */
    protected GrayScaleTransform() {
        // Default uses the class name without the package prefix.
        mName = this.getClass().getSimpleName();
    }

    /**
     * Constructs a Grayscale transform with the given name. This
     * constructor can be used to specify the output directory the
     * grayscale transformed images should be stored in. This naming
     * functionality would also be useful for transforms to which
     * parameters are passed. For example, a 5x5 box transform and a
     * 3x3 box transform could reuse identical code, but be stored in
     * different directories after processing.
     */
    public GrayScaleTransform(String name) {
        super(name);
    }

    /**
     * Uses the common color transformation values for grayscale
     * conversion using a pixel-by-pixel coloring algorithm.
     */
    @Override
    protected Image applyTransform(Image image) {
        // Forward to the platform-specific implementation of this transform.
        return image.applyTransform(Type.GRAY_SCALE_TRANSFORM);
    }
}
