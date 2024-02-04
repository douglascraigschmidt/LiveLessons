import utils.RegexUtils;
import utils.BardDataFactory;

import java.text.BreakIterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.AbstractMap.SimpleEntry;
import static java.util.stream.Collectors.toMap;
import static utils.RegexUtils.makeRegex;

/**
 * This example shows how the Java regular expression methods can be
 * used in conjunction with Java sequential streams and the Java
 * {@link BreakIterator} class to search the complete works of
 * Shakespeare ({@code bardWorks}) for given words and phrases.
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
        var test = new ex45();

        // Test the makeRegex() method.
        test.testRegexList();

        // Create a List of String objects containing the complete
        // works of Shakespeare, one work per String.
        List<String> bardWorks = BardDataFactory
            .getInput(sSHAKESPEARE_DATA_FILE,
                      // Split input into "works".
                      "@");

        // Search the works of Shakespeare for a certain word/phrase ("lord").
        test.processBardWorks(Objects.requireNonNull(bardWorks),
                              "lord");
    }

    /**
     * Test the makeRegex() method.
     */
    private void testRegexList() {
        // The quote to search for matches.
        String quote = """
                       The quick brown fox jumps 
                       \nover the lazy dog.""";

        // The words to match.
        var wordsToMatch = List
            .of("Cat", "Dog", "Mouse", "sox");

        // Convert 'wordsToMatch' to a regular expression.
        var regexString = RegexUtils
            .makeRegex(wordsToMatch);

        // Check whether 'quote' is matched by 'regexString'.
        var result = quote
            // Match across newlines.
            .toLowerCase().matches("(?s)" + regexString)
            ? "matches" : "does not match";

        System.out.println("The quote \""
                           + quote
                           + "\" "
                           + result
                           + " the regex string \n\""
                           +  regexString
                           + "\"\n");
    }

    /**
     * Show how the Java regular expression methods can be used to
     * search the complete works of Shakespeare ({@code bardWorks} for
     * {@code word}.
     */
    private void processBardWorks(List<String> bardWorks,
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

        var pattern = Stream
            // The regular expression to compile, which matches the
            // phrase "'word' followed by either 'true' or 'false'".
            .of("\\b"
                + word
                + "\\b"
                + makeRegex(List.of("true", "false")))

            // Compile the regular expression to perform
            // case-insensitive matches.
            .map(regex -> Pattern
                 .compile(regex, Pattern.CASE_INSENSITIVE))

            // Return the first match.
            .findFirst().orElse(null);

        // Find all works of Shakespeare that match the pattern.
        var matches = findRegexMatches(bardWorksMatchingWord, pattern);

        // Show the portions of the works of Shakespeare that match
        // the pattern.
        showAllMatches(bardWorksMatchingWord.size(),
                       matches);
    }

    /**
     * Return true if the {@code work} contains the {@code
     * searchWord}.
     *
     * @param work The text to search
     * @param searchWord The word to search for
     * @return true if the {@code work} contains the {@code
     *         searchWord}, else false
     */
    private boolean findMatch(String work,
                              String searchWord) {
        BreakIterator iterator = BreakIterator
            // Get the word iterator for the US locale.
            .getWordInstance(Locale.US);

        // Associate iterator with the 'work'.
        iterator.setText(work);

        // Create a Stream of word boundaries and collect into a List.
        List<Integer> boundaries = getWordBoundariesList(iterator);

        // Create a Stream of words using the collected boundaries and
        // return true if any of the words are 'searchWord'.
        return isWordInWork(work, searchWord, boundaries);
    }

    /**
     * Create a {@link Stream} of word boundaries and collect them in
     * a {@link List}.
     *
     * @param iterator The {@link BreakIterator} that iterates through
     *                 a work of Shakespeare
     * @return A {@link List} of word boundaries
     */
    private static List<Integer> getWordBoundariesList(BreakIterator iterator) {
        return Stream
            // Iterate over the boundaries of 'work', beginning with
            // the first boundary.
            .iterate(iterator.first(),
                     // Stop iterating when the iterator returns
                     // BreakIterator.DONE.
                     boundary -> boundary != BreakIterator.DONE,
                     // Move the iterator to the next boundary.
                     boundary -> iterator.next())

            // Convert the Stream of boundaries into a List.
            .toList();
    }

    /**
     * Create a {@link Stream} of words using the collected boundaries
     * and return true if any of the words are {@code searchWord}.
     *
     * @param work A work of Shakespeare
     * @param searchWord The word to search for in the {@code work}
     * @param boundaries A {@link List} of word boundaries
     * @return True if any of the words equal {@code searchWord}, else
     *         false
     */
    private static boolean isWordInWork(String work,
                                        String searchWord,
                                        List<Integer> boundaries) {
        return IntStream
            // Iterate over the boundaries of 'work'.
            .range(0, boundaries.size() - 1)

            // Convert the boundaries into a Stream of words.
            .mapToObj(i -> work
                      .substring(boundaries.get(i),
                                 boundaries.get(i + 1)))

            // Return true if any of the words equal 'searchWord'.
            .anyMatch(word -> word
                      .toLowerCase()
                      .equals(searchWord));
    }

    /**
     * Find the works of Shakespeare that match the {@link Pattern}.
     *
     * @param bardWorksMatchingWord The Shakespeare works matching
     *                              a search word
     * @param pattern The compiled regular expression to search for
     * @return A {@link Map} containing all matching results
     */
    private Map<String, List<MatchResult>> findRegexMatches
        (List<String> bardWorksMatchingWord,
         Pattern pattern) {
        // Collect and return all matching results across all works
        // into a Map.
        return bardWorksMatchingWord
            // Convert List to a Stream.
            .stream()

            // Map each work to a SimpleEntry contain the title of the
            // work and the List of matches for that work.
            .mapMulti(createWorkToMatchesMapper(pattern))

            // Convert the Stream of SimpleEntry objects to a Map
            // where the 'key' is the title of the work, and the
            // 'value' is the List of matches for each work.
            .collect(toMap(SimpleEntry::getKey,
                           SimpleEntry::getValue));
    }

    /**
     * Show the portions of the works of Shakespeare that match the
     * pattern.
     *
     * @param bardWorksMatchingWord The total number of works that
     *                              match the search word
     * @param allMatchingResults A {@link Map} containing all matching
     *                           results
     */
    private void showAllMatches
        (int bardWorksMatchingWord,
         Map<String, List<MatchResult>> allMatchingResults) {
        // Print the total number of matches.
        System.out.println("Matching works = "
                           + allMatchingResults.size()
                           + " out of "
                           + bardWorksMatchingWord
                           + " works and the matches for each work are:");

        allMatchingResults
            // Iterate through the Map and print the results.
            .forEach((key, value) -> {
                    // Print out the title of the work.
                    System.out.println(key);

                    value
                        // Iterate through the List of matches and
                        // print them out.
                        .forEach(matchResult -> System.out
                                 .println("\"" 
                                          + matchResult.group()
                                          + "\" ["
                                          + matchResult.start()
                                          + "]"));
                });
    }

    /**
     * Create an interface tailored for processing text with a focus on
     * pattern matching and result aggregation to simplify the return
     * type of the {@code createWorkToMatchesMapper()} method below.
     */
    interface WorkPatternMatcherConsumer
              extends BiConsumer<String,
                                 Consumer<SimpleEntry<String,
                                          List<MatchResult>>>> {}

    /**
     * This factory method returns a {@link BiConsumer} that maps each
     * work to a {@link SimpleEntry} containing the title of the work
     * and an associated {@link List} of {@code pattern} matches for
     * that work. Only works that match the {@code pattern} are
     * returned.
     *
     * @param pattern The compiled regular expression to search for
     * @return A {@link BiConsumer} that maps the title of each work
     *         with a {@link List} of non-empty matches for that work
     */
    private static WorkPatternMatcherConsumer
    createWorkToMatchesMapper(Pattern pattern) {
        // Return a BiConsumer that maps the title of each work with
        // a List of non-empty matches for that work.
        return (String work,
                Consumer<SimpleEntry<String, List<MatchResult>>> consumer) -> {
            // Get a List of all matches for the work.
            var matchList = pattern
                // Associate the 'work' with the 'pattern'.
                .matcher(work)

                // Get a Stream of MatchResult objects.
                .results()

                // Convert the Stream into a List.
                .toList();

            // Filter out any work that has no matches.
            if (!matchList.isEmpty()) {
                // Get the title of the work.
                var title = RegexUtils.getFirstLine(work);

                // Create a SimpleEntry containing the title of the
                // work and the List of matches for that work and
                // accept the entry into the consumer.
                consumer.accept(new SimpleEntry<>(title, matchList));
            }
        };
    }
}

