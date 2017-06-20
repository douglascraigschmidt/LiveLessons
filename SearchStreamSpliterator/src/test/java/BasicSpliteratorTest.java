import search.PhraseMatchSpliterator;
import search.SearchResults;

import org.junit.Test;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.StringTokenizer;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

/**
 * Tests the features of the PhaseMatchSpliterator.  Thanks to Sanjeev
 * Kumar for contributing this test.
 */
public class BasicSpliteratorTest {
    /**
     * The input to test.
     */
    private static final String sINPUT_DATA =
        "A quick brown fox jumps over the lazy dog";

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
     * Test PhraseMatchSpliterator.
     */
    @Test
    public void test() {
        // Create a tokenizer for the input.
        StringTokenizer tokenizer =
            new StringTokenizer(sINPUT_DATA, " ");

        // Conduct a sequential and parallel search for each entry in
        // the input.
        while (tokenizer.hasMoreElements()) {
            String nextWord = tokenizer.nextToken();

            // Conduct a sequential search.
            SearchResults r1 =
                searchForPhrase(nextWord,
                                sINPUT_DATA,
                                false);

            // Conduct a parallel search.
            SearchResults r2 =
                searchForPhrase(nextWord,
                                sINPUT_DATA,
                                true);
            
            // Get the results as strings.
            String sequentialResult = r1.toString();
            String parallelResult = r2.toString();

            // Make sure the results are correct.
            assertNotEquals(0, r1.getResultList().size());
            assertNotEquals(0, r2.getResultList().size());
            assertEquals(true, sequentialResult.equals(parallelResult));

            // Print the results.
            System.out.println (sequentialResult + ": sequential");
            System.out.println (parallelResult + ": parallel");
        }
    }
}
