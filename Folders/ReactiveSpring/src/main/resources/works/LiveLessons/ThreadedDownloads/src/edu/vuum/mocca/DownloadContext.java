package edu.vuum.mocca;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @class DownloadContext
 *
 * @brief Class that defines methods shared by all the
 *        ConcurrencyStrategy implementations.  It plays the role of
 *        the "Context" in the Strategy pattern.
 */
class DownloadContext {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    private final String TAG =
        DownloadContext.class.getSimpleName();

    /**
     * User's selection of URL to download.
     */
    private final WeakReference<EditText> mUrlEditText;

    /**
     * Image that will be displayed to the user.
     */
    private final WeakReference<ImageView> mImageView;

    /**
     * Activity object used for UI operations.
     */
    private final WeakReference<Activity> mActivity;

    /**
     * The completion command called after the image has been
     * displayed.
     */
    private final Runnable mCompletionCommand;

    /**
     * Default URL to download.
     */
    private final String mDefaultURL = 
        "http://www.dre.vanderbilt.edu/~schmidt/ka.png";

    /**
     * Constructor sets the various data members used by concrete
     * ButtonStrategies.
     */
    public DownloadContext(EditText editText,
                           ImageView imageView,
                           Activity activity,
                           Runnable completionCommand) {
        mUrlEditText = new WeakReference<EditText>(editText);
        mImageView = new WeakReference<ImageView>(imageView);
        mActivity = new WeakReference<Activity>(activity);
        mCompletionCommand = completionCommand; // new WeakReference<Runnable>(completionCommand);
    }

    /**
     * Show a toast to the user.
     */
    public void showToast(String toastString) {
        Toast.makeText(mActivity.get(),
                       toastString,
                       Toast.LENGTH_SHORT).show();
    }

    /**
     * Display a downloaded bitmap image if it's non-null; otherwise,
     * it reports an error via a Toast that's displayed on the UI
     * Thread.  This method can be called from either the UI Thread or
     * a background Thread.
     * 
     * @param image
     *            The bitmap image
     */
    public void displayImage(final Bitmap image)
    {   
        // If this method is run in the UI Thread then display the
        // image.
        if (onUiThread()) {
            if (image == null)
                showToast("image is corrupted,"
                          + " please check the requested URL.");
            else {
                // Display the image on the user's screen.
                mImageView.get().setImageBitmap(image);

                // Indicate we're done with this image.  This call
                // runs in the UI Thread, so we don't need to
                // synchronize it.
                mCompletionCommand.run();
            }
        } 
        // Otherwise, create a new Runnable command that's posted to
        // the UI Thread to display the image.
        else {
            mActivity.get().runOnUiThread(new Runnable() {
                    public void run() {
                        // Display the downloaded image to the user.
                        displayImage(image);
                    }});
        }
    }

    /**
     * Download a bitmap image from the URL provided by the user.
     * 
     * @param url
     *            The url where a bitmap image is located
     *
     * @return the image bitmap or null if there was an error
     */
    public Bitmap downloadImage(String url) {
        //  Use the default URL if the user doesn't supply one.
        if (url.equals(""))
            url = mDefaultURL;

        try {
            // Connect to a remote server, download the contents of
            // the image, and provide access to it via an Input
            // Stream.
            InputStream is =
                (InputStream) new URL(url).getContent();

            // Decode an InputStream into a Bitmap.
            return BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            Log.e(TAG, "Error downloading image");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Read the URL EditText and return the String it contains.
     * 
     * @return String value in mUrlEditText
     */
    public String getUrlString() {
        return mUrlEditText.get().getText().toString();
    }

    /**
     * Hide the keyboard after a user has finished typing the url.
     */
    public void hideKeyboard() {
        InputMethodManager mgr =
            (InputMethodManager) mActivity.get().getSystemService
            (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mUrlEditText.get().getWindowToken(),
                                    0);
    }

    /**
     * Reset the image to what's designated in the imageResource.
     */
    public void resetImage(int imageResource) {
        // Display this image.
        mImageView.get().setImageResource(imageResource);

        // Indicate we're done with this image.
        mCompletionCommand.run();
    }

    /**
     * Returns true if the calling Thread is the UI Thread, else
     * false.
     */
    private Boolean onUiThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
