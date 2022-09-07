import java.util.function.Supplier;

public class CrDemo
       implements Runnable {
    /**
     * A field that stores a string.
     */
    String mString;

    /**
     * Default constructor.
     */
    CrDemo() {
        // Assign the field to "hello".
        mString = "hello";
    }

    /**
     * A three-parameter constructor.
     */
    private CrDemo(String s, Integer i, Long l) {
        // Assign the field to the values of the parameters.
        mString = s + i + l;
    }

    /**
     * Print the value of mString.
     */
    @Override
    public void run() {
        System.out.println(mString);
    }
} 

