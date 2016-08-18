package livelessons.streamgangs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;
import rx.Observable;
import rx.schedulers.Schedulers;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use RxJava in
 * conjunction with Java Streams to search how many times each word in
 * an array of words appears in input data.
 */
public class SearchWithRxJavaWords
        extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithRxJavaWords(List<String> wordsToFind,
                                 String[][] stringsToSearch) {
        // Pass input to superclass constructor.
        super(wordsToFind,
                stringsToSearch);
    }

   @Override
    protected List<List<SearchResults>> processStream() {
       return Observable
           // Get the input from the list of words.
           .from(mWordsToFind)

           // ...
           .flatMap(val -> 
                    // ...
                    Observable.just(val)
                    // ...
                    .subscribeOn(Schedulers.computation())
                    // ...
                    .map(this::processWord))

           // ...
           .toList()

           // ...
           .toBlocking()

           // ...
           .first();
   }

    private List<SearchResults> processWord(String word) {
     	// Get the input.
        return getInput()
            // Sequentially process each String in the input list.
            .stream()

            // Map each String to a Stream containing the words found
            // in the input.
            .map(inputSTring -> {
                    String title = getTitle(inputString);
                    return searchForWord(word, 
                                         // Skip over the title.
                                         input.substring(title.length()),
                                         title);
                })

            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)

            // Create a list of SearchResults.
            .collect(toList());
    }
}
