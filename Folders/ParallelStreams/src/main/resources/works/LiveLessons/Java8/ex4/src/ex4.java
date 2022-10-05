import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;
import static javax.swing.UIManager.put;

/**
 * This example shows how a modern Java {@link BiFunction} lambda can
 * be used to replace all the values of all keys in a {@link
 * ConcurrentHashMap}.  It also contrasts the modern Java {@link
 * BiFunction} with a conventional Java 7 solution using a for-each
 * loop.
 */
public class ex4 {
    /**
     * The main entry point into the Java program.
     */
    static public void main(String[] argv) {
        // Create a map that associates Stooges with IQ points.
    	Map<String, Integer> stoogeMap =
            new ConcurrentHashMap<String, Integer>() {
                {
                    put("Larry", 100);
                    put("Curly", 90);
                    put("Moe", 110);
                }
            };

    	System.out.println(stoogeMap);

        // Replace all values of all keys using a Java 7 for-each loop.
        for (Map.Entry<String, Integer> entry : stoogeMap.entrySet())
            entry.setValue(entry.getValue() - 50);

        System.out.println(stoogeMap);

    	// Replace all values of all keys using a modern Java BiFunction
    	// lambda.
    	stoogeMap.replaceAll((k, v) -> v - 50);

    	System.out.println(stoogeMap);
    }
}

