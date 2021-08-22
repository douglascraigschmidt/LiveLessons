package utils;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.List;

/**
 *
 */
public class ListSpliterator
       extends Spliterators.AbstractSpliterator<Integer[]> {
    /**
     *
     */
    private final List<Integer> mInts;

    /**
     *
     */
    private int mIndex;

    /**
     * @param mInts
     */
    public ListSpliterator(List<Integer> mInts) {
        super(0, 0);
        this.mInts = mInts;
    }

    /**
     *
     */
    @Override
    public boolean tryAdvance(Consumer<? super Integer[]> action) {
        if (mIndex >= mInts.size())
            return false;
        else {
            action.accept(new Integer[]{mInts.get(mIndex),
                                        mInts.get(mIndex + 1)});
            mIndex += 2;
            return true;
        }
    }
}

