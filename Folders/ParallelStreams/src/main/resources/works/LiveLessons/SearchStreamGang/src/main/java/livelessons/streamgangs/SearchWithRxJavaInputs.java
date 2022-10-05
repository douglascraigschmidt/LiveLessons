package livelessons.streamgangs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import javax.naming.directory.SearchResult;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import livelessons.utils.SearchResults;
import livelessons.utils.StreamsUtils;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang framework to use RxJava search in
 * parallel the input data for each phrase in an array of phrases.
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
     * Perform the processing, which uses an RxJava Stream to search
     * in parallel for phrases in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        return Observable
            // Converts input strings array into an Observable that
            // emits the items in the array.
            .fromIterable(getInput())

            // Returns an Observable that emits items based on
            // applying processInput() to each item emitted by the
            // source Observable, where processInput() returns an
            // Observable, and then using flatMap() to merge the
            // resulting Observables into a single list of lists of
            // search results.
            .flatMap(inputString -> Observable
                     // Returns an Observable that emits a single
                     // input string and then completes.
                     .just(inputString)

                     // Return an Observable that applies the
                     // processInput() method to each item emitted by
                     // the source Observable and uses flatMap() to
                     // create one Observable that emits a list of
                     // SearchResults.
                     .flatMap(this::processInput)

                     // Asynchronously subscribes Observers to this
                     // Observable on the computation scheduler.
                     .subscribeOn(Schedulers.from(ForkJoinPool.commonPool())))

            // Returns an Observable that emits a single list
            // composed of all the items emitted by the source
            // Observable, which is itself a list of SearchResults.
            .toList()

            // Converts an Observable into a BlockingObservable (an
            // Observable with blocking operators) and then block
            // until the final result is available.
            .blockingGet();
    }

    /**
     * Search input string for all occurrences of the phrases to find.
     */
    private Observable<List<SearchResults>> processInput(CharSequence inputSeq) {
        // Get the title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input = inputSeq.subSequence(title.length(),
                                                  inputSeq.length());

        return Observable
            // Converts mPhrasesToFind list into an Observable that
            // emits the items in the list.
            .fromIterable(mPhrasesToFind)

            // Return an Observable that applies the searchForPhrase()
            // method to each item emitted by the source Observable
            // and uses flatMap() to create one Observable that emits
            // SearchResults.
           .flatMap(phrase -> Observable
                    // Returns an Observable that emits a single
                    // phrase and then completes.
                    .just(phrase)

                   // Return a SearchResults containing a match for
                   // each time phrase appears in the input string.
                    .map(__ -> searchForPhrase(phrase,
                                               input,
                                               title,
                                               false)))

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

