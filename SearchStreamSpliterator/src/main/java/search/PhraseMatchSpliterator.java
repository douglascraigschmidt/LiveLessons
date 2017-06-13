package search;

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
    private CharSequence mInput;

    /**
     * The phrase to search for in the input string.
     */
    private final String mPhrase;

    /**
     * The compiled regular expression pattern.
     */
    private final Pattern mPattern;

    /**
     * The phrase matcher.
     */
    private Matcher mPhraseMatcher;

    /**
     * The minimum size of an input string to split.
     */
    private final int mMinSplitSize;

    /**
     * Constructor initializes the fields.
     */
    public PhraseMatchSpliterator(CharSequence input,
                                  String phrase) {
        // Transform the phrase parameter to a regular expression.
        mPhrase = phrase;
        
        // Create a regex that will match the phrase across lines.
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
     * This constructor is used internally by the trySplit() method.
     * It initializes all the fields for the "left hand size" of a
     * split.
     */
    private PhraseMatchSpliterator(CharSequence input,
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
            // of the phrase.
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
        // Bail out if the input is too small to split further.
        if (mInput.length() < mMinSplitSize)
            return null;

        // Compute a candidate position for splitting the input.
        int startPos, splitPos = mInput.length() / 2;

        // Get the position to start determining if a phrase spans the
        // split position.
        if ((startPos = computeStartPos(splitPos)) < 0)
            return null;

        // Update splitPos if a phrase spans across the initial
        // splitPos.
        splitPos = tryToUpdateSplitPos(startPos, splitPos);

        // Create a new PhraseMatchSpliterator that handles the "left
        // hand" portion of the input, while the "this" object handles
        // the "right hand" portion of the input.
        return splitInput(splitPos);
    }

    /**
     * Determine the position to start determining if a phrase spans
     * the split position.  Returns -1 if the phrase is too long for
     * the input.
     */
    private int computeStartPos(int splitPos) {
        // Length of the phrase in non-regex characters.
        int phraseLength = mPhrase.length();
        
        // Subtract the phrase length so we can check to make sure the
        // phrase doesn't span across splitPos.
        int startPos = splitPos - phraseLength;

        // Check if phrase is too long for this input segment.
        if (startPos < 0 || phraseLength > splitPos) 
            return -1;
        else
            return startPos;
    }

    /**
     * Update splitPos if a phrase spans across the initial splitPos.
     */
    private int tryToUpdateSplitPos(int startPos,
                                    int splitPos) {
        // Create a substring to check for the case where a phrase
        // spans across the initial splitPos.
        CharSequence substr =
            mInput.subSequence(startPos,
                               // Add length of the phrase in regex
                               // characters.
                               startPos + mPattern.toString().length());

        // Create a pattern matcher for the substring.
        Matcher phraseMatcher = mPattern.matcher(substr);

        // Check to see if the phrase matches within the subtring.
        if (phraseMatcher.find()) 
            // If there's a match update the splitPos to account for
            // the phrase that spans newlines.
            splitPos = startPos 
                + phraseMatcher.start() 
                + phraseMatcher.group().length();

        return splitPos;
    }

    /**
     * Return a new PhraseMatchSpliterator that handles the "left
     * hand" portion of the input, while the "this" object handles the
     * "right hand" portion of the input.
     */
    private PhraseMatchSpliterator splitInput(int splitPos) {
        // Split the input at the appropriate location.
        CharSequence leftHandSide = mInput.subSequence(0, splitPos);

        // Update this field to account for the shorter input on the
        // "right hand" portion.
        mInput = mInput.subSequence(splitPos, mInput.length());

        // Update this field to handle the shorter input.
        mPhraseMatcher = mPattern.matcher(mInput);

        // Create a new PhraseMatchSpliterator that handles the "left
        // hand" portion of the input, while the "this" object handles
        // the "right hand" portion of the input.
        return new PhraseMatchSpliterator(leftHandSide,
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
