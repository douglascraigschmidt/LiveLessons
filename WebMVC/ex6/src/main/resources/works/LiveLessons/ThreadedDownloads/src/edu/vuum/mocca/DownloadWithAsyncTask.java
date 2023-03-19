package edu.vuum.mocca;

import android.graphics.Bitmap;
import android.os.AsyncTask;

/**
 * @class DownloadWithAsyncTask
 *
 * @brief Use the AsyncTask framework to download a bitmap image in a
 *        background Thread and display it to the user in the UI
 *        Thread.  This class plays the role of the "Concrete
 *        Strategy" in the Strategy pattern.
 */
public class DownloadWithAsyncTask implements ButtonStrategy {
    /**
     * AsyncTask subclass that's used to download and display a bitmap
     * image requested by the user.
     */
    private DownloadAsyncTask mDownloader = null;

    /**
     * @class DownloadAsyncTask
     *
     * @brief Extends AsyncTask to download an image in the background
     *        and display it to the user in the UI Thread.
     */
    private static class DownloadAsyncTask 
        extends AsyncTask<String, Integer, Bitmap> {
        /**
         * Context that defines methods used by all concurrency
         * strategies.
         */
        public final DownloadContext mDownloadContext;
        
        /**
         * Constructor stores the DownloadContext for subsequent use.
         */ 
        public DownloadAsyncTask(DownloadContext downloadContext) {
            mDownloadContext = downloadContext;
        }

        /**
         * Called by the AsyncTask framework in the UI Thread to
         * perform initialization actions.
         */
        protected void onPreExecute() {
            // Show the progress dialog before starting the download
            // in a background Thread.
            mDownloadContext.showToast("downloading via AsyncTask");
        }

        /**
         * Download a bitmap image in a background thread.
         * 
         * @param params
         *            The url of a bitmap image
         *
         @ @return The Bitmap representation of the downloaded image.
        */
        protected Bitmap doInBackground(String... urls) {
            // Downlaod the image, which can block since we're in a
            // background thread.
            return mDownloadContext.downloadImage(urls[0]);
        }

        /**
         * Called after an operation executing in the background
         * completes to set the bitmap image to an image view and
         * dismiss the progress dialog.
         * 
         * @param image
         *            The bitmap image
         */
        protected void onPostExecute(Bitmap image) {
            // Display the downloaded image to the user.
            mDownloadContext.displayImage(image);
        }
    };

    /**
     * Create and execute an AsyncTask that downloads the image in a
     * Thread in the pool of Threads.
     */
    @Override
        public void downloadAndDisplayImage(final DownloadContext downloadContext) {
        mDownloader = new DownloadAsyncTask(downloadContext);

        mDownloader.execute(downloadContext.getUrlString());
    }

    /**
     * Cancel a download.
     */
    @Override
        public void cancelDownload(DownloadContext downloadContext) {
        // Let the user know this download is being canceled.
        downloadContext.showToast("Canceling DownloadWithAsyncTask in progress");

        // Cancel the AsyncTask immediately.
        mDownloader.cancel(true);
    }
}
