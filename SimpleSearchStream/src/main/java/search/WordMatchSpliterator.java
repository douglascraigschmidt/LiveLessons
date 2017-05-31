package search;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used in conjunction with StreamSupport.stream() to
 * create a stream of SearchResults.Result objects that match the
 * number of times a word appears in an input string.
 */
public class WordMatchSpliterator
       extends Spliterators.AbstractSpliterator<SearchResults.Result> {
    /**
     * The phrase matcher.
     */
    private final Matcher mWordMatcher;
    
    /**
     * Constructor initializes the fields and super class.
     */
    public WordMatchSpliterator(String input,
                                String word) {
        super(Long.MAX_VALUE, ORDERED | NONNULL);

        // Create a regex that matches on a word boundary.
        String regexWord = 
            "\\b"
            + word.trim()
            + "\\b";

        mWordMatcher = Pattern
            // Compile the regex, which will ignore case.
            .compile(regexWord,
                     Pattern.CASE_INSENSITIVE)

            // Create a Matcher for the regex on the input.
            .matcher(input);
    }

    /**
     * Attempt to advance the spliterator by one position.
     */
    public boolean tryAdvance(Consumer<? super SearchResults.Result> action) {
        // If there's no match then we're done with the iteration.
        if (!mWordMatcher.find())
            return false;
        else {
            // Create a new Result object indicating where the index
            // starts.
            action.accept(new SearchResults.Result(mWordMatcher.start()));
            return true;
        }
    }        
}

