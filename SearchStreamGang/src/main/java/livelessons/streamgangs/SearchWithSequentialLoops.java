package livelessons.streamgangs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import livelessons.utils.SearchResults;
import livelessons.utils.StringUtils;

/**
 * Customizes the SearchStreamGang framework to use Java loops to
 * sequentially search an input data string for each phrase in an array
 * of phrases.
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
     * Perform the processing, which uses a Java loop to sequentially
     * search for phrases in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Run the sequential loop test on all the input.
        List<List<SearchResults>> listOfListOfSearchResults =
            new ArrayList<>();

        // Process all the strings in the list.
        for (CharSequence inputSeq : getInput()) {

            // Get the section title.
            String title = getTitle(inputSeq);

            // Skip over the title.
            CharSequence input = inputSeq.subSequence(title.length(), inputSeq.length());

            // Store the search results into a list.
            List<SearchResults> listOfSearchResults =
                new ArrayList<>();

            // Iterate through all the phrases to find.
            for (String phrase : mPhrasesToFind) {
                // Create the search results (if any).
                SearchResults results = searchForPhrase(phrase,
                                                       input,
                                                       title,
                                                       false);

                // Only add results if there's at least one match.
                if (results.size() > 0)
                    listOfSearchResults.add(results);
            }
                
            // Only add a list if there's at least one match.
            if (listOfSearchResults.size() > 0)
                listOfListOfSearchResults.add(listOfSearchResults);
        }

        return listOfListOfSearchResults;
    }

    /**
     * Looks for all instances of @a phrase in @a inputData and return
     * a list of all the @a SearchResults (if any).
     */
    public SearchResults searchForPhrase(String phrase,
                                        String input,
                                        String title,
                                        boolean notUsed) {
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
        Pattern pattern = Pattern.compile(regexPhrase,
                                          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

        // Create a regex matcher.
        Matcher phraseMatcher = pattern.matcher(input);

        // Determine the number of times the phrase matches in the input
        // string.
        while (phraseMatcher.find())
            resultList.add(new SearchResults.Result(phraseMatcher.start()));

        // Create/return a SearchResults object with the results of
        // the search.
        return new SearchResults(Thread.currentThread().getId(),
                                 0,
                                 phrase,
                                 title,
                                 resultList);
    }
}
