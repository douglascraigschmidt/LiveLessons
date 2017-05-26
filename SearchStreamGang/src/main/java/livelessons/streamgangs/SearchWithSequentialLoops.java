package livelessons.streamgangs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import livelessons.utils.SearchResults;
import livelessons.utils.StringUtils;

import javax.naming.directory.SearchResult;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use Java loops to
 * sequentially search an input data string for each word in an array
 * of words.
 */
public class SearchWithSequentialLoops
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithSequentialLoops(List<String> wordsToFind,
                                     List<List<String>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses a Java loop to sequentially
     * search for words in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Run the sequential loop test on all the input.
        List<List<SearchResults>> listOfListOfSearchResults =
            new ArrayList<>();

        // Process all the strings in the list.
        for (String inputString : getInput()) {

            // Get the section title.
            String title = getTitle(inputString);

            // Skip over the title.
            String input = inputString.substring(title.length());

            // Store the search results into a list.
            List<SearchResults> listOfSearchResults =
                new ArrayList<>();

            // Iterate through all the words to find.
            for (String word : mWordsToFind) {
                // Create the search results (if any).
                SearchResults results = searchForWord(word,
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
     * Looks for all instances of @a word in @a inputData and return a
     * list of all the @a SearchResults (if any).
     */
    public SearchResults searchForWord(String word,
                                       String inputData,
                                       String title,
                                       boolean parallel) {
        List<SearchResults.Result> resultList =
            new ArrayList<>();

        // Lowercase the input.
        String input = inputData.toLowerCase();

        // Determine the number of times the word matches in the input
        // string.

        for (int index = StringUtils.indexOf(input,
                                             false,
                                             word,
                                             0);
             index != -1;
             index = StringUtils.indexOf(input,
                                         false,
                                         word,
                                         index + word.length())) {
            resultList.add(new SearchResults.Result(index));
        }

        // Create/return a SearchResults object with the results of
        // the search.
        return new SearchResults
            (Thread.currentThread().getId(),
             0,
             word,
             title,
             resultList);
    }
}
