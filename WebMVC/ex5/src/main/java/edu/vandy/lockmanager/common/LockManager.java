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

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LockManager other) {
            return this.name.equals(other.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
