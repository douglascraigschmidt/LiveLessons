import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This example two implementations of a zap() method that removes
 * strings from a list of strings.  One method uses basic Java 7
 * features and the other uses basic Java 8 features.
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

        List<String> l1 = zap7(Arrays.asList(nameArray),
                               "Robert");

        List<String> l2 = zap8(Arrays.asList(nameArray),
                               "Robert");

        if (l1.contains("Robert") 
            || l2.contains("Robert"))
            System.out.println("Test failed");
        else
            System.out.println("Test succeeded");
    }        
     
    static List<String> zap7(List<String> lines,
                      String omit) {
        List<String> res = 
            new ArrayList<>(); 

        for (String line : lines) 
            if (!omit.equals(line))
                res.add(line);   
        return res; 
    }

    static List<String> zap8(List<String> lines,
                      String omit) {
        return lines
            .parallelStream()
            .filter(not(omit::equals))
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

