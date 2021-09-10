import java.util.function.Supplier;

/**
 * This example of shows how the Java functional interfaces (including
 * Supplier and a custom functional interface) can be used in
 * conjunction with Java constructor references.
 */
public class CrDemo
       implements Runnable {
    /**
     * This class extends CrDemo and overrides its run() method to
     * uppercase the string.
     */
    private static class CrDemoEx
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

    /**
     * A field that stores a string.
     */
    String mString;

    /**
     * Main entry point into this program.
     */
    public static void main(String[] argv) {
        zeroParamConstructorRef();
        threeParamConstructorRef();
    }

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
     * Demonstrate how a Supplier can be used as a factory for a
     * zero-parameter constructor reference.
     */
    private static void zeroParamConstructorRef() {
        // Assign a constructor reference to a supplier that acts as a
        // factory for a zero-param object of CrDemo.
        Supplier<CrDemo> factory = CrDemo::new;

        // Use the factory to create a new instance of CrDemo and call
        // its run() method.
        CrDemo crDemo = factory.get();
        crDemo.run();
    }

    /**
     * Demonstrate how Suppliers can be used as factories for multiple
     * zero-parameter constructor references.
     */
    private static void zeroParamConstructorRefEx() {
        // Assign a constructor reference to a supplier that acts as a
        // factory for a zero-param object of CrDemo.
        Supplier<CrDemo> crDemoFactory = CrDemo::new;

        // Assign a constructor reference to a supplier that acts as a
        // factory for a zero-param object of CrDemoEx.
        Supplier<CrDemoEx> crDemoFactoryEx = CrDemoEx::new;

        // This helper method invokes the given supplier to create a
        // new object and call its run() method.
        runDemo(crDemoFactory);
        runDemo(crDemoFactoryEx);
    }

    /**
     * Use the given {@code factory} to create a new object and call
     * its run() method.
     */
    private static <T extends Runnable> void runDemo(Supplier<T> factory) {
        factory.get().run();
    }

    /**
     * Print the value of mString.
     */
    @Override
    public void run() {
        System.out.println(mString);
    }

    /**
     * Demonstrate how a custom functional interface (i.e., TriFactor,
     * which is defined below) can be used as a factory for a
     * three-parameter constructor reference.
     */
    private static void threeParamConstructorRef() {
        // Assign a constructor reference to a customized functional
        // interface that acts as a factory to create a
        // three-parameter constructor for CrDemo.
        TriFactory<String, Integer, Long, CrDemo> factory = 
            CrDemo::new;

        // Use the factory to create a new instance of CrDemo and call
        // its run() method.
        factory.of("The answer is ", 4, 2L).run();
    }

    /**
     * Represents a factory that accepts three arguments and produces
     * a result.  This is a functional interface whose functional
     * method is {@link #of(Object, Object, Object)}.
     */
    @FunctionalInterface
    interface TriFactory<P1,
                         P2,
                         P3,
                         R> {
        /**
         * Create an object of type {@code R} using the three
         * parameters and return the object.
         */
        R of(P1 p1, P2 p2, P3 p3);
    }
} 

