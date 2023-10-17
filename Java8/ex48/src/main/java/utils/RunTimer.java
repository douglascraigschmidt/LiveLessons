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
    public static BlockTimer timeRun(Runnable runnable) {
        // Create a block timer within a try-with-resources block
        // to track how long the runnable took to run.
        try (BlockTimer blockTimer = new BlockTimer()) {
            // Run the runnable.
            runnable.run();

            return blockTimer;
        }
    }
}

