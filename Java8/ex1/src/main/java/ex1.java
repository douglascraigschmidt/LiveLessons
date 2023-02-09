import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * This example shows how to use Java lambda expressions and method
 * references to create a closure, as well as to sort elements in a
 * collection using a Java anonymous inner class, lambda expression,
 * and method reference.  It also shows how to use the modern Java
 * forEach() method for Streams and collections.
 */
public class ex1 {
    /**
     * This class demonstrates how to implement closures using modern Java.
     */
    static class ClosureExample {
        /**
         * A private field that can be updated by the closure below.
         */
        private int mRes;

        /**
         * This factory method creates a closure that will run in a
         * background thread.
         *
         * @return The background thread reference
         */
        Thread makeThreadClosure(String string, int n) {
            // Create and return a new Thread whose runnable lambda
            // expression defines a closure that reads the method
            // parameters and updates the mRes field.
            return new Thread(() ->
                              System.out.println(string + (mRes += n)));
        }

        /**
         * The constructor creates/starts/runs a thread closure.
         */
        ClosureExample() throws InterruptedException {
            // Create a thread closure.
            Thread t = makeThreadClosure("result = ", 10);

            // Start the thread.
            t.start();

            // Join when the thread is finished.
            t.join();
        }
    }

    /**
     * The array to sort and print.
     */
    private static final String[] sNameArray = {
            "Barbara",
            "James",
            "Mary",
            "John",
            "Robert",
            "Michael",
            "Linda",
            "james",
            "mary"
    };

    /**
     * This {@link Supplier} makes a copy of an array.
     */
    private static final Supplier<String[]> sArrayCopy =
        () -> Arrays.copyOf(sNameArray, sNameArray.length);

    /**
     * This method provides the entry point into this test program.
     */
    static public void main(String[] argv) throws InterruptedException {
        // First demonstrates the closure example.
        new ClosureExample();

        // Next demonstrate various techniques for sorting/printing an array.
        System.out.println("Original array:\n"
                           + List.of(sNameArray));
        showInnerClass(sArrayCopy.get());
        showLambdaExpression(sArrayCopy.get());
        showMethodReference1(sArrayCopy.get());
        showMethodReference2(sArrayCopy.get());
    }

    /**
     * Show how to sort using an anonymous inner class.
     */
    private static void showInnerClass(String[] nameArray) {
        System.out.println("showInnerClass()");

        // Sort using an anonymous inner class.
        Arrays.sort(nameArray, new Comparator<String>() {
                public int compare(String s, String t) {
                    return s.toLowerCase().compareTo(t.toLowerCase());
                }
            });

        // Print out the sorted contents as an array.
        System.out.println(List.of(nameArray));
    }

    /**
     * Show how to sort using a lambda expression.
     */
    private static void showLambdaExpression(String[] nameArray) {
        System.out.println("showLambdaExpression()");

        // Sort using a lambda expression.
        Arrays.sort(nameArray,
                    // Note type deduction here:
                    (s, t) -> s.compareToIgnoreCase(t));

        // Print out the sorted contents as an array.
        System.out.println(List.of(nameArray));
    }

    /**
     * Show how to sort using a method reference.
     */
    private static void showMethodReference1(String[] nameArray) {
        System.out.println("showMethodReference1()");

        // Sort using a method reference.
        Arrays.sort(nameArray,
                    String::compareToIgnoreCase);

        // Print out the sorted contents using the modern Java Stream
        // forEach() method.
        Stream.of(nameArray).forEach(System.out::print);
    }

    /**
     * Show how to sort using a method reference.
     */
    private static void showMethodReference2(String[] nameArray) {
        System.out.println("\nshowMethodReference2()");

        // Sort using a method reference.
        Arrays.sort(nameArray,
                    String::compareToIgnoreCase);

        // Print out the sorted contents using the modern Java Iterable
        // forEach() method.
        List.of(nameArray).forEach(System.out::print);
    }
}

