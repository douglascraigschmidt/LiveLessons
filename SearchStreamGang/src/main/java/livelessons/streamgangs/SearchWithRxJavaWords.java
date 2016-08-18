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
 * Customizes the SearchStreamGangCommon framework to use
 * CompletableFutures in conjunction with Java Streams to search how
 * many times each word in an array of words appears in input data.
 * words.
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
        return Observable.from(mWordsToFind)
                .flatMap(val -> Observable.just(val)
                        .subscribeOn(Schedulers.computation())
                        .map(this::doSearch))
                .toList()
                .toBlocking()
                .first();
    }

    private List<SearchResults> doSearch(String word) {
        return getInput().stream()
                .map(input -> {
                    String title = getTitle(input);
                    String data = input.substring(title.length());
                    return searchForWord(word, data, title);
                })
                .filter(result -> result.size() > 0)
                .collect(toList());
    }
}
