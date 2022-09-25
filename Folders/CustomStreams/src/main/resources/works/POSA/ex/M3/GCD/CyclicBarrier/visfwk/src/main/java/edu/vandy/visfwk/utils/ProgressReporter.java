package edu.vandy.visfwk.utils;

/**
 * Report mProgressStatus by running the Runnable in the right
 * context (e.g., the UI or main thread).
 */
public interface ProgressReporter {
    /**
     * Report mProgressStatus in the right context.
     */
    default void updateProgress(Runnable runnable) {
        runnable.run();
    }
}

