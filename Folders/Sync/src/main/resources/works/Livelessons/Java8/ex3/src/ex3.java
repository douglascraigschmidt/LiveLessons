import java.util.*;

/**
 * This example uses a Java Function-based method reference to sort a
 * list of threads by their names.  It also demonstrates the use of
 * the Comparator.comparing() factory method.
 */
public class ex3 {
    static public void main(String[] argv) {
        // Create a list of threads.
        List<Thread> threads =
                Arrays.asList(new Thread("Larry"),
                              new Thread("Curly"),
                              new Thread("Moe"));

        // Print out the list in unsorted order.
        System.out.println(threads);

        // Sort the threads by their names.
        threads.sort(Comparator.comparing(Thread::getName));

        // Print out the list in sorted order.
        System.out.println(threads);
    }
}

