package search;

import utils.Options;

import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static utils.StreamsUtils.not;

/**
 * This class searches for phrases in the works of Shakespeare.  It
 * demonstrates the use of modern Java functional programming
 * features, such as lambda expressions, method references, functional
 * interfaces, sequential/parallel streams, and a spliterator.
 */
public class SearchWithSpliterator {
    /**
     * The list of strings to search.
     */
    private final List<? extends CharSequence> mInputList;

    /**
     * The list of phrases to find.
     */
    private final List<String> mPhrasesToFind;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    private final boolean mParallelSpliterator;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    private final boolean mParallelPhrases;

    /**
     * Indicates whether to run the input concurrently.
     */
    private final boolean mParallelInput;

    /**
     * Construtor initializes the fields.
     */
    public SearchWithSpliterator(List<? extends CharSequence> inputList,
                                 List<String> phrasesToFind,
                                 boolean parallelSpliterator,
                                 boolean parallelPhrases,
                                 boolean parallelInput) {
        mInputList = inputList;
        mPhrasesToFind = phrasesToFind;
        mParallelSpliterator = parallelSpliterator;
        mParallelPhrases = parallelPhrases;
        mParallelInput = parallelInput;
    }

    /**
     * Performs stream processing on the input.
     */
    public List<List<SearchResults>> processStream() {
        return (mParallelInput
            // Convert the list into a parallel stream.
            ? mInputList.parallelStream()
            // Convert the List to a sequential Stream.
            : mInputList.stream())

            // Process each input string to find all occurrences of
            // the search phrases.
            .map(this::processInput)

            // If a phrase was found add it to the list of results.
            .filter(not(List<SearchResults>::isEmpty))

            // This terminal operation triggers aggregate operation
            // processing and returns a list of list of SearchResults.
            .toList();
    }

    /**
     * This method searches the @a inputString for all occurrences of
     * the phrases to find.
     */
    private List<SearchResults> processInput(CharSequence inputString) {
        // Get the section title.
        String title = getTitle(inputString);

        // Skip over the title.
        CharSequence input = inputString.subSequence(title.length(),
                                                     inputString.length());

        return (mParallelPhrases
            // Convert the List into a parallel Stream.
            ? mPhrasesToFind.parallelStream()
            // Convert the List to a sequential stream.
            : mPhrasesToFind.stream())

            // Find all indices where phrase matches the input data.
            .map(phrase -> searchForPhrase(phrase,
                                           input,
                                           title,
                                           mParallelSpliterator))

            // Only keep a result that has at least one match.
            .filter(not(SearchResults::isEmpty))
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Terminate the stream and trigger the processing.
            .toList();
    }

    /**
     * Looks for all instances of @code phrase in @code inputData and
     * return a list of all the @code SearchResults (if any).
     */
    private SearchResults searchForPhrase(String phrase,
                                          CharSequence inputData,
                                          String title,
                                          boolean parallel) {
        List<SearchResults.Result> resultList =
            // Use a PhraseMatchSpliterator to add the indices of all
            // places in the inputData where phrase matches.
            StreamSupport
                // Create a stream of Results to record the indices
                // (if any) where the phrase matched the input data.
                .stream(new PhraseMatchSpliterator(inputData, phrase),
                        parallel)
                    
                // This terminal operation triggers aggregate
                // operation processing and returns a list of Results.
                .toList();

    	// Create/return SearchResults to keep track of relevant info.
        return new SearchResults(Thread.currentThread().getId(),
                                 1,
                                 phrase,
                                 title,
                                 resultList);
    }

    /**
     * Return the title portion of the @a inputData.
     */
    private String getTitle(CharSequence input) {
        // Create a Matcher.
        Matcher m = Pattern
                // Compile a regex that matches only the first line in
                // the input.
                .compile("(?m)^.*$")

                // Create a matcher for this pattern.
                .matcher(input);

        // Return the result.
        return m.find()
                // If a match occurs return the title.
                ? m.group()
                // Return an empty string if there's no match.
                : "";
    }
}
