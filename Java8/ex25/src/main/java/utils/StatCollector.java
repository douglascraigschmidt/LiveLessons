package utils;

import java.util.concurrent.atomic.LongAdder;

/**
 * Collect statistics on timing.
 */
public final class StatCollector {
    /** 
     * The amount of time that elapsed.
     */
    private final LongAdder mTime;

    public StatCollector() {
        mTime = new LongAdder();
    }

    public void add(long time) {
        mTime.add(time);
    }

    public void print(String hashMapName, 
                      int numbers) {
        System.out.println("The "
                           + hashMapName
                           + " test took "
                           + mTime.sum()
                           + " milliseconds to find the primality of "
                           + numbers
                           + " numbers");
    }
}
