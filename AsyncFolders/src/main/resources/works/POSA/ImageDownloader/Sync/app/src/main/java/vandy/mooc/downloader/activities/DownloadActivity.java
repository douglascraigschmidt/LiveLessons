package vandy.mooc.downloader.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.widget.EditText;

import java.io.File;

import vandy.mooc.downloader.R;
import vandy.mooc.downloader.utils.DownloadUtils;
import vandy.mooc.downloader.utils.UiUtils;
import vandy.mooc.downloader.utils.UriUtils;

/**
 * This activity prompts the user for a URL to an image and then uses
 * the HaMeR framework to download the image and view it.
 */
public class DownloadActivity
       extends ActivityBase {
    /**
     * URL for the image that's downloaded by default if the user
     * doesn't specify otherwise.
     */
    private final static String DEFAULT_URL =
        "http://www.dre.vanderbilt.edu/~schmidt/robot.png";

    /**
     * EditText field for entering the desired URL to an image.
     */
    private EditText mUrlEditText;

    /**
     * Keeps track of whether a download button click from the user is
     * processed or not.  Only one download click is processed until a
     * requested image is downloaded and displayed.
     */
    private boolean mProcessButtonClick = true;

    /**
     * Reference to the "add" floating action button.
     */
    private FloatingActionButton mAddFab;

    /**
     * Reference to the "download" floating action button.
     */
    private FloatingActionButton mDownloadFab;

    /**
     * Keeps track of whether the edit text is visible for the user to
     * enter a URL.
     */
    private boolean mIsEditTextVisible = false;

    /**
     * Display progress of download
     */
    private ProgressDialog mProgressDialog;

    /**
     * Hook method called when a new instance of Activity is
     * created. One time initialization code goes here, e.g., UI
     * snackbar and some class scope variable initialization.
     *
     * @param savedInstanceState
     *            object that contains saved state information.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Always call super class for necessary
        // initialization/implementation.
        super.onCreate(savedInstanceState);

        // Set the default snackbar.
        setContentView(R.layout.activity_main);

        // Initialize the views.
        initializeViews();
    }

    /**
     * Initialize the views.
     */
    private void initializeViews() {
        // Cache the EditText that holds the urls entered by the
        // user (if any).
        mUrlEditText = (EditText) findViewById(R.id.url);

        // Cache floating action button that adds a URL.
        mAddFab =
            (FloatingActionButton) findViewById(R.id.add_fab);

        // Cache floating action button that downloads an image.
        mDownloadFab =
            (FloatingActionButton) findViewById(R.id.download_fab);

        // Make the EditText invisible for animation purposes
        mUrlEditText.setVisibility(View.INVISIBLE);

        // Make the download button invisible for animation purposes
        mDownloadFab.setVisibility(View.INVISIBLE);

        // Register a listener to help display download FAB when the user
        // hits enter.
        mUrlEditText.setOnEditorActionListener
            ((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    UiUtils.hideKeyboard(DownloadActivity.this,
                                         mUrlEditText.getWindowToken());
                    // Insert default value if no input was specified.
                    if (TextUtils.isEmpty(
                                          mUrlEditText.getText().toString().trim())) {
                        mUrlEditText.setText(
                                             String.valueOf(DEFAULT_URL));
                    }
                    UiUtils.showFab(mDownloadFab);
                    return true;
                } else
                    return false;
            });
    }

    /**
     * Called by the Android Activity framework when the user clicks +
     * floating action button.
     * @param view The view
     */
    public void addUrl(View view) {
        // Check whether the EditText is visible to determine
        // the kind of animations to use.
        if (mIsEditTextVisible) {
            // Hide the EditText using circular reveal animation
            // and set boolean to false.
            UiUtils.hideEditText(mUrlEditText);
            mIsEditTextVisible = false;

            // Rotate the FAB from 'X' to '+'.
            int animRedId = R.anim.fab_rotate_backward;

            // Load and start the animation.
            mAddFab.startAnimation
                (AnimationUtils.loadAnimation(this,
                                              animRedId));
            // Hides the download FAB.
            UiUtils.hideFab(mDownloadFab);
        } else {
            // Reveal the EditText using circular reveal animation and
            // set boolean to true.
            UiUtils.revealEditText(mUrlEditText);
            mIsEditTextVisible = true;
            mUrlEditText.requestFocus();

            // Rotate the FAB from '+' to 'X'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                                                                animRedId));
        }
    }

    /**
     * Called by the Android Activity framework when the user clicks
     * the "Download Image" button.  It uses the HaMeR framework to
     * download and display an image.
     *
     * @param view
     *            The view.
     */
    public void downloadImage(View view) {
        try {
            // Hide the keyboard.
            UiUtils.hideKeyboard(this,
                                 mUrlEditText.getWindowToken());

            // Get the URL to download.
            Uri url = getUrl();

            // Make sure that there's not already a download in progress.
            if (!mProcessButtonClick)
                UiUtils.showToast(this,
                                  "Already downloading image "
                                  + url);
            // Do a sanity check to ensure the URL is valid.
            else if (!URLUtil.isValidUrl(url.toString()))
                UiUtils.showToast(this,
                                  "Invalid URL "
                                  + url.toString());
            else {
                // Disable processing of a button click.
                mProcessButtonClick = false;

                // Inform the user that the download is starting.
                showDialog("downloading via HaMeR");

                // Start downloading the image from the URL given
                // by the user.
                startDownload(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Use a Java thread and the HaMeR framework to download an image
     * and display the result.
     */
    private void startDownload(Uri url) {
        // Create a runnable that downloads an image in a background
        // thread.
        Runnable downloadRunnable =
            new Runnable() {
                public void run() {
                    // Download the image synchronously.
                    final Uri imagePathname =
                        DownloadUtils.downloadImage(DownloadActivity.this,
                                                    url);

                    // Create a command that will display the image in
                    // the UI thread.
                    runOnUiThread(new Runnable() {
                            public void run() {
                                // Display the image.
                                displayImage(imagePathname);
                            }});}
            };

        /*
         * Here's the Java 8 version of this code using lambda expressions:
        Runnable downloadRunnable =
                () -> {
                    // Download the image synchronously.
                    final Uri imagePathname =
                            DownloadUtils.downloadImage(DownloadActivity.this,
                                    url);

                    // Create a command that will display the image in
                    // the UI thread.
                    runOnUiThread(() ->
                        // Display the image.
                        displayImage(imagePathname));};
        */

        // Start the thread to download the image.
        new Thread(downloadRunnable).start(); 
    }

    /**
     * Use an activity from the Gallery app to display the image.
     */
    private void displayImage(Uri imagePathname) {
        // Stop displaying the progress dialog.
        dismissDialog();

        // Call the makeGalleryIntent() factory method to create
        // an Intent that will launch the "Gallery" app by passing
        // in the path to the downloaded image file.
        Intent intent =
            makeGalleryIntent(imagePathname.toString());

        // Start the default Android Gallery app image viewer.
        startActivity(intent);

        // Allow user to click the download button again.
        mProcessButtonClick = true;
    }

    /**
     * Get the URL to download based on user input or the default.
     */
    private Uri getUrl() {
        // Get the text the user typed in the edit text (if anything).
        String userInput = mUrlEditText.getText().toString();

        // If the user didn't provide a URL then use the default.
        if ("".equals(userInput))
            userInput = DEFAULT_URL;

        return Uri.parse(userInput);
    }

    /**
     * Factory method that returns an implicit Intent for viewing the downloaded
     * image in the Gallery app.
     */
    public Intent makeGalleryIntent(String pathToImageFile) {
        // Create an intent that will start the Gallery app to view
        // the image.
        return UriUtils
            .buildFileProviderReadUriIntent(this,
                                            Uri.fromFile(new File(pathToImageFile)),
                                            Intent.ACTION_VIEW,
                                            "image/*");
    }

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
}
