package livelessons.streamgangs;

import sun.plugin.liveconnect.SecurityContextHelper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

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
 * phrase in an array of phrases.
 */
public class SearchWithRxJavaInputs
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithRxJavaInputs(List<String> phrasesToFind,
                                  List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses an RxJava Stream to
     * concurrently search for phrases in the input data.
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
     * Search input string for all occurrences of the phrases to find.
     */
    private List<SearchResults> processInput(CharSequence inputSeq) {
        // Get the title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input = inputSeq.subSequence(title.length(),
                                                  inputSeq.length());

        return mPhrasesToFind
            // Sequentially process each phrase.
            .stream()

            // Map each phrase to a stream of SearchResults for each
            // time the phrase is found in the input.
            .map(phrase -> 
                 // Return a SearchResults containing a match for
                 // each time phrase appears in the input string.
                 searchForPhrase(phrase, 
                                 input,
                                 title,
                                 false))

            // Only keep a result that has at least one match.
            .filter(((Predicate<SearchResults>) SearchResults::isEmpty).negate())
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Create a list of SearchResults.
            .collect(toList());
    }
}

