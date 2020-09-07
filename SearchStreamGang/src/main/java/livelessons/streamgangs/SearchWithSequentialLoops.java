package livelessons.streamgangs;

import livelessons.utils.SearchResults;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Customizes the SearchStreamGang framework to use Java for-each
 * loops to sequentially search an input character sequence for each
 * phrase in a list of phrases.
 */
public class SearchWithSequentialLoops
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithSequentialLoops(List<String> phrasesToFind,
                                     List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java for-each loop to
     * sequentially search for phrases in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Run the sequential loop test on all the input.
        List<List<SearchResults>> listOfListOfSearchResults =
            new ArrayList<>();

        // Process all the strings in the list.
        for (CharSequence inputSeq : getInput())
            // Map each input string to list of SearchResults
            // containing the phrases found in the input.
            listOfListOfSearchResults.add(processInput(inputSeq));

        // Return the results.
        return listOfListOfSearchResults;
    }

    /**
     * Sequentially search {@code inputSeq} for all occurrences of the
     * phrases to find.
     */
    private List<SearchResults> processInput(CharSequence inputSeq) {
        // Get the section title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input = inputSeq.subSequence(title.length(),
                                                  inputSeq.length());

        // Store the search results into a list.
        List<SearchResults> listOfSearchResults =
            new ArrayList<>();

        // Iterate through all the phrases to find.
        for (String phrase : mPhrasesToFind) {
            // Create the search results (if any).
            SearchResults results = searchForPhrase(phrase,
                                                    input,
                                                    title);

            // Only add results if there's at least one match.
            if (results.size() > 0)
                listOfSearchResults.add(results);
        }
        
        // Return the results
        return listOfSearchResults;
    }

    /**
     * Looks for all instances of {@code phrase} in {@code inputData}
     * and return a list of all the {@code SearchResults} (if any).
     */
    @SuppressWarnings("UnnecessaryContinue")
    public SearchResults searchForPhrase(String phrase,
                                         CharSequence input,
                                         String title) {
        // Create a list to hold the results.
        List<SearchResults.Result> resultList =
            new ArrayList<>();

        // Create a regex that will match the phrase across lines.
        String regexPhrase = phrase
            // Replace multiple spaces with one whitespace
            // boundary expression.
            .trim().replaceAll("\\s+", "\\\\s+")
            // Quote any question marks to avoid problems.
            .replace("?", "\\?");

        // Ignore case and search for phrases that split across lines.
        Pattern pattern = Pattern
            .compile(regexPhrase,
                     Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // Create a regex matcher and then determine the number of
        // times the phrase matches in the input string.
        for (Matcher phraseMatcher = pattern.matcher(input);
             phraseMatcher.find();
             resultList.add(new SearchResults.Result(phraseMatcher.start())))
            continue;

        // Create/return a SearchResults object with the results of
        // the search.
        return new SearchResults(Thread.currentThread().getId(),
                                 0,
                                 phrase,
                                 title,
                                 resultList);
    }
}
