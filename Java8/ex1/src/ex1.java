import java.util.*;
import java.util.stream.Stream;

/**
 * This example shows how to use Java 8 lambda expressions and method
 * references to sort elements of a collection.  It also shows how to
 * use the Java 8 forEach() method.
 */
public class ex1 {
    /**
     * This class demonstrates how to implement closures using Java 8.
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
            // Create and return a new thread whose runnable lambda
            // expression defines a closure that reads the parameters
            // and updates the mRes field.
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

    static public void main(String[] argv) throws InterruptedException {
        // Run the closure example.
         new ClosureExample();

        // The array to sort and print.
        String[] nameArray = {
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

        System.out.println("Original array:\n"
                           + Arrays.asList(nameArray));

        // Demonstrate the various techniques.
        showInnerClass(nameArray);
        showLambdaExpression(nameArray);
        showMethodReference1(nameArray);
        showMethodReference2(nameArray);
    }

    /**
     * Show how to sort using an anonymous inner class.
     */
    private static void showInnerClass(String[] nameArray) {
        System.out.println("showInnerClass()");

        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        // Sort using an anonymous inner class.
        Arrays.sort(nameArrayCopy, new Comparator<String>() {
                public int compare(String s,String t) { 
                    return s.toLowerCase().compareTo(t.toLowerCase()); 
                }
            });

        // Print out the sorted contents as an array.
        System.out.println(Arrays.asList(nameArrayCopy));
    }

    /**
     * Show how to sort using a lambda expression.
     */
    private static void showLambdaExpression(String[] nameArray) {
        System.out.println("showLambdaExpression()");

        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        // Sort using a lambda expression.
        Arrays.sort(nameArrayCopy,
                    // Note type deduction here:
                    (s, t) -> s.compareToIgnoreCase(t));

        // Print out the sorted contents as an array.
        System.out.println(Arrays.asList(nameArrayCopy));
    }

    /**
     * Show how to sort using a method reference.
     */
    private static void showMethodReference1(String[] nameArray) {
        System.out.println("showMethodReference1()");

        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        // Sort using a method reference.
        Arrays.sort(nameArrayCopy,
                    String::compareToIgnoreCase);

        // Print out the sorted contents using the Java 8 forEach()
        // method.
        Stream.of(nameArrayCopy).forEach(System.out::print);
    }

    /**
     * Show how to sort using a method reference.
     */
    private static void showMethodReference2(String[] nameArray) {
        System.out.println("\nshowMethodReference2()");

        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        // Sort using a method reference.
        Arrays.sort(nameArrayCopy,
                    String::compareToIgnoreCase);

        // Print out the sorted contents using the Java 8 forEach()
        // method.
        Arrays.asList(nameArrayCopy).forEach(System.out::print);
    }
}

