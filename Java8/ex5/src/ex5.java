import java.util.*;

/**
 * This example shows how a Java 8 Consumer interface can be used to
 * print out the values in a list.
 */
public class ex5 {
    static public void main(String[] argv) {
        List<Thread> threads =
            new ArrayList<>(Arrays.asList(new Thread("Larry"),
                                          new Thread("Curly"),
                                          new Thread("Moe")));
        threads.forEach(System.out::println);

        // Sort the threads by their names.
        threads.sort(Comparator.comparing(Thread::getName));

        threads.forEach(System.out::println);
    }
}

