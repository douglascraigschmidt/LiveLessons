package tests;

import utils.Utils;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingLong;
import static utils.Utils.startsWithHh;

/**
 * This class defines various generators and input that these
 * generators use to produce {@link Stream} and {@link Map} objects.
 */
@SuppressWarnings("unchecked")
public class Generators {
    /** 
     * A {@link List} of key characters in the play Hamlet.
     */
    public final static List<String> sCharacters = List
        .of("horatio",
            "claudius",
            "Gertrude",
            "Fortinbras", // Fortinbras appears twice.
            "fortinbras",
            "Polonius",
            "Reynaldo",
            "Voltemand",
            "Cornelius",
            "Rosencrantz",
            "Gildenstern",
            "Osiric",
            "Hamlet",
            "Hamlet", // Hamlet appears twice.
            "laertes",
            "Ophelia");

    /**
     * A {@link String} of key characters in the play Hamlet.
     */
    public final static String sCharactersStr =
        "horatio,claudius,Gertrude,Fortinbras,fortinbras,Polonius,Reynaldo,Voltemand,"
        + "Cornelius,Rosencrantz,Gildenstern,Osiric,Hamlet,Hamlet,laertes,Ophelia";

    /**
     * Create an array of {@link List} objects containing characters
     * from Hamlet.
     */
    public final static List<String>[] sArray = new List[]{
        List.of("Hamlet",
                "claudius",
                "Gertrude"),
        List.of("Ophelia",
                "laertes",
                "Polonius"),
        List.of("Reynaldo",
                "horatio",
                "Voltemand",
                "Cornelius",
                "Rosencrantz",
                "Gildenstern",
                "Osiric"),
        List.of("Fortinbras")};

    /**
     * @return A {@link Map} of characters from the play Hamlet whose
     * keys start with upper- or lower-case 'h' whose names are
     * consistently capitalized and whose values are the lengths of
     * the character's names.
     */
    public static Map<String, Long> generateHCharactersMap
        (String characters) {
        return Generators
            // Generate a Stream.
            .generateHCharacters(Pattern
                                 // Create a stream of characters from
                                 // William Shakespeare's Hamlet.
                                 .compile(",")
                                 .splitAsStream(characters))

            // Terminal operation that triggers intermediate operation
            // processing and groups the results into a map whose keys
            // are strings of matching Hamlet characters and whose
            // values are the length of each string.
            .collect(groupingBy(identity(),
                                // Use a TreeMap to sort the results.
                                TreeMap::new,
                                summingLong(String::length)));
    }

    /**
     * @return A sorted {@link Stream} of characters from the play
     * Hamlet whose names start with upper- or lower-case 'h' whose
     * names are consistently capitalized.
     */
    public static Stream<String> generateHCharacters
    (Stream<String> characters) {
        return characters
            // Remove any strings that don't start with 'h' or 'H'.
            .filter(startsWithHh(true))

            // Capitalize the first letter in the string.
            .map(Utils::capitalize)

            // Sort the results in ascending order.
            .sorted();
    }
}
