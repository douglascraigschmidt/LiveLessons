package livelessons.streamgangs;

import livelessons.utils.*;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * This class factors out common code used by all StreamGang framework
 * implementation strategies in the SearchStreamGang program.  It
 * customizes this framework to search a list of input character
 * sequences for phrases provided in a list of phrases to find.
 */
public abstract class SearchStreamGang
       extends StreamGang<CharSequence> {
    /**
     * The list of phrases to find.
     */
    protected final List<String> mPhrasesToFind;

    /**
     * Iterator to the list of character sequences to search.
     */
    private final Iterator<List<CharSequence>> mInputIterator;

    /**
     * Constructor initializes the fields.
     */
    public SearchStreamGang(List<String> phrasesToFind,
                            List<List<CharSequence>> listOfListOfInputsToSearch) {
        // Store the phrases to search for in the input.
        mPhrasesToFind = phrasesToFind;

        // Create an iterator for the array of character sequences to search.
        mInputIterator = listOfListOfInputsToSearch.iterator();
    }

    /**
     * This factory method returns the next list of character
     * sequences to search for via the implementation strategies.
     */
    @Override
    protected List<CharSequence> getNextInput() {
        if (!mInputIterator.hasNext())
            // No more input, so we're done.
            return null;
        else {
            // Start a new cycle.
            incrementCycle();

            // Return a list containing character sequences to search.
            return mInputIterator.next();
        }
    }

    /**
     * This template method starts the processing to search the list
     * of input character sequences for the given phrases to find.
     */
    @Override
    protected void initiateStream() {
        // Execute the test by calling its processStream() entry point
        // and recording how long it takes to run.
        List<List<SearchResults>> results = RunTimer
            .timeRun(this::processStream, TAG);

        // Print the results of the test.
        printResults(TAG, results);
    }

    /**
     * Print out the search results for the given {@code test}.
     */
    private void printResults(String test,
                              List<List<SearchResults>> listOfListOfSearchResults) {
        System.out
            .println(StringUtils.lastSegment(test, '.') +
                     ": The search returned "
                     // Compute the number of phrase matches.
                     + listOfListOfSearchResults
                     .stream()
                     .mapToInt(list -> list
                               .stream()
                               .mapToInt(SearchResults::size)
                               .sum())
                     .sum()
                     + " phrase matches for "
                     + getInput().size() 
                     + " input strings");

        // Print out the titles if running in verbose mode.
        if (Options.getInstance().isVerbose())
            printPhrases(listOfListOfSearchResults);
    }

    /**
     * Displays the phrases associated with each input source.
     */
    private void printPhrases(List<List<SearchResults>> listOfListOfSearchResults) {
        // Create a map that associates phrases found in the input
        // with the titles where they were found.
        Map<String, List<SearchResults>> resultsMap = listOfListOfSearchResults
            // Convert the list of lists into a stream of lists.
            .stream()

            // Flatten the lists into a stream of SearchResults.
            .flatMap(List::stream)

            // Collect the SearchResults into a TreeMap, which sorts
            // the keys by their titles.
            .collect(groupingBy(SearchResults::getTitle,
                                TreeMap::new,
                                toList()));

        // Print the map results, where each title is printed followed
        // by a list of indices where phrase appeared in the input.
        resultsMap
            .forEach((key, value)
                     -> {
                         System.out.println("Title \""
                                            + key
                                            + "\" contained");
                         // Print out the indicates for this key.
                         value.forEach(SearchResults::print);
                     });
    }

    /**
     * Looks for all instances of {@code phrase} in {@code input} and
     * return a list of all the {@code SearchResults} (if any).
     */
    public SearchResults searchForPhrase(String phrase,
                                         CharSequence input,
                                         String title,
                                         boolean parallel) {
        // Use PhraseMatchSpliterator to add the indices of all
        // locations in the input where the phrase matches.
        List<SearchResults.Result> resultList = StreamSupport
            // Create a stream of Results to record the indices
            // (if any) where the phrase matched the input.
            .stream(new PhraseMatchSpliterator(input, phrase),
                    parallel)

            // Terminal operation triggers aggregate operation
            // processing and returns a list of Result objects.
            .collect(toList());

        // Create/return a SearchResults to track the relevant info.
        return new SearchResults(Thread.currentThread().getId(),
                                 currentCycle(),
                                 phrase,
                                 title,
                                 resultList);
    }
    
    /**
     * Return the title portion of the {@code input}.
     */
    public String getTitle(CharSequence input) {
        // Create a Matcher.
        Matcher m = Pattern
            // Compile a regex that matches only the first line in the
            // input.
            .compile("(?m)^.*$")

            // Create a matcher for this pattern.
            .matcher(input);

        // Find/return the first line in the string.
        return m.find()
            // Return the title string if there's a match.
            ? m.group()

            // Return an empty string if there's no match.
            : "";
    }

    /**
     * Hook method that must be overridden by each subclass to perform
     * the stream gang processing.
     */
    protected abstract List<List<SearchResults>> processStream();
}

