import tests.CollectTests;
import tests.ForEachTests;
import tests.ReduceTests;

import java.util.*;

import static java.lang.Character.toLowerCase;
import static tests.Generators.sCharacters;
import static utils.Utils.capitalize;

/**
 * This program shows many modern Java Streams terminal operations,
 * including forEach*(), collect() (and various pre-defined
 * collectors), and several variants of reduce().  It also includes a
 * classic Java example as a baseline.  In addition, this program
 * shows how Java Streams can be used with "pure" functions (i.e.,
 * functions whose return values are only determined by their input
 * values) that have no side effects.
 */
public class ex12 {
    /**
     * The main entry point into the test program.
     */
    static public void main(String[] argv) {
        // Demonstrate the Java 7 baseline.
        runClassicJava();

        // Demonstrate the forEach() terminal operations.
        forEachTests();

        // Demonstrate the collect() terminal operations.
        collectTests();

        // Demonstrate the reduce() terminal operations.
        reduceTests();
    }

    /**
     * Demonstrate the forEach*() terminal operations.
     */
    private static void forEachTests() {
        ForEachTests.runForEach1();
        ForEachTests.runForEach2();
        ForEachTests.runForEachOrdered();
    }

    /**
     * Demonstrate the collect() terminal operations.
     */
    private static void collectTests() {
        CollectTests.runCollectToList();
        CollectTests.runCollectToImmutableList();
        CollectTests.runCollectToSet();
        CollectTests.runCollectToMap();
        CollectTests.runCollectGroupingBy();
        CollectTests.runCollectJoining();
        CollectTests.runTeeingCollector();
    }
    
    /**
     * Demonstrate the reduce() terminal operations.
     */
    private static void reduceTests() {
        ReduceTests.runReduce1();
        ReduceTests.runReduce2();
        ReduceTests.runReduce3();
        ReduceTests.runMapReduce1();
        ReduceTests.runMapReduce2();
    }

    /**
     * Run an example using only classic Java features, which serves
     * as a baseline for comparing with modern Java solutions.
     */
    private static void runClassicJava() {
        System.out.println("Results from runClassicJava():");

        List<String> listOfCharacters = new ArrayList<>(sCharacters);

        // Loop through all the characters.
        for (int i = 0; i < listOfCharacters.size();) {
            // Remove any strings that don't start with 'h' or 'H'.
            if (toLowerCase(listOfCharacters.get(i).charAt(0)) != 'h') {
                listOfCharacters.remove(i);
            } else {
                // Capitalize the first letter of a character whose
                // names starts with 'H' or 'h'.
                listOfCharacters
                    .set(i,
                         capitalize(listOfCharacters.get(i)));
                i++;
            }
        }

        // Sort the results in ascending order.
        Collections.sort(listOfCharacters);

        // Print the results.
        for (String s : listOfCharacters)
            System.out.println(s);
    }
}

