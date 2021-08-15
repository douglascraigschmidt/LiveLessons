import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

/**
 * This example shows the use of a predicate lambda expressions in the
 * context of a Java ConcurrentHashMap removeIf() method.
 */
public class ex10 {
    /**
     * This factory method creates a ConcurrentHashMap containing the names of Stooges and their IQs.
     */
    static private Map<String, Integer> makeMap() {
       return new ConcurrentHashMap<String, Integer>()  {
          {
            put("Larry", 100);
            put("Curly", 90);
            put("Moe", 110);
          }
        };
    }

    /**
     * Demonstrate the use of predicate lambda expressions.
     */
    static public void main(String[] argv) {
        // Create a map that associates Stooges with their IQ levels.
        Map<String, Integer> iqMap = makeMap();

        System.out.println(iqMap);
        // This lambda expression removes entries with IQ less than or
        // equal to 100.
        iqMap.entrySet().removeIf(entry -> entry.getValue() <= 100);
        System.out.println(iqMap);

        iqMap = makeMap();
        System.out.println(iqMap);

        // Create two predicate objects.
        Predicate<Map.Entry<String, Integer>> lowIq = entry -> entry.getValue() <= 100;
        Predicate<Map.Entry<String, Integer>> curly = entry -> entry.getKey().equals("Curly");

        // This lambda expression removes entries with IQ less than or
        // equal to 100 with the name "curly".
        iqMap.entrySet().removeIf(lowIq.and(curly));
        
        System.out.println(iqMap);
    }
}

