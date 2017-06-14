package utils;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

/**
 * Defines a Matcher-based spliterator implementation, as per
 * https://stackoverflow.com/questions/24660888/collect-hashset-java-8-regex-pattern-stream-api/24663422#24663422.
 * This feature will be supported natively as of JDK 9, as per
 * https://bugs.openjdk.java.net/browse/JDK-8071479.
 */
public class MatcherSpliterator 
       extends Spliterators.AbstractSpliterator<MatchResult> {
    /**
     * The Matcher used to generate the stream.
     */
    private final Matcher mMatcher;

    /**
     * Constructor initializes the fields.
     */
    public MatcherSpliterator(Matcher matcher) {
        super(Long.MAX_VALUE,
              ORDERED | NONNULL | IMMUTABLE);
        mMatcher = matcher;
    }

    /**
     * If there's another match by the Matcher store the result into
     * the given action, returning true; else returns false.
     */
    @Override 
    public boolean tryAdvance(Consumer<? super MatchResult> action) {
        if (!mMatcher.find()) 
            return false;
        else {
            action.accept(mMatcher.toMatchResult());
            return true;
        }
    }
}
