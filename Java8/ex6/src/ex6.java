import java.util.*;

/**
 * This example shows how a Java 8 Supplier interface can be used to
 * print a default value.
 */
public class ex6 {
    static public void main(String[] argv) {
        Map<String, String> personMap = new HashMap<String, String>() {
            { put("Demon", "Naughty");
              put("Angel", "Nice"); 
            } 
        };

        // The person to search for (who is not in the map).
        String person = "Demigod";

        // Try to find the person in the map (of course, they won't be
        // there).
        Optional<String> disposition = 
            Optional.ofNullable(personMap.get(person));

        System.out.println("disposition of "
                           + person + " = "
                           // Pass a Supplier lambda expression that
                           // returns a default value if the person is
                           // not found.
                           + disposition.orElseGet(() -> "unknown"));
    }
}

