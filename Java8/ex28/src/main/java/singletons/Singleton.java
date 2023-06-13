package singletons;

/**
 * This interface defines setter and getter methods for a singleton.
 */
public interface Singleton<T> {
    /**
     * @return The contents of a field
     */
    T getField();

    /**
     * Set the contents of a field and return the old contents.
     *
     * @param f The new contents of the field
     * @return The old contents of the field
     */
    T setField(T f);
}
