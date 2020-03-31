import java.util.*;

/**
 * This example shows the use of a simple lambda expression in the
 * context of a Java List/ArrayList removeIf() method.
 */
public class ex2 {
    static public void main(String[] argv) {
        List<Integer> list = 
            new ArrayList<>(List.of(1, 2, 3, 4, 5));

        System.out.println(list);
        
        // This lambda expression removes the even numbers from the
        // list.
        list.removeIf(i -> i % 2 == 0);
        
        System.out.println(list);
    }
}

