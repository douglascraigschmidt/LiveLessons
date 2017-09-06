package vandy.mooc.buggydownloader.downloaders;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import vandy.mooc.buggydownloader.activities.DownloadActivity;

/**
 * This class downloads a bitmap image in the background using
 * handlers and messages.
 */
public class DownloadWithMessages 
       implements Runnable {
    /**
     * The URL to download. 
     */ 
    private final String mUrl;

    /**
     * WeakReference enable garbage collection of Activity.
     */
    private final WeakReference<DownloadActivity> mActivity;
    
    /*
     * Types of Messages that can be passed from a background
     * Thread to the UI Thread to specify which processing to
     * perform.
     */
    private static final int SHOW_DIALOG = 1;
    private static final int DISMISS_DIALOG = 2;
    private static final int DISPLAY_IMAGE = 3;

    /**
     * Extends Handler and uses its handleMessage() hook method to
     * process Messages sent to it from a background Thread.
     */
    private final Handler mMessageHandler =
        new Handler() {
            /**
             * Process the specified Messages passed to MessageHandler
             * in the UI Thread. These Messages instruct the Handler
             * to start showing the progress dialog, dismiss it, or
             * display the designated bitmap image via the ImageView.
             */
            public void handleMessage(Message msg) {
                // Check to see if the activity still exists and return if
                // not.
                switch (msg.what) {
                case SHOW_DIALOG:
                    // Inform the user that the download is starting.
                    mActivity.get().showDialog("downloading via Handlers and Messages");
                    break;

                case DISMISS_DIALOG:
                    // Dismiss the progress dialog.
                    mActivity.get().dismissDialog();
                    break;

                case DISPLAY_IMAGE:
                    // Display the downloaded image to the user.
                    mActivity.get().displayBitmap((Bitmap) msg.obj);
                    break;
                }
            }
        };

    /**
     * Class initializes the fields.
     * 
     * @param activity The enclosing Activity
     * @param url      The bitmap image url
     */
    public DownloadWithMessages(DownloadActivity activity,
                                String url) {
        mUrl = url;
        mActivity = new WeakReference<>(activity);
    }

    /**
     * Download a bitmap image in a background thread. It sends
     * various messages to the mHandler running in the UI thread.
     */
    public void run() {
        // Factory creates a message that instructs mMessageHandler to
        // begin showing the progress dialog to the user.
        Message msg =
            mMessageHandler.obtainMessage(SHOW_DIALOG);

        // Send the message to initiate the progress dialog.
        mMessageHandler.sendMessage(msg);

        // Download the image.
        final Bitmap image =
            mActivity.get().downloadBitmap(mUrl);

        // Factory creates a message that instructs the
        // mMessageHandler to dismiss the progress dialog.
        msg = mMessageHandler.obtainMessage(DISMISS_DIALOG);

        // Send the message to dismiss the progress dialog.
        mMessageHandler.sendMessage(msg);

        // Factory creates a message that instructs mMessageHandler to
        // display the image to the user.
        msg = mMessageHandler.obtainMessage(DISPLAY_IMAGE,
                                            image);

        // Send the message to instruct the UI thread to display the
        // image.
        mMessageHandler.sendMessage(msg);
    }
}

