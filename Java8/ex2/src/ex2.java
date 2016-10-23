import java.util.*;

/**
 * ...
 */
public class ex2 {
    static public void main(String[] argv) {
        List<Integer> list = 
            new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5));

        System.out.println(list);
        
        // Remove the even numbers from the list.
        list.removeIf(i -> i % 2 == 0);
        
        System.out.println(list);
    }
}

