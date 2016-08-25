import java.util.Spliterators;
import java.util.function.Consumer;

import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.ORDERED;

/**
 * Creates a Stream of matches to a word in the input data.
 */
public class WordMatcherSpliterator
       extends Spliterators.AbstractSpliterator<Integer> {
    /**
     * Matches a word in the input data.
     */
    private final WordMatcher mMatcher;

    /**
     * Constructor initializes the field and super class.
     */
    public WordMatcherSpliterator(WordMatcher matcher) {
        super(Long.MAX_VALUE, ORDERED | NONNULL);
        mMatcher = matcher;
    }

    /**
     * Attempt to advance the spliterator by one position.
     */
    public boolean tryAdvance(Consumer<? super Integer> action) {
        // If there's no match then we're done with the iteration.
        if (!mMatcher.find())
            return false;

        // Store the index of the match in the action and keep the
        // iteration going.
        action.accept(mMatcher.next());
        return true;
    }
}

