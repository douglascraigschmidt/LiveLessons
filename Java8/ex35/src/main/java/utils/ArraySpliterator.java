package utils;

import java.util.Spliterators;
import java.util.function.Consumer;

/**
 *
 */
public class ArraySpliterator
       extends Spliterators.AbstractSpliterator<Integer[]> {
    /**
     *
     */
    private final Integer[] mInts;

    /**
     *
     */
    private int mIndex;

    /**
     * @param mInts
     */
    public ArraySpliterator(Integer[] mInts) {
        super(0, 0);
        this.mInts = mInts;
    }

    /**
     *
     */
    @Override
    public boolean tryAdvance(Consumer<? super Integer[]> action) {
        if (mIndex >= mInts.length)
            return false;
        else {
            action.accept(new Integer[]{mInts[mIndex], mInts[mIndex + 1]});
            mIndex += 2;
            return true;
        }
    }
}

