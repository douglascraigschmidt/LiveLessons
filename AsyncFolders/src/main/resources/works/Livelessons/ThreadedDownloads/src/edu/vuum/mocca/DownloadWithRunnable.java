package edu.vuum.mocca;

import android.graphics.Bitmap;

/**
 * @class DownloadWithRunnable
 *
 * @brief Use the Runnables and Handlers defined by the HaMeR
 *        framework to implement the ConcurrencyStrategy interface and
 *        download a bitmap image in a background Thread.  This class
 *        plays the role of the "Concrete Strategy" in the Strategy
 *        pattern.
 */
public class DownloadWithRunnable implements ButtonStrategy {
    /**
     * Thread object that's used for the download.
     */
    private Thread mThread = null;

    /**
     * Creates and starts a new Thread to download an image in the
     * background via a Runnable. The downloaded image is then
     * diplayed in the UI Thread by posting another Runnable via the
     * Activity's runOnUiThread() method, which uses an internal
     * Handler.
     */
    @Override
    public void downloadAndDisplayImage(final DownloadContext downloadContext) {
        Runnable downloadRunnable = new Runnable() {
            /**
             * Download a bitmap image in a background Thread and then
             * post a Runnable to the UI Thread to set the image to
             * the ImageView.
             */
            @Override
            public void run() {
                // Download the image.
                final Bitmap image =
                    downloadContext.downloadImage(downloadContext.getUrlString());

                // Display the downloaded image to the user.
                downloadContext.displayImage(image);
            }
        };

        // Inform the user that the download is starting with
        // this particular concurrency strategy.
        downloadContext.showToast
            ("downloading via Handlers and Runnables");

        // Create and Start a new Thread to perform the download and
        // display the results to the user.
        mThread = new Thread(downloadRunnable);
        mThread.start();
    }

    /**
     * Cancel the download.
     */
    @Override
    public void cancelDownload(DownloadContext downloadContext) {
        // Let the user know this download is being canceled.
        downloadContext.showToast("Canceling DownloadWithRunnable in progress");

        // Interrupt the Thread so it will stop the download.
        mThread.interrupt();
    }
}
