import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example two zap() method implementations that remove strings
 * from a list of strings.  One method uses basic Java 7 features and
 * the other uses basic Java 8 features.
 */
public class ex0 {
    static public void main(String[] argv) {
        // The array of names.
        String[] nameArray = {
            "Barbara",
            "James",
            "Mary",
            "John",
            "Robert",
            "Michael",
            "Linda",
            "james",
            "mary"
        };

        // Remove "Robert" from the list created from nameArray.
        List<String> l1 = zap7(Arrays.asList(nameArray),
                               "Robert");

        // Remove "Robert" from the list created from nameArray.
        List<String> l2 = zap8(Arrays.asList(nameArray),
                               "Robert");

        // Check to ensure the zap*() methods work.
        if (l1.contains("Robert") 
            || l2.contains("Robert"))
            System.out.println("Test failed");
        else
            System.out.println("Test succeeded");
    }        

    /**
     * Remove any strings matching @a omit from the list of strings
     * using basic Java 7 features.
     */
    static List<String> zap7(List<String> lines,
                             String omit) {
        // Create an array list return result.
        List<String> res = 
            new ArrayList<>(); 

        // Iterate through all the lines in the list and remove any
        // that match @a omit.
        for (String line : lines) 
            if (!omit.equals(line))
                res.add(line);   

        // Return the list.
        return res; 
    }

    /**
     * Remove any strings matching @a omit from the list of strings
     * using basic Java 8 features.
     */
    static List<String> zap8(List<String> lines,
                      String omit) {
        return lines
            // Convert the list to a stream.
            .stream()

            // Remove any methods that match @a omit.
            .filter(not(omit::equals))

            // Trigger intermediate operation processing and return
            // new list of results.
            .collect(toList());
    }

    /**
     * A generic negation predicate that can be used to negate a
     * predicate.
     *
     * @return The negation of the input predicate.
     */
    public static<T> Predicate<T> not(Predicate<T> p) {
        return p.negate();
    }
}

