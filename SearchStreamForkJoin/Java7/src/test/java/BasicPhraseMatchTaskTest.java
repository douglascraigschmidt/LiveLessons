import org.junit.Test;
import search.PhraseMatchTask;
import search.SearchResults;

import java.util.StringTokenizer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Tests the features of the PhraseMatchTask.  Thanks to Sanjeev Kumar
 * for contributing this test.
 */
public class
BasicPhraseMatchTaskTest {
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
             // Perform the processing (either sequentially or in
             // parallel) and return a list of Results.
             new PhraseMatchTask(inputData,
                                 phrase,
                                 parallel).compute());
    }

    /**
     * Test PhraseMatchTask.
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
