package utils;

/**
 * This class is used to time the execution of a statement in
 * a try-resources block.
 */
public class BlockTimer
       implements AutoCloseable {
    /**
     * Records the start time.
     */
    private final long startTime;

    /**
     * Records the elapsed time.
     */
    private long elapsedTime;

    /**
     * Initialize the start time.
     */
    public BlockTimer() {
        this.startTime = System.nanoTime();
    }

    /**
     * Calculate the elapsed time.
     */
    @Override
    public void close() {
        this.elapsedTime = (System.nanoTime() - startTime) / 1_000_000;
    }

    /**
     * @return The elapsed time.
     */
    public long getElapsedTime() {
        return elapsedTime;
    }

    /**
     * @return The elapsed time as a {@link String}.
     */
    @Override
    public String toString() {
        return String.format("%d ms", elapsedTime);
    }
}

