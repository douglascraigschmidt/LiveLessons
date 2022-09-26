/**
 * This class implements {@link Runnable} and overrides its {@code run()}
 * method to uppercase a {@link String} field.
 */
public class CrDemoEx
       implements Runnable {
    /**
     * A field that stores a string.
     */
    String mString;

    /**
     * Constructor.
     */
    CrDemoEx() {
        // Assign the field to the value "hello".
        mString = "hello";
    }

    /**
     * Print the upper-cased value of mString.
     */
    @Override
    public void run() {
        System.out.println(mString.toUpperCase());
    }
}

