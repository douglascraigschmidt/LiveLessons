package livelessons.streamgangs;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import livelessons.utils.SearchResults;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Customizes the SearchStreamGang framework to use Project Reactor to
 * perform a parallel search of each each phrase (from a list of
 * phrases) within each input data string.
 */
public class SearchWithReactorPhrases
       extends SearchStreamGang {
    /**
     * Constructor initializes the super class.
     */
    public SearchWithReactorPhrases(List<String> phrasesToFind,
                                    List<List<CharSequence>> stringsToSearch) {
        // Pass input to superclass constructor.
        super(phrasesToFind,
              stringsToSearch);
    }

    /**
     * Perform the processing, which uses an Reactor Stream to search
     * for phrases in parallel from the input data.
     */
    @Override
    protected List<List<SearchResults>> processStream() {
        return Flux
            // Converts the input strings list into an Flux that
            // emits the items in the list.
            .fromIterable(getInput())

            // Returns a Flux that emits items based on applying
            // processInput() to each item emitted by the source Flux,
            // where processInput() returns a Flux, and then using
            // flatMap() to merge the resulting Fluxes and emitting the
            // results of this merger as a single Flux.
            .flatMap(inputString -> Mono
                     // Returns a Mono that emits a single
                     // input string and then completes.
                     .just(inputString)

                     // Returns a Mono that applies the
                     // processInput() method to each item emitted by
                     // the source Mono and emits a list of
                     // SearchResults.
                     .flatMap(this::processInput))

            // Returns an Mono that emits a single item, which
            // is composed of all the items emitted by the source
            // Flux, which is itself a list of SearchResults.
            .collectList()

            // Blocks until all the computations are done.
            .block();
    }

    /**
     * Search in parallel the {@code inputSeq} for all occurrences of
     * the phrases to find.
     * @return A Mono to a list of search results found in the {@code inputSeq}.
     */
    private Mono<List<SearchResults>> processInput(CharSequence inputSeq) {
        // Get the title.
        String title = getTitle(inputSeq);

        // Skip over the title.
        CharSequence input = inputSeq.subSequence(title.length(),
                                                  inputSeq.length());

        return Flux
            // Converts mPhrasesToFind list into an Flux that
            // emits the items in the list.
            .fromIterable(mPhrasesToFind)

            // Returns an Flux that emits items based on applying
            // processPhrase() to each item emitted by the source
            // Flux, where processPhrase() returns an Flux,
            // and then merging the resulting Fluxs and emitting
            // the results of this merger as a single Flux.
           .flatMap(phrase -> Mono
                    // Returns an Flux that emits a single phrase
                    // and then completes.
                    .just(phrase)

                    // Return a SearchResults containing a match for
                    // each time phrase appears in the input string.
                    .map(__ -> searchForPhrase(phrase,
                                               input,
                                               title,
                                               false))

                    // Asynchronously subscribes Monos to this
                    // Mono on the given scheduler.
                    .subscribeOn(Schedulers.fromExecutor(ForkJoinPool.commonPool())))

            // Only keep a result that has at least one match.
            .filter(result -> result.size() > 0)

            // Returns an Mono that emits a single item, which
            // is composed of all the items emitted by the source
            // Flux, which is itself a list of SearchResults.
            .collectList();
    }
}

