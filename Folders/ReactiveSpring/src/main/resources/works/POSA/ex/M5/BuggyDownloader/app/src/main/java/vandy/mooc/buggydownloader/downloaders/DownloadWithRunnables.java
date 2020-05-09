package vandy.mooc.buggydownloader.downloaders;

import java.lang.ref.WeakReference;

import android.graphics.Bitmap;

import vandy.mooc.buggydownloader.activities.DownloadActivity;

/**
 * This class downloads/displays a bitmap image using runnables.
 */
public class DownloadWithRunnables
       implements Runnable {

    // The URL to download. 
    private final String mUrl;

    /**
     * WeakReference enable garbage collection of Activity.
     */
    private final WeakReference<DownloadActivity> mActivity;

    /**
     * Class initializes the fields.
     * 
     * @param activity The enclosing Activity
     * @param url      The bitmap image url
     */
    public DownloadWithRunnables(DownloadActivity activity,
                                 String url) {
        mUrl = url;
        mActivity = 
            new WeakReference<>(activity);
    }

    /**
     * Download a bitmap image in the background.  It also displays
     * the image and dismisses the progress dialog.
     */
    public void run() {
        // Download the bitmap image.
        final Bitmap image =
            mActivity.get().downloadBitmap(mUrl);

        // Display the image in the UI thread.
        mActivity.get().runOnUiThread(() -> {
                // Dismiss the progress dialog.
                mActivity.get().dismissDialog();
                   
                // Display the downloaded image to the user.
                mActivity.get().displayBitmap(image);
            });
    }
}

