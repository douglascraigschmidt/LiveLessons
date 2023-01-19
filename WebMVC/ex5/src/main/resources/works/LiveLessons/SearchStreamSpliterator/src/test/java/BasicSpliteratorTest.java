import org.testng.annotations.Test;
import search.PhraseMatchSpliterator;
import search.SearchResults;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Tests the features of the PhaseMatchSpliterator.  Thanks to Sanjeev
 * Kumar for contributing this test.
 */
public class BasicSpliteratorTest {
    /**
     * The input to test.
     */
    private static final String sINPUT_DATA =
        "The quick\nbrown fox\njumps over\nthe lazy dog";

    /**
     * Search for a phrase in the input data.
     */
    private static SearchResults searchForPhrase(String phrase,
                                                 CharSequence inputData,
                                                 boolean parallel) {
        return new SearchResults
            (phrase,
             "",
             StreamSupport
             // Create a stream of Results to record the indices (if
             // any) where the phrase matched the input data.
             .stream(new PhraseMatchSpliterator(inputData, phrase),
                     parallel)

             // This terminal operation triggers aggregate operation
             // processing and returns a list of Results.
             .collect(toList()));
    }

    /**
     * Perform sequential and parallel searches 
     */
    private String performSearches(String phrase) {
        // Conduct a sequential search.
        SearchResults r1 =
            searchForPhrase(phrase,
                            sINPUT_DATA,
                            false);

        // Conduct a parallel search.
        SearchResults r2 =
            searchForPhrase(phrase,
                            sINPUT_DATA,
                            true);
            
        // Get the results as strings.
        String sequentialResult = r1.toString();
        String parallelResult = r2.toString();

        // Make sure the results are correct.
        assertNotEquals(0, r1.getResultList().size());
        assertNotEquals(0, r2.getResultList().size());
        assertEquals(true, sequentialResult.equals(parallelResult));

        return sequentialResult;
    }

    /**
     * Test PhraseMatchSpliterator for single words.
     */
    @Test
    public void testSingleWords() {
        // Create a list of words that match.
        List<String> results = Pattern
            // Compile splitter into a regular expression (regex).
            .compile("\\s")

            // Use the regex to split the file into a stream of
            // strings.
            .splitAsStream(sINPUT_DATA)

            // Run the stream in parallel.
            .parallel()

            // Perform the sequential and parallel searches.
            .map(this::performSearches)

            // Collect the results into a list.
            .collect(toList());

        // Print the list.
        System.out.println(results);
    }

    /**
     * Test PhraseMatchSpliterator for phrases.
     */
    @Test
    public void testPhrases() {
        // Create a list of phrases that match.
        List<String> results = Pattern
            // Compile splitter into a regular expression (regex).
            .compile("\\n")

            // Use the regex to split the file into a stream of
            // strings.
            .splitAsStream(sINPUT_DATA)

            // Run the stream in parallel.
            .parallel()

                // Perform the sequential and parallel searches.
            .map(this::performSearches)

            // Collect the results into a list.
            .collect(toList());

        // Print the list.
        System.out.println(results);
    }
}
