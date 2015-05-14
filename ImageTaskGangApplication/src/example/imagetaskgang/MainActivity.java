package example.imagetaskgang;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import example.imagetaskgang.filters.Filter;
import example.imagetaskgang.filters.GrayScaleFilter;
import example.imagetaskgang.filters.NullFilter;
import example.imagetaskgang.servermodel.FilterData;
import example.imagetaskgang.servermodel.ImageData;
import example.imagetaskgang.servermodel.ImageStreamService;
import example.imagetaskgang.servermodel.ServerResponse;

import retrofit.RestAdapter;
import retrofit.client.Request;
import retrofit.client.UrlConnectionClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
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
     * The button to run the ImageTaskGang locally using the user input.
     */
    private Button mRunLocalButton;
    
    /**
     * The button to run the ImageTaskGang on the server using the user input.
     */
    private Button mRunServerButton;
    
    /**
     * The button to clear the lists of user input.
     */
    private Button mClearListsButton;
	
    /**
     * Array of Filters to apply to the downloaded images.
     */
    private final Filter[] mFilters = {
        new NullFilter(),
        new GrayScaleFilter()
    };
    
    /**
     * An enumeration representing where the image processing
     * will take place, locally to the phone or on the server
     */
    public static enum ProcessingContext {
    	LOCAL, SERVER
    }
	
    /**
     * The retrofit service that handles requests
     */
    private ImageStreamService mImageService;
	
    /**
     * Define a completion hook that's called back to display the
     * results after the ImageTaskGang finishes processing and storing
     * Images downloaded from the Lists of URLs.
     */
    final Runnable mCompletionHook = 
        new Runnable() {
            @Override
                public void run() {
                // Uses the Android HaMeR concurrency framework to
                // invoke the displayImages() method in the UI Thread
                // so that the ResultsActivity is launched in that
                // context.
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goToResultActivity();
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

        // Set the main content view.
        setContentView(R.layout.activity_main);
		
        // Cache a LinearLayout where each element is an
        // AutoCompleteTextview that holds a comma-separated list of
        // URLs to download.
        mListUrlGroups =
            (LinearLayout) findViewById(R.id.listOfURLLists);
        
        // Cache references to the buttons which become visible
        // when a valid USER input is given.
        mRunLocalButton = (Button) findViewById(R.id.runWithUserURLsLocal);
        
        mRunServerButton = (Button) findViewById(R.id.runWithUserURLsServer);
        
        mClearListsButton = (Button) findViewById(R.id.clearLists);

        // Initialize the Platform singleton with the appropriate
        // Platform strategy, which in this case will be the
        // AndroidPlatform.
        PlatformStrategy.instance
            (new PlatformStrategyFactory(this)
             .makePlatformStrategy());

        // Initialize the Options singleton.
        Options.instance().parseArgs(null);
        
        // Configure the restAdapter to create the ImageStreamService
        RestAdapter restAdapter =
        		new RestAdapter.Builder()
					.setClient(new ExtendedTimeoutUrlConnectionClient())
					.setEndpoint("10.0.0.2/ImageStreamWeb/")
					.build();
        
        mImageService = restAdapter.create(ImageStreamService.class);
    }
    
    /**
     * Run the gang locally using a default set of URL lists
     */
    public void runWithDefaultURLsLocal(View view) {
    	runURLs(view, 
    		    PlatformStrategy.InputSource.DEFAULT,
    			ProcessingContext.LOCAL);
    }
    
    /**
     * Run the gang on the server using a default set of URL lists
     */
    public void runWithDefaultURLsServer(View view) {
    	runURLs(view, 
    		    PlatformStrategy.InputSource.DEFAULT,
    			ProcessingContext.SERVER);
    }
    
    /**
     * Run the gang locally by reading the input lists of URLs.
     */
    public void runWithUserURLsLocal(View view) {
    	runURLs(view, 
    		    PlatformStrategy.InputSource.USER,
    			ProcessingContext.LOCAL);
    }
    
    /**
     * Run the gang on the server by reading the input lists of URLs.
     */
    public void runWithUserURLsServer(View view) {
    	runURLs(view, 
    		    PlatformStrategy.InputSource.USER,
    			ProcessingContext.SERVER);
    }
    
    /**
     * Helper method to run the appropriate configuration of
     * inputs and contexts
     */
    private void runURLs(View view, 
    		PlatformStrategy.InputSource inputSource,
    		ProcessingContext processingContext) {
    	
    	// Ensure the desired button was pressed (it must be visible)
    	if (view.getVisibility() != View.VISIBLE) {
    		return; // no - op
    	}
    	
        Iterator<List<URL>> iterator =
            PlatformStrategy.instance().getUrlIterator(inputSource);
    	
        // Check to see if the user entered any lists.
        if (iterator != null) {
        	if(iterator.hasNext() 
        	   && (inputSource == PlatformStrategy.InputSource.USER ?
        			   !isEmpty() : true)) {
                    switch(processingContext) {
                    default:
                    case LOCAL:
                        new Thread(new ImageTaskGang(mFilters,
                                                     iterator,
                                                     mCompletionHook)).start();
                        break;
                    case SERVER:
                        new InvokeServerTask().execute(inputSource);
                        break;
                    }
                    setButtonsEnabled(false);
	        }
	        else {
	            showToast("No list of URLs entered");
	        }
        }
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
            new String[mFilters.length];

        for (int i = 0; i < filterNames.length; ++i) 
            filterNames[i] = mFilters[i].getName();
        
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
     * Helper method to properly transition to the
     * results activity
     */
    private void goToResultActivity() {
    	setButtonsEnabled(true);
        displayImages();
    }

    /**
     * The name of the extra attached to the intent that starts
     * ResultActivity, which allows the ResultActivity to divide the
     * output into groups for viewing the results more clearly.
     */
    static final String FILTER_EXTRA = "filter_extra";
    
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
        
        // Set the appropriate buttons to visible
        mRunLocalButton.setVisibility(View.VISIBLE);
        mRunServerButton.setVisibility(View.VISIBLE);
        mClearListsButton.setVisibility(View.VISIBLE);
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
            (LinearLayout) findViewById(R.id.defaultButtonLayout);
    	int buttonCount = buttonLayout.getChildCount();
    	for (int i = 0; i < buttonCount; ++i) {
            buttonLayout.getChildAt(i).setEnabled(enabled);
    	}
    	
    	buttonLayout = 
            (LinearLayout) findViewById(R.id.userButtonLayout);
    	buttonCount = buttonLayout.getChildCount();
    	for (int i = 0; i < buttonCount; ++i) {
            buttonLayout.getChildAt(i).setEnabled(enabled);
    	}
    	
    	buttonLayout =
            (LinearLayout) findViewById(R.id.buttonLayoutBottom);
    	buttonCount = buttonLayout.getChildCount();
    	for (int i = 0; i < buttonCount; ++i) {
            buttonLayout.getChildAt(i).setEnabled(enabled);
    	}
    }
    
    public void clearLists(View view) {
    	mListUrlGroups.removeAllViews();
    	mListUrlGroups.invalidate();
    	
    	// Set the appropriate buttons to visible
        mRunLocalButton.setVisibility(View.INVISIBLE);
        mRunServerButton.setVisibility(View.INVISIBLE);
        mClearListsButton.setVisibility(View.INVISIBLE);
    }
	
    /**
     * Delete the previously downloaded pictures and directories.
     */
    public void clearFilterDirectories(View view) {
    	setButtonsEnabled(false);
        for (Filter filter : mFilters) {
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
     * Show a toast to the user.
     */
    public void showToast(String msg) {
        Toast.makeText(this,
                       msg,
                       Toast.LENGTH_SHORT).show();
    }
    
    
    /**
     * An AsyncTask that runs the ImageStream processing
     * on the server using retrofit and writes the results to files.
     */
    private class InvokeServerTask 
    		extends AsyncTask<PlatformStrategy.InputSource, Void, Boolean> {
  
    	@Override
        protected Boolean doInBackground(
        		PlatformStrategy.InputSource... inputSources) {
        	// Make the request, invoking the server to run the ImageStream
        	// processing. 'response' will hold the POJOs that represent
        	// the results of the processing
            ServerResponse response = mImageService.execute(
            		PlatformStrategy.instance().getUrlLists(inputSources[0]));
            
            // Iterate over the results, writing them to appropriate files.
            // This is analogous to the OutputFilterDecorator functionality.
            boolean success = true;
            for(FilterData filterData : response.filterList) {
            	for(ImageData imageData : filterData.imageData) {
            		if(!PlatformStrategy.instance()
	    					.storeExternalImage(
	    							filterData.filterName,
								 	imageData.imageName,
								 	PlatformStrategy.instance()
								 		.makeImage(Base64.decode(
								 				   	imageData.image,
	    											Base64.DEFAULT)))) {
            			success = false;
            		}
            	}
            }
            
            return success;
        }
		
    	// If the request and file writing is successful, display
    	// the results.
        @Override
        protected void onPostExecute(Boolean success) {
        	if(success)
        		mCompletionHook.run();
        	else
        		showToast("Server failed!");
        }
    }
    
    /**
     *  Tailors the URLConnectionClient to remain open while the server
     *  completes the ImageStreamProcessing
     */
    private class ExtendedTimeoutUrlConnectionClient 
    									extends UrlConnectionClient {
    	
    	private final int WAIT_TIME = 30 * 1000;
    	
    	@Override 
    	protected HttpURLConnection openConnection(Request request) 
    			throws IOException {
    		HttpURLConnection connection = super.openConnection(request);
		    connection.setConnectTimeout(WAIT_TIME);
		    connection.setReadTimeout(WAIT_TIME);
		    return connection;
    	}
    }
    
}
