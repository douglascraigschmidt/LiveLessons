import java.util.*;

/**
 * This example shows how a Java Supplier interface can be used to
 * print a default value if a key is not found in a map.  It also
 * shows how to use the Java Optional class.
 */
public class ex6 {
    static public void main(String[] argv) {
        // Create a HashMap that associates beings with their
        // personas.
        Map<String, String> beingMap = new HashMap<String, String>() {
                { put("Demon", "Naughty");
                  put("Angel", "Nice"); 
                } 
            };

        // The being to search for (who is not in the map).
        String being = "Demigod";

        // Try to find the being in the map (of course, they won't be
        // there).
        Optional<String> disposition = 
            Optional.ofNullable(beingMap.get(being));

        System.out.println("disposition of "
                           + being + " = "
                           // Pass a Supplier lambda expression that
                           // returns a default value if the being is
                           // not found.
                           + disposition.orElseGet(() -> "unknown"));
    }
}

