package edu.vandy.lockmanager;

/**
 * Define an object that clients can use as a lock in a
 * distributed system.
 */
public class Lock {
    /**
     * The identity of the {@link Lock}
     */
    public String id;

    /**
     * A default constructor is needed for HTTP interface.
     */
    public Lock() {}

    /**
     * Initialize the {@link Lock} with an id.
     *
     * @param id The identity of the {@link Lock}
     */
    public Lock(String id) {
        this.id = id;
    }

    /**
     * @return A printable representation of the {@link Lock}
     */
    @Override
    public String toString() {
       return "palantir#" + id;
    }
}