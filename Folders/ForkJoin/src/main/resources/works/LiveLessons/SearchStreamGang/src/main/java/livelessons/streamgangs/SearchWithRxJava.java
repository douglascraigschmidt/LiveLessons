package livelessons.streamgangs;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import livelessons.utils.SearchResults;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Customizes the SearchStreamGang framework to use RxJava to perform
 * a parallel search of each input data string and each phrase (from a
 * list of phrases) within each input data string.
 */
public class SearchWithRxJava
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithRxJava(List<String> phrasesToFind,
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
            // Converts input strings list into an Observable that
            // emits the items in the list.
            .fromIterable(getInput())

            // Returns an Observable that emits items based on
            // applying processInput() to each item emitted by the
            // source Observable, where processInput() returns an
            // Observable, and then merging the resulting Observables
            // and emitting the results of this merger as a single
            // Observable.
            .flatMap(inputString -> Observable
                     // Returns an Observable that emits a single
                     // input string and then completes.
                     .just(inputString)

                     // Returns an Observable that applies the
                     // processInput() method to each item emitted by
                     // the source Observable and emits a list of
                     // SearchResults.
                     .flatMap(this::processInput)

                     // Asynchronously subscribes Observers to this
                     // Observable on the computation scheduler.
                     .subscribeOn(Schedulers.from(ForkJoinPool.commonPool())))

            // Returns an Observable that emits a single item, which
            // is composed of all the items emitted by the source
            // Observable, which is itself a list of SearchResults.
            .toList()

            // Converts an Observable into a BlockingObservable (an
            // Observable with blocking operators).
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

            // Returns an Observable that emits items based on applying
            // processPhrase() to each item emitted by the source
            // Observable, where processPhrase() returns an Observable,
            // and then merging the resulting Observables and emitting
            // the results of this merger as a single Observable.
           .flatMap(phrase -> Observable
                    // Returns an Observable that emits a single phrase
                    // and then completes.
                    .just(phrase)
                    
                    .map(__ ->
                         // Return a SearchResults containing a match for
                         // each time phrase appears in the input string.
                         searchForPhrase(phrase, 
                                         input,
                                         title,
                                         false))

                    // Asynchronously subscribes Observers to this
                    // Observable on the computation scheduler.
                    .subscribeOn(Schedulers.from(ForkJoinPool.commonPool())))

            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)

            // Returns an Observable that emits a single item, which
            // is composed of all the items emitted by the source
            // Observable, which is itself a list of SearchResults.
            .toList()

            // And then convert the single back to an observable.
            .toObservable();
    }
}

