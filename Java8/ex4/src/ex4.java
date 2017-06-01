import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static javax.swing.UIManager.put;

/**
 * This example shows how a Java 7 BiFunction lambda can be used to
 * replace all the values of all keys in a ConcurrentHashMap.  It also
 * contrasts the Java 8 BiFunction with a conventional Java 7 solution
 * using a foreach loop.
 */
public class ex4 {
    static public void main(String[] argv) {
        // Create a map that associates Stooges with IQ points.
    	Map<String, Integer> iqMap = 
            new ConcurrentHashMap<String, Integer>() {
                { 
                    put("Larry", 100);
                    put("Curly", 90);
                    put("Moe", 110);
                }
            };

    	System.out.println(iqMap);
    		 
    	// Replace all values of all keys using a Java 8 BiFunction
    	// lambda.
    	iqMap.replaceAll((k, v) -> v - 50);

    	System.out.println(iqMap);

    	// Replace all values of all keys using a Java 7 foreach loop.
    	for (Map.Entry<String, Integer> entry : iqMap.entrySet())
            entry.setValue(entry.getValue() - 50);
    	
    	System.out.println(iqMap);
    }
}

