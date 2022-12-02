package transforms;

import utils.Image;

/**
 * A Transform subclass that converts a downloaded image to sepia
 * tones.  It plays the role of the "Concrete Component" in the
 * Decorator pattern and the "Concrete Class" in the Template Method
 * pattern.
 */
public class SepiaTransform
        extends Transform {

    /**
     * Only available to Factory inner class to constructs a
     * Transform with the default name (simple class name).
     */
    protected SepiaTransform() {
    }

    /**
     * Constructors. See GrayScaleTransform for
     * explanation of transform naming.
     */
    public SepiaTransform(String name) {
        super(name);
    }

    /**
     * Constructs a new Image that does not change the original at all.
     */
    @Override
    protected Image applyTransform(Image image) {
        return image.applyTransform(Type.SEPIA_TRANSFORM);
    }
}
