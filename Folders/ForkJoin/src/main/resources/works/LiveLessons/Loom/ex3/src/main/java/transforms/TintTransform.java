package transforms;

import utils.Image;

/**
 * A Transform subclass that converts a downloaded image using a
 * redish tint.  It plays the role of the "Concrete Component" in the
 * Decorator pattern and the "Concrete Class" in the Template Method
 * pattern.
 */
public class TintTransform
        extends Transform {

    /**
     * Only available to Factory inner class to constructs a
     * Transform with the default name (simple class name).
     */
    protected TintTransform() {
    }

    /**
     * Constructors for the transform. See GrayScaleTransform for
     * explanation of transform naming.
     */
    public TintTransform(String name) {
        super(name);
    }

    /**
     * Constructs a new Image that does not change the original at all.
     */
    @Override
    protected Image applyTransform(Image image) {
        return image.applyTransform(Type.TINT_TRANSFORM);
    }
}
