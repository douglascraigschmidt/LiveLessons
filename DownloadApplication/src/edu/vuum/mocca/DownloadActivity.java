package edu.vuum.mocca;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * @class DownloadActivity
 * 
 * @brief A class that allows a user to download a bitmap image using
 *        a DownloadService.
 */
public class DownloadActivity extends Activity {
    /**
     * User's selection of URL to download
     */
    private EditText mUrlEditText;

    /**
     * Image that's been downloaded
     */
    private ImageView mImageView;

    /**
     * Default URL.
     */
    private String mDefaultUrl = 
        "http://www.dre.vanderbilt.edu/~schmidt/ka.png";

    /**
     * Display progress of download
     */
    private ProgressDialog mProgressDialog;

    /**
     * Stores an instance of DownloadHandler.
     */
    Handler mDownloadHandler = null;

    /**
     * Method that initializes the Activity when it is first created.
     * 
     * @param savedInstanceState
     *            Activity's previously frozen state, if there was one.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the content view specified in the main.xml file.
        setContentView(R.layout.main);

        // Caches references to the EditText and ImageView objects in
        // data members to optimize subsequent access.
        mUrlEditText = (EditText) findViewById(R.id.mUrlEditText);
        mImageView = (ImageView) findViewById(R.id.mImageView);

        // Initialize the downloadHandler.
        mDownloadHandler = new DownloadHandler(this);
    }

    /**
     * Show a toast, notifying a user of an error when retrieving a
     * bitmap.
     */
    void showErrorToast(String errorString) {
        Toast.makeText(this,
                       errorString,
                       Toast.LENGTH_LONG).show();
    }

    /**
     * Display a downloaded bitmap image if it's non-null; otherwise,
     * it reports an error via a Toast.
     * 
     * @param image
     *            The bitmap image
     */
    void displayImage(Bitmap image)
    {   
        if (mImageView == null)
            showErrorToast("Problem with Application,"
                           + " please contact the Developer.");
        else if (image != null)
            mImageView.setImageBitmap(image);
        else
            showErrorToast("image is corrupted,"
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
        mImageView.setImageResource(R.drawable.default_image);
    }

    /**
     * Called when a user clicks the Download Image button to download
     * an image using the DownloadService
     * 
     * @param view
     *            The "Download Image" button
     */
    public void downloadImage(View view) {
        // Obtain the requested URL from the user input.
        String url = getUrlString();

        Log.e(DownloadActivity.class.getSimpleName(),
              "Downloading " + url);

        hideKeyboard();

        // Inform the user that the download is starting.
        showDialog("downloading via startService()");
        
        // Create an Intent to download an image in the background via
        // a Service.  The downloaded image is later diplayed in the
        // UI Thread via the downloadHandler() method defined below.
        Intent intent =
            DownloadService.makeIntent(this,
                                       Uri.parse(url),
                                       mDownloadHandler);

        // Start the DownloadService.
        startService(intent);
    }

    /**
     * @class DownloadHandler
     *
     * @brief An inner class that inherits from Handler and uses its
     *        handleMessage() hook method to process Messages sent to
     *        it from the DownloadService.
     */
    private static class DownloadHandler extends Handler {
        /**
         * Allows Activity to be garbage collected properly.
         */
        private WeakReference<DownloadActivity> mActivity;

        /**
         * Class constructor constructs mActivity as weak reference
         * to the activity
         * 
         * @param activity
         *            The corresponding activity
         */
        public DownloadHandler(DownloadActivity activity) {
            mActivity = new WeakReference<DownloadActivity>(activity);
        }

        /**
         * This hook method is dispatched in response to receiving the
         * pathname back from the DownloadService.
         */
        public void handleMessage(Message message) {
            DownloadActivity activity = mActivity.get();
            // Bail out if the DownloadActivity is gone.
            if (activity == null)
                return;

            // Try to extract the pathname from the message.
            String pathname = DownloadService.getPathname(message);
                
            // See if the download worked or not.
            if (pathname == null)
                activity.showDialog("failed download");

            // Stop displaying the progress dialog.
            activity.dismissDialog();

            // Display the image in the UI Thread.
            activity.displayImage(BitmapFactory.decodeFile(pathname));
        }
    };

    /**
     * Display the Dialog to the User.
     * 
     * @param message 
     *          The String to display what download method was used.
     */
    public void showDialog(String message) {
        mProgressDialog =
            ProgressDialog.show(this,
                                "Download",
                                message,
                                true);
    }
    
    /**
     * Dismiss the Dialog
     */
    public void dismissDialog() {
        if (mProgressDialog != null)
            mProgressDialog.dismiss();
    }
    
    /**
     * Hide the keyboard after a user has finished typing the url.
     */
    private void hideKeyboard() {
        InputMethodManager mgr =
            (InputMethodManager) getSystemService
            (Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(mUrlEditText.getWindowToken(),
                                    0);
    }

    /**
     * Show a collection of menu items for the ThreadedDownloads
     * activity.
     * 
     * @return true
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }
    
    /**
     * Read the URL EditText and return the String it contains.
     * 
     * @return String value in mUrlEditText
     */
    String getUrlString() {
        String s = mUrlEditText.getText().toString();
        if (s.equals(""))
            s = mDefaultUrl;
        return s;
    }

}
