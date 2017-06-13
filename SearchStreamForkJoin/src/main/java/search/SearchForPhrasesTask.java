package search;

import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static utils.StreamsUtils.not;

/**
 *
 */
public class SearchForPhrasesTask
       extends RecursiveTask<List<SearchResults>> {
    /**
     * The input string to search.
     */
    private final CharSequence mInputString;

    /**
     * The list of phrases to find.
     */
    private List<String> mPhrasesToFind;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    private boolean mParallel;

    /**
     * Constructor initializes the field.
     */
    public SearchForPhrasesTask(CharSequence inputString,
                                List<String> phrasesToFind,
                                boolean parallel) {
        mInputString = inputString;
        mPhrasesToFind = phrasesToFind;
        mParallel = parallel;
    }

    /**
     * This method searches the @a inputString for all occurrences
     * of the phrases to find.
     */
    public List<SearchResults> compute() {
        // Get the section title.
        String title = getTitle(mInputString);

        // Skip over the title.
        CharSequence input = mInputString.subSequence(title.length(),
                                                      mInputString.length());

        // Find all occurrences of phrase in the input string.
        return mPhrasesToFind
            // Convert the list of phrases to find into a stream.
            .stream()

            // Find all indices where phrase matches the input data.
            .map(phrase -> searchForPhrase(phrase,
                                           input,
                                           title,
                                           mParallel))

            // Only keep a result that has at least one match.
            .filter(not(SearchResults::isEmpty))
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Terminate the stream and trigger the processing.
            .collect(toList());
    }

    /**
     * Looks for all instances of @code phrase in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    private SearchResults searchForPhrase(String phrase,
                                          CharSequence inputData,
                                          String title,
                                          boolean parallel) {
        // Create/return SearchResults to keep track of relevant info.
        return new SearchResults
            (Thread.currentThread().getId(),
             1,
             phrase,
             title,
             // Use a PhraseMatchTask to add the indices of all places
             // in the inputData where phrase matches.
             new PhraseMatchTask(inputData, phrase, parallel).compute());
    }

    /**
     * Return the title portion of the @a inputData.
     */
    private String getTitle(CharSequence input) {
        // Create a Matcher.
        Matcher m = Pattern
            // This regex matchs the first line in the input.
            .compile("(?m)^.*$")

            // Create a matcher for this pattern.
            .matcher(input);

        return m.find()
            ? m.group()
            : "";
    }
}

