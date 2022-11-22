package utils;

import java.io.File;
import java.net.URL;

/**
 * This Java utility class defines methods that download images from a
 * given {@link URL}.
 */
public class DownloadUtils {
    /**
     * A Java utility class should have a private constructor.
     */
    private DownloadUtils() {}

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

