package transforms;

import utils.Image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract class that defines an interface for applying
 * transformation operations on an Image. Each Transform has a name
 * and an abstract method whose implementation must be overridden by a
 * subclass.  Plays the role of the "Abstract Class" in the Template
 * Method pattern and the role of the "Component" in the Decorator
 * pattern.
 */
public abstract class Transform {
    /**
     * Supported transformations (with class as value to use with
     * newInstance() creation).
     */
    public enum Type {
        GRAY_SCALE_TRANSFORM(transforms.GrayScaleTransform.class),
        TINT_TRANSFORM(transforms.TintTransform.class),
        SEPIA_TRANSFORM(transforms.SepiaTransform.class);

        private final Class<? extends Transform> mClazz;

        Type(Class<? extends Transform> clazz) {
            mClazz = clazz;
        }

        @Override
        public String toString() {
            return mClazz.getSimpleName();
        }
    }

    /**
     * The name of the transformation, which defaults to the 
     * "simple class name" of the subclass Transform instance.
     */
    protected String mName = getClass().getSimpleName();

    /**
     * Only available to Factory inner class to constructs a
     * Transform with the default name (simple class name).
     */
    protected Transform() {
    }

    /**
     * Only available to Factory inner class to constructs a
     * Transform with a custom name.
     */
    protected Transform(String name) {
        mName = name;
    }

    /**
     * This abstract hook method must be overridden by a subclass to
     * define the logic for processing the given {@code imageEntity}.
     */
    protected abstract Image applyTransform(Image imageEntity);

    /**
     * This template method calls the applyTransform() hook method (which
     * must be defined by a subclass) to transform the {@code imageEntity}
     * parameter and sets the transformName of the result to the name of
     * the transform.
     */
    public Image transform(Image image) {
        // Call the applyTransform() hook method.
        return applyTransform(image);
    }

    /**
     * Sets the name of the transform.
     */
    public void setName(String transformName) {
        mName = transformName;
    }

    /**
     * Gets the name of the transform.
     */
    public String getName() {
        return mName;
    }

    /**
     * Factory class used to create new instances of supported transforms
     * or to create list of new instances each supporting a specified
     * list of transforms.
     */
    public static final class Factory {
        private Factory() {
        }

        private static final Map<String, Class<? extends Transform>> mMap = new HashMap<>();

        /**
         * Registers a transform implementation class so that the factory 
         * can create a new instance when requested. No check is performed 
         * for a transform class being registered multiple times with different
         * names.
         *
         * @param name The name used as the map lookup key. If null, the
         *             class name is used.
         * @param clazz The class definition to register.
         */
        public void register(String name, Class<? extends Transform> clazz) {
            Class<? extends Transform> existingClazz = mMap.get(name);

            if (existingClazz != null) {
                throw new IllegalArgumentException(
                                                   "Transform name "
                                                   + name
                                                   + " is already used by class "
                                                   + existingClazz.getSimpleName());
            }

            mMap.put(name != null ? name : clazz.getSimpleName(), clazz);
        }

        /**
         * Removes a previously registered transform class from the factory map.
         * @param name Name that was used to register the transform that is now
         *             to be removed.
         */
        public void unregister(String name) {
            mMap.remove(name);
        }

        /**
         * Removes a previously registered transform class from the factory map.
         * @param clazz The transform class to unregister.
         */
        public void unregister(Class<? extends Transform> clazz) {
            if (!mMap.containsKey(clazz.getSimpleName())) {
                throw new IllegalArgumentException(
                                                   "Transform name "
                                                   + clazz.getSimpleName()
                                                   + " has not been registered in Factor map.");
            }

            mMap.remove(clazz.getSimpleName());
        }

        /**
         * Creates the specified {@code type} transform. Any images downloaded using
         * this transform will be saved in a folder that has the same name as the
         * transform class.
         *
         * @param name Name used to register the transform.
         * @return A new transform instance matching the specified name.
         */
        public static Transform newTransform(String name) {
            return newTransform(name, mMap.get(name));
        }

        /**
         * Creates list containing new instances of the requested transform types.
         *
         * @return List of new instances of the specified transform types.
         */
        public static List<Transform> newTransformsFromNames(List<String> names) {
            // Create and initialize transform instances.
            return names.stream()
                .map(name -> newTransform(name, mMap.get(name)))
                .collect(Collectors.toList());
        }

        /**
         * Creates an new instance of the specified {@code type} of transform. Any
         * images downloaded using this transform will be saved in a folder named
         * {@code name}.
         *
         * @param name The name of the transform.
         * @return A new transform instance of the specified class.
         */
        public static Transform newTransform(String name, Class<? extends Transform> clazz) {
            try {
                // Construct a new transform instance.
                Transform transform = clazz.getDeclaredConstructor().newInstance();
                transform.setName(name);
                return transform;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Creates the specified {@code type} transform. Any images downloaded using
         * this transform will be saved in a folder that has the same name as the
         * transform class.
         *
         * @param type Type of transform.
         * @return A transform instance of the specified type.
         */
        public static Transform newTransform(Type type) {
            return newTransform(type, null);
        }

        /**
         * Creates an new instance of the specified {@code type} of transform. Any
         * images downloaded using this transform will be saved in a folder named
         * {@code name}.
         *
         * @param type Type of transform.
         * @param name The name of a directory where all downloaded images will
         *             be saved, or null to let the transform decide..
         * @return A transform instance of the specified type.
         */
        public static Transform newTransform(Type type, String name) {
            try {
                // Construct a new transform instance.
                Transform transform = type.mClazz.getDeclaredConstructor().newInstance();

                // Set the transform name if a name was specified.
                if (name != null && !name.isEmpty()) {
                    transform.setName(name);
                }

                return transform;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Creates list containing new instances of the requested transform types.
         *
         * @return List of new instances of the specified transform types.
         */
        public static List<Transform> newTransforms(List<Type> types) {
            if (types == null) {
                // Gracefully accommodate an empty transform list.
                return new ArrayList<>();
            } else {
                // Create and initialize transform instances.
                return types.stream()
                    .map(type -> newTransform(type, null))
                    .collect(Collectors.toList());
            }
        }
    }
}
