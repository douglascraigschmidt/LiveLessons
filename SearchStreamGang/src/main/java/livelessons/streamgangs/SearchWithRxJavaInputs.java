package livelessons.streamgangs;

import sun.plugin.liveconnect.SecurityContextHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.naming.directory.SearchResult;

import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;
import rx.Observable;
import rx.Subscription;
import rx.functions.Actions;
import rx.observables.BlockingObservable;
import rx.schedulers.Schedulers;

import static java.util.stream.Collectors.toList;

/**
 * Customizes the SearchStreamGang framework to use RxJava in
 * conjunction with Java Streams to search the input data for each
 * word in an array of words.
 */
public class SearchWithRxJavaInputs
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithRxJavaInputs(List<String> wordsToFind,
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
            // Converts input strings array into an Observable that
            // emits the items in the array.
            .from(getInput())

           // Returns an Observable that emits items based on applying
           // processInput() to each item emitted by the source
           // Observable, where processInput() returns an Observable,
           // and then merging the resulting Observables and emitting
           // the results of this merger as a single Observable.
            .flatMap(inputString ->
                     Observable

                     // Returns an Observable that emits a single
                     // input string and then completes.
                     .just(inputString)

                     // Returns an Observable that applies the
                     // processInput() method to each item emitted by
                     // the source Observable and emits a list of
                     // SearchResults.
                     .map(this::processInput)

                     // Asynchronously subscribes Observers to this
                     // Observable on the computation scheduler.
                     .subscribeOn(Schedulers.computation()))

           // Returns an Observable that emits a single item: a list
           // composed of all the items emitted by the source
           // Observable, which is itself a list of SearchResults.
           .toList()

           // Converts an Observable into a BlockingObservable (an
           // Observable with blocking operators).
           .toBlocking()

           // When the blocking observable emits the single list of
           // SearchResults item and completes, return that list.
           .single();
    }

    /**
     * Search input string for all occurrences of the words to find.
     */
    private List<SearchResults> processInput(String inputString) {
        // Get the title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        return mWordsToFind
            // Sequentially process each word.
            .stream()

            // Map each word to a stream of SearchResults for each
            // time the word is found in the input.
            .map(word -> 
                 // Return a SearchResults containing a match for
                 // each time word appears in the input string.
                 searchForWord(word, input, title))

            // Only keep SearchResults with at least one match.
            .filter(result -> result.size() > 0)

            // Create a list of SearchResults.
            .collect(toList());
    }
}

