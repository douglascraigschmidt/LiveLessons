package edu.vuum.mocca;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

/**
 * @class DownloadWithMessages
 *
 * @brief Use the Handlers and Messages defined by the HaMeR framework
 *        to download a bitmap image in a background Thread and then
 *        displays it.  This class plays the role of the "Concrete
 *        Strategy" in the Strategy pattern.
 */
public class DownloadWithMessages implements ButtonStrategy {
    /**
     * Thread object that's used for the download.
     */
    private Thread mThread = null;

    /**
     * Creates and starts a Thread that downloads an image in the
     * background and then use Messages to display the image in the UI
     * Thread.
     */
    @Override
    public void downloadAndDisplayImage(final DownloadContext downloadContext) {
        // Object used to handle messages sent to it via the
        // background Thread.
        final MessageHandler messageHandler =
            new MessageHandler(downloadContext);

        Runnable downloadRunnable = new Runnable() {
            /**
             * Download a bitmap image in a background Thread by
             * sending Messages to the mHandler running in the UI
             * Thread.
             */
            @Override
            public void run() {
                // Factory creates a Message that instructs the
                // MessageHandler to post the toast to the user.
                Message msg =
                    messageHandler.obtainMessage(MessageHandler.SHOW_TOAST);

                // Send the Message to show the toast.
                messageHandler.sendMessage(msg);

                // Download the image.
                final Bitmap image = 
                    downloadContext.downloadImage(downloadContext.getUrlString());

                // Factory creates a Message that instructs the
                // MessageHandler to display the image to the user.
                msg =
                    messageHandler.obtainMessage(MessageHandler.DISPLAY_IMAGE,
                                                 image);

                // Send the Message to instruct the UI Thread to
                // display the image.
                messageHandler.sendMessage(msg);
            }
            };

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
        downloadContext.showToast("Canceling DownloadWithMessages in progress");

        // Interrupt the Thread so it will stop the download.
        mThread.interrupt();
    }

    /**
     * @class MessageHandler
     *
     * @brief A static inner class that inherits from Handler and uses
     *        its handleMessage() hook method to process Messages sent
     *        to it from a background Thread. Since it's static its
     *        instances do not hold implicit references to their outer
     *        classes.
     */
    private static class MessageHandler extends Handler {
        /**
         * Types of Messages that can be passed from a background
         * Thread to the UI Thread to specify which processing to
         * perform.
         */
        public static final int SHOW_TOAST = 1;
        public static final int DISPLAY_IMAGE = 2;

        /**
         * Ensure that garbage collection occurs properly.
         */
        final private WeakReference<DownloadContext> mContext;

        /**
         * Constructor that assigns the WeakReference.
         */
        public MessageHandler(DownloadContext downloadContext) {
            mContext =
                new WeakReference<DownloadContext>(downloadContext);
        }

        /**
         * Process the specified Messages passed to MessageHandler in
         * the UI Thread.  These Messages instruct the Handler to
         * start showing the progress dialog, dismiss it, or display
         * the designated bitmap image via the ImageView.
         */
        public void handleMessage(Message msg) {
            DownloadContext context = mContext.get();

            if (context == null)
                return;
                    
            switch (msg.what) {

            case SHOW_TOAST:
                context.showToast("downloading via Handlers and Messages");
                break;

            case DISPLAY_IMAGE:
                // Display the downloaded image to the user.
                context.displayImage((Bitmap) msg.obj);
                break;
            }
        }
    }
}
