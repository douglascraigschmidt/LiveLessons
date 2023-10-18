package utils;

/**
 * This class times a {@link Runnable}.
 */
public class RunTimer {
    /**
     * Times a {@link Runnable} and returns the time in milliseconds
     * it took to run the {@link Runnable}
     *
     * @param runnable The {@link Runnable} to time
     * @return The time in milliseconds it took to run the {@link Runnable}
     */
    public static long timeRun(Runnable runnable) {
        // Create a block timer to track how long the runnable took to run.
        BlockTimer blockTimer = new BlockTimer();

        // Use the blockTimer within a try-with-resources block
        // so that it is automatically closed when the try block ends
        // to record the elapsed time.
        try (blockTimer) {
            // Run the runnable.
            runnable.run();
        }

        // Return the elapsed time.
        return blockTimer.getElapsedTime();
    }
}

