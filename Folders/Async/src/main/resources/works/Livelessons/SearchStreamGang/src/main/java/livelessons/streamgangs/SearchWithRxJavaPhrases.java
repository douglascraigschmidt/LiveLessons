package livelessons.streamgangs;

import livelessons.utils.SearchResults;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static livelessons.utils.StreamsUtils.not;

/**
 * Customizes the SearchStreamGang framework to use RxJava in
 * conjunction with Java Streams to search how many times each phrase in
 * an array of phrases appears in input data.
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
           .from(mPhrasesToFind)

           // Returns an Observable that emits items based on applying
           // processPhrase() to each item emitted by the source
           // Observable, where processPhrase() returns an Observable,
           // and then merging the resulting Observables and emitting
           // the results of this merger as a single Observable.
           .flatMap(phrase ->
                    Observable
                    // Returns an Observable that emits a single phrase
                    // and then completes.
                    .just(phrase)

                    // Returns an Observable that applies the
                    // processPhrase() method to each item emitted by
                    // the source Observable and emits a list of
                    // SearchResults.
                    .map(this::processPhrase)

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
     * Search all the input strings for all occurrences of the phrase to
     * find.
     */
    private List<SearchResults> processPhrase(String phrase) {
     	// Get the input.
        return getInput()
            // Sequentially process each String in the input list.
            .stream()

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
            .filter(not(SearchResults::isEmpty))
            // Filtering can also be done as
            // .filter(result -> result.size() > 0)

            // Create a list of SearchResults.
            .collect(toList());
    }
}
