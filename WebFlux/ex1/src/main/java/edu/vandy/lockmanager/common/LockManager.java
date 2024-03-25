package edu.vandy.lockmanager.common;

import java.util.Objects;

/**
 * This class is used to keep track of allocated {@link LockManager}
 * objects.
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
     * @return The unique name of the {@link LockManager}
     *
    public String getName() {
        return mName;
    }
    */

    /**
     * Set the unique name of the {@link LockManager}.
     *
     * @param name The unique name of the {@link LockManager}
     */
    public LockManager(String name) {
        this.name = name;
    }

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
        return object instanceof LockManager other
            && Objects.equals(this.name, other.name);
    }

    /**
     * @return A hash of the {@link LockManager} {@code name}
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
