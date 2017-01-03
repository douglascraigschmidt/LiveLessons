import java.util.*;

/**
 * This example program shows the use of a simple lambda expression in
 * the context of a Java ArrayList.
 */
public class ex2 {
    static public void main(String[] argv) {
        List<Integer> list = 
            new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        System.out.println(list);
        
        // This lambda expression removes the even numbers from the
        // list.
        list.removeIf(i -> i % 2 == 0);
        
        System.out.println(list);
    }
}

