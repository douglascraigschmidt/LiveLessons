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
 * demonstrates the use of Java 8 functional programming features,
 * such as lambda expressions, method references, functional
 * interfaces, sequential/parallel streams, a fork/join pool, and a
 * spliterator.
 */
public class SearchWithSpliterator {
    /**
     * The list of strings to search.
     */
    private List<? extends CharSequence> mInputList;

    /**
     * The list of phrases to find.
     */
    private List<String> mPhrasesToFind;

    /**
     * Indicates whether to run the spliterator concurrently.
     */
    private boolean mParallelSpliterator;

    /**
     * Indicates whether to run the phrases concurrently.
     */
    private boolean mParallelPhrases;

    /**
     * Indicates whether to run the input concurrently.
     */
    private boolean mParallelInput;

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
        Stream<? extends CharSequence> inputStream = mInputList
            // Convert the list of input strings into a stream.
            .stream();

        if (mParallelInput)
            // Convert the stream to a parallel stream.
            inputStream.parallel();

        // Create a list of SearchResults that indicate which phrases
        // are found in the list of input strings.
        return inputStream
            // Process each input string to find all occurrences of
            // the search phrases.
            .map(this::processInput)

            // If a phrase was found add it to the list of results.
            .filter(not(List<SearchResults>::isEmpty))

            // This terminal operation triggers aggregate operation
            // processing and returns a list of list of SearchResults.
            .collect(toList());
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

        Stream<String> phraseStream = mPhrasesToFind
            // Convert the list of phrases to find into a stream.
            .stream();

        if (mParallelPhrases)
            // Convert the stream to a parallel stream.
            phraseStream.parallel();

        // Find all occurrences of phrase in the input string.
        return phraseStream
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
                .collect(toList());

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
                // Compile a regex that matches only the first line in the input.
                .compile("(?m)^.*$")

                // Create a matcher for this pattern.
                .matcher(input);

        return m.find()
                ? m.group()
                : "";

        /* Could also use
          
        int index = inputData.indexOf('\n');
        return inputData.substring(0,
                                   index);
        */
    }
}
