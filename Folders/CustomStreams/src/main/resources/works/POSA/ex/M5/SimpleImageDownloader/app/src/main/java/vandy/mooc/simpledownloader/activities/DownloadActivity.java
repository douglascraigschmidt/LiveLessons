package vandy.mooc.simpledownloader.activities;

import vandy.mooc.simpledownloader.R;
import vandy.mooc.simpledownloader.downloaders.DownloadWithAsyncTask;
import vandy.mooc.simpledownloader.downloaders.DownloadWithMessages;
import vandy.mooc.simpledownloader.downloaders.DownloadWithRunnables;
import vandy.mooc.simpledownloader.utils.UiUtils;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;

/**
 * This activity allows a user to download a bitmap image from a
 * remote server using the following concurrency strategies from the
 * Android HaMeR and AsyncTask frameworks:
 *
 * . Download with Runnables (HaMeR framework)
 * . Download with Messages (HaMeR framework)
 * . Download with AsyncTask (AsyncTask framework)
 *        
 * After the image is downloaded and converted into a Bitmap it is
 * displayed on the user's screen.
 * 
 * This implementation intentionally doesn't handle runtime
 * configuration changes robustly.  See the ImageDownloads example for
 * a framework that handles these changes via the Model-View-Presenter
 * (MVP) pattern.
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
     * Called when a user clicks a button to download/display an image
     * with using a runnable.
     * 
     * @param view
     *            The "Run Runnable" button
     */
    public void runRunnable(View view) {
        // Obtain the requested URL from the user input.
        final String url = getUrlString();

        UiUtils.hideKeyboard(this,
                             mUrlEditText.getWindowToken());

        // Inform the user that the download is starting.
        showDialog("downloading via Handlers and Runnables");
        
        // Create and start a new thread to download an image in the
        // background via a runnable.  The downloaded image is then
        // diplayed in the UI thread by posting another runnable via
        // the activity's runOnUiThread() method, which uses an
        // internal handler.
        new Thread(new DownloadWithRunnables(this,
                                             url)).start();
    }

    /**
     * Called when a user clicks a button to download an image with a
     * runnable and messages.
     * 
     * @param view
     *            The "Run Messages" button
     */
    public void runMessages(View view) {
        // Obtain the requested URL from the user input.
        final String url = getUrlString();

        UiUtils.hideKeyboard(this,
                             mUrlEditText.getWindowToken());

        // Create and start a new thread to download an image in the
        // background and then use messages and messagehandler to
        // cause it to be displayed in the UI thread.
        new Thread(new DownloadWithMessages(this,
                                            url)).start();
    }

    /**
     * Called when a user clicks a button to download an image via an
     * an asynctask.
     * 
     * @param view
     *            The "Run Async" button
     */
    public void runAsyncTask(View view) {
        // The the URL for the image to download.
        final String url = getUrlString();

        UiUtils.hideKeyboard(this,
                             mUrlEditText.getWindowToken());

        // Execute the download using an asynctask.
        new DownloadWithAsyncTask(this).execute(url);
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
                              // Use a Toast to inform user that
                              // something has gone wrong.
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
