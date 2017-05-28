package livelessons.utils;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used in conjunction with StreamSupport.stream() to
 * create a stream of SearchResults.Result objects that match the
 * number of times a phrase appears in an input string.  The
 * comparison is case-insensitive.
 */
public class PhraseMatchSpliterator
       implements Spliterator<SearchResults.Result> {
    /**
     * The input string.
     */
    private String mInput;

    /**
     * The phrase to search for in the input string.
     */
    private final String mPhrase;

    /**
     * The compiled regular expression pattern.
     */
    private Pattern mPattern;

    /**
     * The phrase matcher.
     */
    private Matcher mPhraseMatcher;

    /**
     * The minimum size of an input string to split.
     */
    private int mMinSplitSize = 0;

    /**
     * Constructor initializes the fields.
     */
    public PhraseMatchSpliterator(String input,
                                  String phrase) {
        // Transform the phrase parameter to a regular expression.
        mPhrase = phrase;
        
        String regexPhrase = phrase
            // Replace multiple spaces with one whitespace
            // boundary expression.
            .trim().replaceAll("\\s+", "\\\\s+")
            // Quote any question marks to avoid problems.
            .replace("?", "\\?");

        // Ignore case and search for phrases that split across lines.
        mPattern = Pattern.compile(regexPhrase,
                                   Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        // Create a regex matcher.
        mPhraseMatcher = mPattern.matcher(input);

        // Initialize the fields.
        mInput = input;
        mMinSplitSize = input.length() / 2;
    }

    /**
     * This constructor is used internally by the trySplit() method
     * below.
     */
    private PhraseMatchSpliterator(String input,
                                   String phrase,
                                   Pattern pattern,
                                   int minSplitSize) {
        mPattern = pattern;
        mPhraseMatcher = mPattern.matcher(input);
        mInput = input;
        mPhrase = phrase;
        mMinSplitSize = minSplitSize;
    }

    /**
     * Attempt to advance the spliterator by one matching phrase.
     */
    @Override
    public boolean tryAdvance(Consumer<? super SearchResults.Result> action) {
        // Try to find a phrase match in the input, ignoring case.  If
        // there's no match then we're done with the iteration.
        if (!mPhraseMatcher.find())
            return false;
        else {
            // Create/accept a new Result object that stores the index
            // of the word.
            action.accept(new SearchResults.Result(mPhraseMatcher.start()));

            // Indicate that the spliterator should continue.
            return true;
        }
    }

    /**
     * Attempt to split the input so phrases can be matched
     * concurrently.
     */
    @Override
    public Spliterator<SearchResults.Result> trySplit() {
        int currentSize = mInput.length();
        int phraseLength = mPhrase.length();
        int patternPhraseLength = mPattern.toString().length();

        // Bail out if the input is too small to split further.
        if (currentSize < mMinSplitSize)
            return null;

        // Compute the candidate split point.
        int splitPos = currentSize / 2;

        // Subtract the phrase length so we can check to make sure the
        // phrase doesn't span across splitPos.
        int startPos = splitPos - phraseLength;

        // Check if phrase is too long for this segment.
        if (startPos < 0
            || phraseLength > splitPos) {
            return null;
        }

        // Handle the case where a phrase spans across the initial
        // splitPos.
        String substr = mInput.substring(startPos,
                                         startPos + patternPhraseLength);

        // Try to match the pattern in the substr.
        Matcher phraseMatcher = mPattern.matcher(substr);

        // Check to see if the phrase matches.
        if (phraseMatcher.find()) 
            // Update the splitPos to account for the phrase that
            // spans newlines.
            splitPos = startPos + phraseMatcher.start() + phraseMatcher.group().length();

        // Split the input at the appropriate location.
        String rightHandSide = mInput.substring(0, splitPos);
        mInput = mInput.substring(splitPos);

        // Create a new PhraseMatchSpliterator object that handles the
        // "right hand" portion of the input, while the "this" object
        // handles the "left hand" portion of the input.
        return new PhraseMatchSpliterator(rightHandSide,
                                          mPhrase,
                                          mPattern,
                                          mMinSplitSize);
    }

    /**
     * Estimate the current size of the remaining input.
     */
    @Override
    public long estimateSize() {
        return mInput.length();
    }

    /**
     * The characteristics for this spliterator.
     */
    @Override
    public int characteristics() {
        return ORDERED + SIZED + SUBSIZED + NONNULL + IMMUTABLE;
    }
}
