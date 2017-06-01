import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * This example shows the use of a simple lambda expression in the
 * context of a Java ConcurrentHashMap removeIf() method.
 */
public class ex10 {
    static public void main(String[] argv) {
        // Create a map that associates Stooges with IQ levels.
        ConcurrentMap<String, Integer> iqMap =
            new ConcurrentHashMap<String, Integer>() {
                {
                    put("Larry", 100);
                    put("Curly", 90);
                    put("Moe", 110);
                }
            };

        System.out.println(iqMap);
        
        // This lambda expression removes entries with IQ less than or
        // equal to 100.
        iqMap.entrySet().removeIf(entry -> entry.getValue() <= 100);
        
        System.out.println(iqMap);
    }
}

