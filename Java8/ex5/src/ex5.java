import java.util.*;

/**
 * ...
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

