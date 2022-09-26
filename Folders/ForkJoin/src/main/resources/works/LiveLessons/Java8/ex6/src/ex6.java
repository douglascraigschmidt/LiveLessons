import java.util.*;
import java.util.function.Supplier;

/**
 * This example shows how a Java {@link Supplier} interface can be
 * used in conjunction with the Java {@link Optional} class to print a
 * default value if a key is not found in a {@link Map}.
 */
public class ex6 {
    /**
     * The main entry point into the Java program.
     */
    static public void main(String[] argv) {
        // Create a HashMap that associates beings with their
        // personas.
        Map<String, String> beingMap = new HashMap<String, String>() {
                { 
                    put("Demon", "Naughty");
                    put("Angel", "Nice");
                    put("Wizard", "Wise");
                } 
            };

        beingMap
            // Display the contents of the Map.
            .forEach(ex6::printDisposition);

        // The being to search for (who is not in the map).
        String being = "Demigod";

        // Try to find the being in the Map.  Since they won't be
        // there an empty Optional will be returned from ofNullable().
        Optional<String> disposition = 
            Optional.ofNullable(beingMap.get(being));

        printDisposition(being,
                         // Pass a Supplier lambda expression that
                         // returns a default value if the being is
                         // not found.
                         disposition.orElseGet(() -> "unknown"));
    }

    /**
     * Print the {@code disposition} associated with the {@code
     * being}.
     *
     * @param being The being
     * @param disposition The being's disposition
     */
    private static void printDisposition(String being,
                                         String disposition) {
        System.out.println("disposition of "
                           + being + " = "
                           + disposition);
    }
}

