package edu.vandy.mathservices.common;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.List;

/**
 * This class converts a {@link List} of {@link Integer} objects into
 * a stream of two-element {@link Integer} arrays.
 */
public class ListSpliterator
       extends Spliterators.AbstractSpliterator<Integer[]> {
    /**
     * The {@link List} of {@link Integer} objects to process.
     */
    private final List<Integer> mInts;

    /**
     * The start of the next pair of {@link Integer} objects in the
     * {@link List} to process.
     */
    private int mIndex;

    /**
     * The constructor initializes the field.
     */
    public ListSpliterator(List<Integer> mInts) {
        super(0, 0);
        this.mInts = mInts;
    }

    /**
     * Attempts to extract the next pair of {@link Integer} objects
     * from the {@link List}.
     *
     * @param action Yields a two-element array of {@link Integer}
     *               objects
     * @return False if there are no more pairs of {@link Integer}
     *         objects to process, else true
     */
    @Override
    public boolean tryAdvance(Consumer<? super Integer[]> action) {
        // Return false and terminate the spliterator if all elements
        // have been processed.
        if (mIndex >= mInts.size())
            return false;
        else {
            // Create a new two-element array of Integer objects and
            // return it via the action Consumer.
            action.accept(new Integer[]{mInts.get(mIndex),
                                        mInts.get(mIndex + 1)});

            // Split over the two elements that were just processed.
            mIndex += 2;
            
            // Keep going.
            return true;
        }
    }
}

