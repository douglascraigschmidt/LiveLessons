package vandy.mooc.buggydownloader.activities;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

import vandy.mooc.buggydownloader.R;
import vandy.mooc.buggydownloader.utils.UiUtils;

/**
 * This activity allows a user to attempt to download a bitmap image
 * from a remote server using Java threads.  After the image is
 * downloaded and converted into a bitmap it is displayed on the
 * user's screen.
 * 
 * However, this implementation is intentionally buggy!  Moreover, it
 * intentionally doesn't handle runtime configuration changes
 * robustly.  See the ImageDownloads example for a framework that
 * handles these changes via the Model-View-Presenter (MVP) pattern.
 */
public class DownloadActivity
       extends LifecycleLoggingActivity {
    /**
     * Debug Tag for logging debug output to LogCat.
     */
    private final static String TAG =
        DownloadActivity.class.getSimpleName();

    /**
     * Default URL to download
     */
    private final static String mDefaultUrl =
        "http://www.dre.vanderbilt.edu/~schmidt/ka.png";

    /**
     * User's selection of URL to download
     */
    private EditText mUrlEditText;

    /**
     * Image that's been downloaded
     */
    private ImageView mImageView;

    /**
     * Display progress of download
     */
    private ProgressDialog mProgressDialog;

    /**
     * Method that initializes the activity when it is first created.
     * 
     * @param savedInstanceState
     *            Activity's previously frozen state, if there was one.
     */
    public void onCreate(Bundle savedInstanceState) {
        // Initialize the super class.
        super.onCreate(savedInstanceState);

        // Sets the content view specified in the
        // image_downloads_activity.xml file.
        setContentView(R.layout.download_activity);

        // Stores references to the EditText and ImageView objects in
        // data members to optimize subsequent access.
        mUrlEditText = (EditText) findViewById(R.id.mUrlEditText);
        mImageView = (ImageView) findViewById(R.id.mImageView);
    }

    /**
     * Called when a user clicks the "buggy1" button to
     * download/display an image.
     * 
     * @param view The "buggy1" button
     */
    public void buggy1(View view) {
        // Obtain the requested URL from the user input.
        final String url = getUrlString();
 
        UiUtils.hideKeyboard(this,
                             mUrlEditText.getWindowToken());

        // Inform the user that the download is starting.
        showDialog(" via buggy1() method");

        try {
            // Try to download the image.  This call will fail since a
            // network-related call is being made in the UI thread.
            Bitmap image = downloadBitmap(url);

            // Display the downloaded image to the user.
            displayBitmap(image);
        } catch (Exception e) {
            UiUtils.showToast(this,
                              "Exception "
                              + e
                              + " caught in buggy1()");
        } finally {
            // Dismiss the progress dialog.
            dismissDialog();
        }
    }

    /**
     * Called when a user clicks the "buggy2" button to download an
     * image.
     * 
     * @param view
     *            The "buggy2" button
     */
    public void buggy2(View view) {
        // Obtain the requested URL from the user input.
        String url = getUrlString();

        UiUtils.hideKeyboard(this,
                             mUrlEditText.getWindowToken());

        // Inform the user that the download is starting.
        showDialog(" via buggy2() method");

        // Create and start a new thread to download an image in the
        // background and then use messages and mMessageHandler to
        // cause it to be displayed in the UI thread.
        new Thread(() -> {
                try {
                    // Download the image in a background thread.
                    Bitmap image = downloadBitmap(url);

                    // Try to display the downloaded image to the user, which
                    // should fail since it's called in a background thread.
                    displayBitmap(image);
                } catch (final Exception e) {
                    runOnUiThread(() -> UiUtils.showToast(DownloadActivity.this,
                                                          "Exception "
                                                          + e
                                                          + " caught in buggy2()"));
                } finally {
                    // Dismiss the progress dialog.
                    dismissDialog();
                }
        }).start();
    }

    /**
     * Download a bitmap image from the URL provided by the user.
     * 
     * @param url
     *            The url where a bitmap image is located
     * @return the image bitmap or null if there was an error
     */
    public Bitmap downloadBitmap(String url) {
        // Use the default URL if the user doesn't supply one.
        String finalUrl = url.equals("") ? mDefaultUrl : url;

        try {
            Bitmap bitmap =
                UiUtils.downloadAndDecodeImage(finalUrl);

            if (bitmap == null)
                // Post error reports to the UI Thread.
                runOnUiThread(() -> 
                        // Use a Toast to inform user that something
                        // has gone wrong.
                        UiUtils.showToast(DownloadActivity.this,
                                          "Error downloading image,"
                                          + " please recheck URL "
                                          + finalUrl));
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**

    /**
     * Display a downloaded bitmap image if it's non-null; otherwise,
     * it reports an error via a Toast.
     * 
     * @param image
     *            The bitmap image
     */
    public void displayBitmap(Bitmap image) {
        if (mImageView == null)
            UiUtils.showToast(this,
                              "Problem with the app,"
                              + " please contact the developer.");
        else if (image != null)
            mImageView.setImageBitmap(image);
        else
            UiUtils.showToast(this,
                              "image is corrupted,"
                              + " please check the requested URL.");
    }

    /**
     * Called when a user clicks a button to reset an image to
     * default.
     * 
     * @param view
     *            The "Reset Image" button
     */
    public void resetImage(View view) {
        mImageView.setImageResource(R.mipmap.ic_icon);
    }

    /**
     * Read the URL edittext and return the string it contains.
     * 
     * @return String value in mUrlEditText
     */
    String getUrlString() {
        return mUrlEditText.getText().toString();
    }

    /**
     * Display the dialog to the user.
     * 
     * @param message 
     *          The string to display what download method was used.
     */
    public void showDialog(String message) {
        mProgressDialog =
            ProgressDialog.show(this,
                                "Download",
                                message);
    }
    
    /**
     * Dismiss the Dialog.
     */
    public void dismissDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
}
