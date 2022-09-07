/**
 * This class extends {@link CrDemo} and overrides its {@code run()}
 * method to uppercase the string.
 */
public class CrDemoEx
       extends CrDemo {
    /**
     * Constructor.
     */
    CrDemoEx() {
        super();
    }

    /**
     * Print the upper-cased value of mString.
     */
    @Override
    public void run() {
        System.out.println(mString.toUpperCase());
    }
}

