package common;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.List;

/**
 * This class converts a {@link List} of {@link Integer} objects into
 * a stream of {@link GCDParam} objects.
 */
public class ListSpliterator
       extends Spliterators.AbstractSpliterator<GCDParam> {
    /**
     * The {@link List} of {@link Integer} objects to process.
     */
    private final List<Integer> mIntegers;

    /**
     * The start of the next pair of {@link Integer} objects in the
     * {@link List} to process.
     */
    private int mIndex;

    /**
     * The constructor initializes the field.
     */
    public ListSpliterator(List<Integer> integers) {
        super(0, 0);
        mIntegers = integers;
    }

    /**
     * Attempts to extract the next pair of {@link Integer} objects
     * from the {@link List}.
     *
     * @param action Yields a {@link GCDParam}
     * @return False if there are no more pairs of {@link Integer}
     *         objects to process, else true
     */
    @Override
    public boolean tryAdvance(Consumer<? super GCDParam> action) {
        // Return false if all elements have been processed, which
        // terminates the spliterator.
        if (mIndex >= mIntegers.size())
            return false;
        else {
            // Create a new two-element array of Integer objects and
            // return it via the action Consumer.
            action.accept(new GCDParam(mIntegers.get(mIndex),
                                       mIntegers.get(mIndex + 1)));

            // Split over the two elements that were just processed.
            mIndex += 2;
            
            // Keep going.
            return true;
        }
    }
}

