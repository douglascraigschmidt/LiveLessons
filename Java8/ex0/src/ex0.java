import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

/**
 * This example shows two zap*() method implementations that remove
 * a string from a list of strings.  One method uses traditional Java 7
 * features and the other uses basic modern Java features.
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
        List<String> l1 = zap7(List.of(nameArray),
                               "Robert");

        // Remove "Robert" from the list created from nameArray.
        List<String> l2 = zapModern(List.of(nameArray),
                                    "Robert");

        // Check to ensure the zap*() methods work.
        if (l1.contains("Robert") 
            || l2.contains("Robert"))
            System.out.println("Test failed");
        else
            System.out.println("Test succeeded");
    }        

    /**
     * Remove any strings matching {@code omit} from the list of
     * strings using basic Java 7 features.
     */
    static List<String> zap7(List<String> lines,
                             String omit) {
        // Create an array list return result.
        List<String> res = 
            new ArrayList<>(); 

        // Iterate through all the lines in the list and remove any
        // that match omit.
        for (String line : lines) 
            if (!omit.equals(line))
                res.add(line);   

        // Return the list.
        return res; 
    }

    /**
     * Remove any strings matching {@code omit} from the list of
     * strings using basic modern Java features.
     */
    static List<String> zapModern(List<String> lines,
                                  String omit) {
        return lines
            // Convert the list to a stream.
            .stream()

            // Remove any strings that match omit.
            .filter(not(omit::equals))

            // Trigger intermediate operation processing and return
            // new list of results.
            .collect(toList());
    }
}

