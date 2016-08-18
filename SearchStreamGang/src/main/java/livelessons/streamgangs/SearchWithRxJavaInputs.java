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
 * Customizes the SearchStreamGangCommon framework to use
 * CompletableFutures in conjunction with Java Streams to search the
 * input data for each word in an array of words.
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
     * Perform the processing, which uses a Java 8 Stream to
     * concurrently search for words in the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        // Convert the input strings into a list of futures.
        List<List<SearchResults>> result = new ArrayList<>();

        Observable.from(getInput())
                  .map(this::processInputAsync)

                  // @@ Monte, should the call to flatMap() be moved
                  // to a separate statement, i.e., *after* creating
                  // the list of Observables?
                  .flatMap(t -> t)

                  // @@ Monte, is there any way to avoid using
                  // toBlocking() here since that may cause additional
                  // synchronization overhead?
                  .toBlocking()

                  // @@ Monte, could toList() be used here instead of forEach()?
                  .forEach(result::add);

        return result;
    }

    /**
     * Search input string for all occurrences of the words to find.
     */
    private Observable<List<SearchResults>> processInputAsync(String inputString) {
        // Store the title.
        String title = getTitle(inputString);

        // Skip over the title.
        String input = inputString.substring(title.length());

        return Observable.from(mWordsToFind)
                         // @@ Monte, should the call to flatMap() be
                         // moved to a separate statement, i.e.,
                         // *after* creating the list of Observables?
                         .flatMap(word -> Observable.<SearchResults>create(subscriber -> {
                                     // @@ Monte, do we need the intermediate variable or
                                     // can we just say:
                                     // subscriber.onNext(searchForWord(word, input, title));
                                     SearchResults searchResults = searchForWord(word, input, title);
                                     subscriber.onNext(searchResults);
                                     subscriber.onCompleted();
                                 }).subscribeOn(Schedulers.computation()))
                         .toList();
    }
}

