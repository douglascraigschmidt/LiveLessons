package edu.vuum.mocca;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

/**
 * @class ThreadedDownloadsActivity
 * 
 * @brief This Activity allows a user to download a bitmap image from
 *        a remote server using the following concurrency strategies
 *        from the HaMeR and AsyncTask frameworks:
 *
 *        . Handlers and Runnable (HaMeR framework)
 *        . Handlers and Messages (HaMeR framework)
 *        . AsyncTask (AsyncTask framework)
 *        
 *        After the image is downloaded and store it is displayed on
 *        the user's screen.
 */
public class ThreadedDownloadsActivity extends Activity {
    /**
     * Debug Tag for logging debug output to LogCat.
     */
    protected final static String TAG =
        ThreadedDownloadsActivity.class.getSimpleName();

    /**
     * Maps buttons (represented via their resource ids) to
     * ButtonStrategy implementations.
     */
    private ButtonStrategyMapper mButtonStrategyMapper;

    /**
     * The currently active ButtonStrategy, which is used to keep
     * track of whether we need to cancel an ongoing download before
     * initiating a new one.
     */
    protected ButtonStrategy mActiveButtonStrategy = null;

    /**
     * Lifecycle hook method that initializes the Activity when it is
     * first created.
     * 
     * @param savedInstanceState
     *            Activity's previously frozen state, if there was one.
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Sets the content view specified in the main.xml file.
        setContentView(R.layout.main);

        // Initialize the ButtonStrategyMapper that efficiently maps
        // button ids to strategies for downloading and displaying
        // images concurrently.
        mButtonStrategyMapper = 
            new ButtonStrategyMapper
               (new int[] { 
                    R.id.runnable_button,
                    R.id.messages_button,
                    R.id.async_task_button,
                    R.id.reset_image_button },
                new ButtonStrategy[] {
                    new DownloadWithRunnable(),
                    new DownloadWithMessages(),
                    new DownloadWithAsyncTask(),
                    new ResetImage()
               });
    }
    
    /**
     * Factory method that returns the DownloadContext associated with
     * this user request, which plays the role of the "Context" in the
     * Strategy pattern.
     */
    private DownloadContext makeDownloadContext() {
        // This command is called back after the image is displayed to
        // indicate there's no active ButtonStrategy.
        Runnable completionCommand = 
            new Runnable() {
                public void run() {
                    // Indicate there's no active ButtonStrategy.
                    mActiveButtonStrategy = null;
                }
            };

        // Create a DownloadContext that stores references to the
        // EditText, ImageView, "this", and completion hook objects in
        // data members, which are used by the various concrete
        // ButtonStrategies to download and display an image
        // concurrently.
        return new DownloadContext((EditText) findViewById(R.id.mUrlEditText),
                                   (ImageView) findViewById(R.id.mImageView),
                                   this,
                                   completionCommand);
    }

    /**
     * Called when a user clicks a button to download an image.
     * 
     * @param view
     *            Indicates the button pressed by the user.
     */
    public void handleButtonClick(View view) {
        // Create a DownloadContext object associated with this user
        // request, which plays the role of the "Context" in the
        // Strategy pattern.
        DownloadContext downloadContext = makeDownloadContext();
        
        // Hide the keyboard.
        downloadContext.hideKeyboard();

        // Only one download is allowed at a time, so if there's a
        // download in progress then cancel it first.
        if (mActiveButtonStrategy != null) {
            mActiveButtonStrategy.cancelDownload(downloadContext);
        }

        // Get the ButtonStrategy associated with the button's
        // resource id.
        mActiveButtonStrategy = 
            mButtonStrategyMapper.getButtonStrategy(view.getId());

        // Invoke the ButtonStrategy to download and display the
        // image concurrently.
        mActiveButtonStrategy.downloadAndDisplayImage(downloadContext);
    }

    /**
     * Show a collection of menu items for the
     * ThreadedDownloadsActivity.
     * 
     * @return true
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options,
                                  menu);
        return true;
    }
}

