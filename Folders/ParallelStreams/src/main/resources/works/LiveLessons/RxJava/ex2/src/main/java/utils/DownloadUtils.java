package utils;

import java.io.File;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

/**
 * This Java utility class ..
 */
public class DownloadUtils {
    /**
     * Transform the {@code url} to a {@code File} by downloading each
     * image via its URL and storing it to the local file system.
     * This method does not use the {@code ManagedBlocker} mechanism
     * in the Java fork-join framework, i.e., the pool of worker
     * threads will not be expanded when blocking on I/O occurs.
     */
    public static File downloadAndStoreImage(URL url) {
        return
                // Perform a blocking image download.
                downloadImage(url)

                        // Store the image on the local device.
                        .store();
    }

    /**
     * Transform the {@code url} to a {@code File} by downloading each
     * image via its URL and storing it to the local file system.
     * This method uses the {@code ManagedBlocker} mechanism in the
     * Java fork-join framework, which adds new worker threads to the
     * pool adaptively when blocking on I/O occurs.
     */
    public static File downloadAndStoreImageMB(URL url) {
        // Create a one element array so we can update it in the
        // anonymous inner class instance below.
        final Image[] image = new Image[1];

        try {
            ForkJoinPool
                    // Submit an anonymous managedBlock implementation to
                    // the common fork-join thread pool.  This call
                    // ensures the common fork-join thread pool is
                    // expanded to handle the blocking image download.
                    .managedBlock(new ForkJoinPool.ManagedBlocker() {
                        /**
                         * Download the image, which will block the
                         * calling thread.
                         */
                        @Override
                        public boolean block() {
                            image[0] = downloadImage(url);
                            return true;
                        }

                        /**
                         * Always return false.
                         */
                        @Override
                        public boolean isReleasable() {
                            return false;
                        }
                    });
        } catch (InterruptedException e) {
            throw new Error(e);
        }

        // Store and return the image on the local device.
        return image[0].store();
    }

    /**
     * Transform the {@code url} to a {@code File} by downloading each
     * image via its URL and storing it on the local file system.
     * This method uses the {@code BlockingTask} wrapper around the
     * {@code ManagedBlocker} mechanism in the Java fork-join
     * framework, which adds new worker threads to the pool adaptively
     * when blocking on I/O occurs.
     */
    public static File downloadAndStoreImageBT(URL url) {
        return BlockingTask
                // This call ensures the common fork-join thread pool
                // is expanded to handle the blocking image download.
                .callInManagedBlock(() -> downloadImage(url))

                // Store the image on the local device.
                .store();
    }

    /**
     * Factory method that retrieves the image associated with the
     * {@code url} and creates an {@code Image} to encapsulate it.
     * This method blocks until I/O has completed.
     */
    public static Image downloadImage(URL url) {
        return new Image(url,
                // Download the content from the url.
                NetUtils.downloadContent(url));
    }
}

