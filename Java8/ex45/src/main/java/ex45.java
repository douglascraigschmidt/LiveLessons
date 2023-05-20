import utils.TestDataFactory;

import java.text.BreakIterator;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static utils.RegexUtils.getFirstLine;
import static utils.RegexUtils.makeRegex;

/**
 * This example shows how the Java regular expression methods can be
 * used to search the complete works of Shakespeare ({@code bardWorks})
 * particular phrases.
 */
public class ex45 {
    /**
     * The complete works of William Shakespeare.
     */
    private static final String sSHAKESPEARE_DATA_FILE =
        "completeWorksOfShakespeare.txt";

    /**
     * Main entry point into the program.
     */
    static public void main(String[] argv) {
        // Test the makeRegex() method.
        testRegexList();

        // Create a List of String objects containing the complete
        // works of Shakespeare.
        List<String> bardWorks = TestDataFactory
            .getInput(sSHAKESPEARE_DATA_FILE,
                      // Split input into "works".
                      "@");

        assert bardWorks != null;

        // Search the works of Shakespeare for a certain word/phrase.
        processBardWorks(bardWorks, "lord");
    }

    /**
     * Test the makeRegex() method.
     */
    private static void testRegexList() {
        var quote = "The quick fox jumps over \nthe lazy concatentate.";
        var regexString =
            makeRegex(List.of("Cat", "Dog", "Mouse", "sox"));

        if (quote.toLowerCase().matches("(?s)" + regexString))
            System.out.println("matches");
        else
            System.out.println("no matches");
    }

    /**
     * Show how the Java regular expression methods can be used to
     * search the complete works of Shakespeare ({@code bardWorks} for
     * {@code word}.
     */
    private static void processBardWorks(List<String> bardWorks,
                                         String word) {
        // Create a List of Shakespeare works containing 'word'.
        var bardWorksMatchingWord = bardWorks
            // Convert List to a Stream.
            .stream()

            // Only keep works containing 'word'.
            .filter(work ->
                    // Return true if 'word' appears in 'work'.
                    findMatch(work, word))

            // Convert List to a Stream.
            .toList();

        // The regular expression to compile, which matches the phrase
        // "'word' followed by either 'true' or 'false'".
        String regex = "\\b"
            + word
            + "\\b"
            + ".*(\\btrue\\b|\\bfalse\\b)";

        // Compile the regular expression to perform case-insensitive
        // matches.
        Pattern pattern = Pattern
            .compile(regex,
                     Pattern.CASE_INSENSITIVE);

        // Show the portions of the works of Shakespeare that match
        // the pattern.
        showRegexMatches(bardWorksMatchingWord, pattern);
    }

    /**
     * Return true if the {@code work} contains the {@code searchWord}.
     *
     * @param work The text to search
     * @param searchWord The word to search for
     * @return true if the {@code work} contains the {@code searchWord}
     */
    private static boolean findMatch(String work,
                                     String searchWord) {
        // Create a BreakIterator that will break words.
        BreakIterator iterator = BreakIterator
            .getWordInstance(Locale.US);

        // Set the text to search.
        iterator.setText(work);

        // Get the first and second boundary from the iterator.
        int previous = iterator.first();

        // Iterate through all the text.
        for (int current = iterator.next(); 

             // Keep iterating until the BreakIterator is done.
             current != BreakIterator.DONE;

             // Update the current boundary.
             current = iterator.next()) {
            // Get the text between the previous and current
            // boundaries.
            String word = work.substring(previous, current);

            // Check if the item matches the predicate and
            // that 'word' contains 'searchWord'.
            if (Character.isLetterOrDigit(word.charAt(0))
                && word.toLowerCase().equals(searchWord)) {
                return true;
            }

            // Make current boundary the previous boundary.
            previous = current;
        }

        // Return false if there's no match.
        return false;
    }

    /**
     * Show the portions of the works of Shakespeare that match the
     * {@link Pattern}.
     *
     * @param bardWorksMatchingWord The Shakespeare works matching
     *                              a search word       
     * @param pattern The compiled regular expression to search for
     */
    private static void showRegexMatches
        (List<String> bardWorksMatchingWord,
         Pattern pattern) {
        bardWorksMatchingWord
            // Process each work in the Stream.
            .forEach(work -> {
                    pattern
                        // Create a Matcher that associates the regex pattern
                        // with the work.
                        .matcher(work)

                        // Create a Stream of matches.
                        .results()

                        // Only print the title for matches that aren't empty.
                        .peek(___ -> System.out
                            // Print the title of the work.
                            .println(getFirstLine(work)))

                        // Print each match.
                        .forEach(matchResult -> System.out
                                 .println("\""
                                          // Print the match.
                                          + matchResult.group()
                                          + "\" ["
                                          // Print match location.
                                          + matchResult.start()
                                          + "]"));
                });
    }
}
