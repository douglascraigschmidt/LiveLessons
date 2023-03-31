package edu.vandy.lockmanager.common;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * This class is used to keep track of allocated
 * {@link ArrayBlockingQueue} objects.
 */
public class LockManager {
    /**
     * The unique name of the {@link LockManager}.
     */
    public String name;

    /**
     * The number of permits in this {@link LockManager}.
     */
    public Integer permitCount;

    /**
     * Set the unique name of the {@link LockManager}.
     *
     * @param name The unique name of this {@link LockManager}
     */
    public LockManager(String name) {
        this.name = name;
    }

    /**
     * Constructor initializes the name of the {@link LockManager}.
     *
     * @param name The unique name of this {@link LockManager}
     * @param permitCount The number of permits managed
     */
    public LockManager(String name,
                       Integer permitCount) {
        this.name = name + ":[" + permitCount + "]";
    }

    /**
     * This class needs a default constructor.
     */
    LockManager() {
        name = "default";
    }

    /**
     * @return A {@link String} representation
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Overrides the {@code equals()} method to compare two {@link
     * LockManager} objects based on their {@code name}.
     *
     * @param object The other {@link Object} to compare with this
     *               object
     * @return true if the object names are equal, false otherwise
     */
    @Override
    public boolean equals(Object object) {
        // Fun use of a recent Java feature.
        return object instanceof LockManager other
            && this.name.equals(other.name);
    }

    /**
     * @return A hash of the {@link LockManager} {@code name}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
