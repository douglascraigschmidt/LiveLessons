package singletons;

/**
 * This class implements the Singleton pattern with an enum.  See
 * <a href="https://javarevisited.blogspot.com/2012/07/why-enum-singleton-are-better-in-java.html">
 * this link</a> for more information about this approach.
 */
public enum SingletonE
       implements Singleton<String> {
    /**
     * The one-and-only instance of this enum.
     */
    INSTANCE;

    /**
     * Define a non-static field.
     */
    private String mField;

    /**
     * Private constructor prevents instantiation from outside the
     * class.
     */
    private SingletonE() {
        System.out.println(Thread.currentThread().getName()
            + ": creating SingletonE");
    }

    /**
     * @return The value of the field
     */
    public String getField() {
        return mField;
    }

    /**
     * Set the contents of a field and return the old contents.
     *
     * @param field The new contents of the field
     * @return The old contents of the field
     */
    public String setField(String field) {
        String oldField = mField;
        mField = field;
        return oldField;
    }

    /**
     * The static instance() method from the Singleton pattern.
     */
    public static SingletonE instance() {
        return INSTANCE;
    }
}
