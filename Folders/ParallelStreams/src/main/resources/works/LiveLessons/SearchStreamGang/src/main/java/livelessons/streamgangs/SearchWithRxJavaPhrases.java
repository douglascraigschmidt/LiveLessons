package livelessons.streamgangs;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import livelessons.utils.SearchResults;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang framework to use RxJava to search
 * in parallel how many times each phrase in an array of phrases
 * appears in input data.
 */
public class SearchWithRxJavaPhrases
        extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithRxJavaPhrases(List<String> phrasesToFind,
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
            // Converts mPhrasesToFind array into an Observable that
            // emits the items in the array.
            .fromIterable(mPhrasesToFind)

            // Return an Observable that applies the searchForPhrase()
            // method to each item emitted by the source Observable
            // and uses flatMap() to create one Observable that emits
            // SearchResults.
            .flatMap(phrase -> Observable
                     // Returns an Observable that emits a single phrase
                     // and then completes.
                     .just(phrase)

                     // Returns an Observable that applies the
                     // processPhrase() method to each item emitted by
                     // the source Observable and emits a list of
                     // SearchResults.
                     .flatMap(this::processPhrase)

                     // Asynchronously subscribes Observers to this
                     // Observable on the computation scheduler.
                     .subscribeOn(Schedulers.from(ForkJoinPool.commonPool())))

            // Returns an Observable that emits a single list composed
            // of all the items emitted by the source Observable,
            // which is itself a list of SearchResults.
            .toList()

            // Converts an Observable into a BlockingObservable (an
            // Observable with blocking operators) and then block
            // until the final result is available.
            .blockingGet();
    }

    /**
     * Search all the input strings for all occurrences of the phrase to
     * find.
     */
    private Observable<List<SearchResults>> processPhrase(String phrase) {
     	// Get the input.
        return Observable
            // Converts input strings list into an Observable that
            // emits the items in the list.
            .fromIterable(getInput())

            // Map each input string to a stream of SearchResults for
            // each time the phrase appears in the input.
            .map(inputSeq -> {
                    String title = getTitle(inputSeq);

                    // Skip over the title.
                    CharSequence input = inputSeq.subSequence(title.length(),
                                                              inputSeq.length());

                    // Return a SearchResults containing a match for
                    // each time phrase appears in the input string.
                    return searchForPhrase(phrase, 
                                           input,
                                           title,
                                           false);
                })

            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)

            // Returns a Single that emits a single item, which is
            // composed of all the items emitted by the source
            // Observable, which is itself a list of SearchResults.
            .toList()

            // And then convert the single back to an observable.
            .toObservable();
    }
}
