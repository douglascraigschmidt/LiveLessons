package example.imagetaskgang;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * @class MainActivity
 *
 * @brief Main Activity for the Android version of the ImageTaskGang
 *        application.
 */
public class MainActivity extends Activity {
    /**
     * A LinearLayout where each element is an AutoCompleteTextview
     * that holds a comma-separated list of URLs to download.
     */
    protected LinearLayout mListUrlGroups;
    
    /**
     * The button to run the gang using the user
     * input
     */
    private Button mRunButton;
	
    /**
     * Suggestions of default URLs that are supposed to be presented
     * to the user via AutoCompleteTextView.
     */
    private final String[] SUGGESTIONS = new String[] {        
        "http://www.dre.vanderbilt.edu/~schmidt/ka.png,"
        + "http://www.dre.vanderbilt.edu/~schmidt/uci.png,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/douglass.jpg",
        "http://www.cs.wustl.edu/~schmidt/gifs/lil-doug.jpg,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/wm.jpg,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/ironbound.jpg"
    };
    
    /**
     * The adapter responsible for recommending suggestions of URLs to
     * download images from.
     */
    private ArrayAdapter<String> mSuggestions;
	
    /**
     * Array of Filters to apply to the downloaded images.
     */
    private final Filter[] FILTERS = {
        new NullFilter(),
        new GrayScaleFilter()
    };
	
    /**
     * The name of the extra attached to the intent that starts
     * ResultActivity, which allows the ResultActivity to divide the
     * output into groups for viewing the results more clearly.
     */
    static final String FILTER_EXTRA = "filter_extra";

    /**
     * Define a completion hook that's called back to display the
     * results after the ImageTaskGang finishes downloading,
     * processing, and storing Images provided by the List of URLs.
     */
    final Runnable displayImagesRunnable = 
        new Runnable() {
            @Override
            public void run() {
                // Run the displayImages() method on the UI Thread so
                // that startActivity() occurs in that context.
                MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                            public void run() {
                            setButtonsEnabled(true);
                            displayImages();
                        }
                    });
            }
        };

    /**
     * Hook method called when the Activity is first launched to
     * initialize the content view and various data members.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the main view.
        setContentView(R.layout.activity_main);
		
        // Cache a LinearLayout where each element is an
        // AutoCompleteTextview that holds a comma-separated list of
        // URLs to download.
        mListUrlGroups =
            (LinearLayout) findViewById(R.id.listOfURLLists);
        
        // Cache a reference to the button to run the gang
        // on user input, so that it can become visible
        // when a valid input is given
        mRunButton = (Button) findViewById(R.id.runGang);

        // Initialize the Platform singleton with the appropriate
        // Platform strategy, which in this case will be the
        // AndroidPlatform.
        PlatformStrategy.instance
            (new PlatformStrategyFactory(this)
             .makePlatformStrategy());

        // Initialize the Options singleton.
        Options.instance().parseArgs(null);
    }
    
    /**
     * Hook method that is called first when the activity context has
     * been created.  Set the mSuggestions in onStart() so we are sure
     * the context exists and will not throw a NullPointerException.
     */
    @Override
    protected void onStart() {
    	super.onStart();
        mSuggestions = 
            new ArrayAdapter<String>(this,
                                     R.layout.suggestion_item,
                                     SUGGESTIONS);
    }
	
    /**
     * Adds a List of URLs to the ListView to allow for variable
     * number of URL Lists to process (i.e., variable number of
     * iteration cycles by the ImageTaskGang).
     */
    @SuppressLint("InflateParams")
    public void addURLs(View view) {
    	
    	// Create the new list from R.layout.list_item
        AutoCompleteTextView newList = 
            (AutoCompleteTextView) 
            LayoutInflater.from(this).inflate (R.layout.list_item,
                                               null);
        
        // Set the adapter to the given suggestions
        newList.setAdapter(mSuggestions);
        
        // Add the view and invalidate it so that the
        // layout is redrawn by the framework
        mListUrlGroups.addView(newList);
        mListUrlGroups.invalidate();
        
        // Set the "Run" button to visible
        mRunButton.setVisibility(View.VISIBLE);
    }
	
    /**
     * Run the gang using a default set of URL lists hardcoded into
     * the application rather than reading the input lists.
     */
    public void useDefaultURLs(View view) {
        new Thread(new ImageTaskGang(FILTERS,
                                     PlatformStrategy.instance().getUrlIterator
                                     (PlatformStrategy.InputSource.DEFAULT),
                                     displayImagesRunnable)).start();
        setButtonsEnabled(false);
    }
	
    /**
     * Run the gang by reading the input lists of URLs.
     */
    public void runImageTaskGang(View view) {
    	if (mRunButton.getVisibility() == View.VISIBLE) {
	    	Iterator<List<URL>> iterator =
	            PlatformStrategy.instance().getUrlIterator
	            (PlatformStrategy.InputSource.USER);
	    	
	    	// Check to see if the user entered any lists.
	    	if (iterator != null) {
	            if (iterator.hasNext() && !isEmpty()) {
	                new Thread(new ImageTaskGang(FILTERS,
	                                             iterator,
	                                             displayImagesRunnable)).start();
	                setButtonsEnabled(false);
	            } else 
	                showToast("No list of URLs entered");
	    	}
    	}
    }
    
    /**
     * Checks to see if all the lists are empty.
     */
    private boolean isEmpty() {
    	int listCount = mListUrlGroups.getChildCount();
    	for (int i = 0; i < listCount; ++i) {
    		// Obtain a reference to the child list and check if
    		// text has been entered
            AutoCompleteTextView list = 
                (AutoCompleteTextView) mListUrlGroups.getChildAt(i);
            if (list.getText().length() > 0) 
                return false;
    	}
    	return true;
    }

    /**
     * Sets the list of buttons to disabled, which shows that the
     * application is processing visually, and is a (slightly
     * forceful) way to keep multiple task gangs from being started
     * accidentally.
     */
    private void setButtonsEnabled(boolean enabled) {
    	LinearLayout buttonLayout = 
            (LinearLayout) findViewById(R.id.buttonLayout);
    	int buttonCount = buttonLayout.getChildCount();
    	for (int i = 0; i < buttonCount; ++i) {
            buttonLayout.getChildAt(i).setEnabled(enabled);
    	}
    }
	
    /**
     * Delete the previously downloaded pictures and directories.
     */
    public void clearFilterDirectories(View view) {
    	setButtonsEnabled(false);
        for (Filter filter : FILTERS) {
            deleteSubFolders
                (new File(PlatformStrategy.instance().getDirectoryPath(), 
                          filter.getName()).getAbsolutePath());
        }
        setButtonsEnabled(true);
        showToast("Previously downloaded files deleted");
    }
	
    /**
     * A helper method that recursively deletes files in a specified
     * directory. 
     */
    private void deleteSubFolders(String path) {
        File currentFolder = new File(path);        
        File files[] = currentFolder.listFiles();

        if (files == null) 
            return;

        // Android does not allow you to delete a directory with child
        // files, so we need to write code that handles this
        // recursively.
        for (File f : files) {          
            if (f.isDirectory()) 
                deleteSubFolders(f.toString());
            f.delete();
        }
        currentFolder.delete();
    }
	
    /**
     * Creates an Intent that's used to start the ResultsActivity,
     * which can be used to view the results.
     */
    private void displayImages() {
        // Pass a list of filterNames to the ResultsActivity so it
    	// knows what buttons to generate to allow the user to view
    	// all the downloaded results.
        String[] filterNames =
            new String[FILTERS.length];

        for (int i = 0; i < filterNames.length; ++i) 
            filterNames[i] = FILTERS[i].getName();
        
        // Create the intent and add the list of filterNames as an
        // extra.
        Intent resultsIntent =
            new Intent(this,
                       ResultsActivity.class);
        resultsIntent.putExtra(FILTER_EXTRA, 
                               filterNames);
        // Start the ResultsActivity.
        startActivity(resultsIntent);
    }
    
    /**
     * Show a toast to the user.
     */
    public void showToast(String msg) {
        Toast.makeText(this,
                       msg,
                       Toast.LENGTH_SHORT).show();
    }
}
