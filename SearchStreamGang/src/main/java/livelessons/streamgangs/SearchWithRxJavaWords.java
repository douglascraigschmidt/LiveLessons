package livelessons.streamgangs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;
import rx.Observable;
import rx.observables.BlockingObservable;
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

    /**
     * Perform the processing, which uses an RxJava Stream to
     * concurrently search for words in the input data.
     */
   @Override
   protected List<List<SearchResults>> processStream() {
       return Observable
           // Converts mWordsToFind array into an Observable that
           // emits the items in the array.
           .from(mWordsToFind)

           // Returns an Observable that emits items based on applying
           // processWord() to each item emitted by the source
           // Observable, where processWord() returns an Observable,
           // and then merging the resulting Observables and emitting
           // the results of this merger as a single Observable.
           .flatMap(word ->
                    Observable
                    // Returns an Observable that emits a single word
                    // and then completes.
                    .just(word)

                    // Asynchronously subscribes Observers to this
                    // Observable on the computation scheduler.
                    .subscribeOn(Schedulers.computation())

                    // Returns an Observable that applies the
                    // processWord() method to each item emitted by
                    // the source Observable and emits a list of
                    // SearchResults.
                    .map(this::processWord))

           // Returns an Observable that emits a single item: a list
           // composed of all the items emitted by the source
           // Observable, which is itself a list of SearchResults.
           .toList()

           // Converts an Observable into a BlockingObservable (an
           // Observable with blocking operators).
           .toBlocking()

           // Returns an Observable that emits only the very first
           // item emitted by the source Observable, which is a list
           // of a list of SearchResults.
           .first();
   }

    /**
     * Search all the input strings for all occurrences of the word to
     * find.
     */
    private List<SearchResults> processWord(String word) {
     	// Get the input.
        return getInput()
            // Sequentially process each String in the input list.
            .stream()

            // Map each input string to a stream of SearchResults for
            // each time the word appears in the input.
            .map(inputString -> {
                    String title = getTitle(inputString);

                    // Return a SearchResults containing a match for
                    // each time word appears in the input string.
                    return searchForWord(word, 
                                         // Skip over the title.
                                         inputString.substring(title.length()),
                                         title);
                })

            // Only keep SearchResults with at least one match.
            .filter(result -> result.size() > 0)

            // Create a list of SearchResults.
            .collect(toList());
    }
}
