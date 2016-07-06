package livelessons.imagestreamgang.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import livelessons.imagestreamgang.R;
import livelessons.imagestreamgang.filters.Filter;
import livelessons.imagestreamgang.filters.GrayScaleFilter;
import livelessons.imagestreamgang.filters.NullFilter;
import livelessons.imagestreamgang.streams.ImageStream;
import livelessons.imagestreamgang.streams.ImageStreamCompletableFuture1;
import livelessons.imagestreamgang.streams.ImageStreamCompletableFuture2;
import livelessons.imagestreamgang.streams.ImageStreamParallel;
import livelessons.imagestreamgang.streams.ImageStreamSequential;
import livelessons.imagestreamgang.utils.Options;
import livelessons.imagestreamgang.utils.UiUtils;

/**
 * Main Activity for the Android ImageStreamGang app.
 */
public class MainActivity 
       extends MainActivityBase {
    /**
     * A LinearLayout where each element is an AutoCompleteTextview
     * that holds a comma-separated list of URLs to download.
     */
    protected LinearLayout mListUrlGroups;
    
    /**
     * The button to run the ImageStreamGang using the user input.
     */
    private Button mRunButton;

    /**
     * The button to clear the lists of user input.
     */
    private Button mClearListsButton;

    /**
     * The button to load default images.
     */
    private Button mDefaultButton;

    /**
     * Long-press of mDefaultButton will toggle this boolean value which
     * is used to determine if local or remote default images are loaded.
     */
    private boolean mLocalDefaultMode;

    /**
     * User selection for the desired stream.  Defaults to the
     * ImageStreamSequential.
     */
    private int mStreamId = R.id.completablefuture2;

    /**
     * Array of Filters to apply to the downloaded images.
     */
    private final Filter[] mFilters = {
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
     * Suggestions of default URLs that are supposed to be presented
     * to the user via AutoCompleteTextView.
     */
    private final String[] sSUGGESTIONS = new String[] {        
        "http://www.dre.vanderbilt.edu/~schmidt/ka.png,"
        + "http://www.dre.vanderbilt.edu/~schmidt/uci.png,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/douglass.jpg", 
        "http://www.cs.wustl.edu/~schmidt/gifs/lil_dougjpg,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/wm.jpg,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/ironbound.jpg"
    };
    
    /**
     * The adapter responsible for recommending suggestions of URLs to
     * download images from.
     */
    private ArrayAdapter<String> mSuggestions;
	
    /**
     * Define a completion hook that's called back to display the
     * results after the ImageStreamGang finishes processing and
     * storing Images downloaded from the Lists of URLs.
     */
    private final Runnable mCompletionHook = () ->
        // Uses the Android HaMeR concurrency framework to invoke the
        // displayImages() method in the UI Thread so that the
        // ResultsActivity is launched in that context.
        MainActivity.this.runOnUiThread(() -> goToResultActivity());
    	
    /**
     * Hook method called when the Activity is first launched to
     * initialize the content view and various data members.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the main content view.
        setContentView(R.layout.main_activity);
		
        // Cache a LinearLayout where each element is an
        // AutoCompleteTextview that holds a comma-separated list of
        // URLs to download.
        mListUrlGroups =
            (LinearLayout) findViewById(R.id.listOfURLLists);
        
        // Cache references to the buttons which become visible
        // when a valid USER input is given.
        mRunButton = (Button) findViewById(R.id.runWithUserURLs);
        mClearListsButton = (Button) findViewById(R.id.clearLists);
        mDefaultButton = (Button) findViewById(R.id.runWithDefaultURLs);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Initialize the Options singleton.
        Options.instance().parseArgs(null);

        // Setup a long-click callback to toggle between local and remote
        // url loading.
        mDefaultButton.setOnLongClickListener(view -> {
            mLocalDefaultMode = !mLocalDefaultMode;
            ((Button)view).setText(
                    getString(mLocalDefaultMode
                            ? R.string.default_local_button
                            : R.string.default_button));
            return true;
        });
    }
    
    /**
     * Run the ImageStreamGang using a default set of URL lists.
     */
    public void runWithDefaultURLs(View view) {
        //
    	runURLs(view,
                ((Button)view).getText().equals(
                        getString(R.string.default_button))
                                ? Options.InputSource.DEFAULT
                                : Options.InputSource.DEFAULT_LOCAL);
    }
    
    /**
     * Run the streamgang by reading the input lists of URLs.
     */
    public void runWithUserURLs(View view) {
    	runURLs(view, 
                Options.InputSource.USER);
    }
    
    /**
     * Helper method to run the appropriate configuration of
     * inputs and contexts
     */
    private void runURLs(View view, 
                         Options.InputSource inputSource) {
    	// Ensure the desired button was pressed (it must be visible).
    	if (view.getVisibility() != View.VISIBLE) 
            return; // no - op
    	
        // Create an iterator containing the URL to download.
        Iterator<List<URL>> iterator =
            Options.instance().getUrlIterator(this,
                                              mListUrlGroups,
                                              inputSource);
    	
        // Check to see if the user entered any lists.
        if (iterator != null) {
            if (iterator.hasNext() 
                && (inputSource != Options.InputSource.USER || !isEmpty()))
                new Thread(makeImageStream(mFilters,
                                           iterator,
                                           mCompletionHook)).start();
            setButtonsEnabled(false);
        } else 
            UiUtils.showToast(this,
                              "No list of URLs entered");
    }
	
    /**
     * Factory method that creates the ImageStream selected by the
     * user (or the default value of mStreamId).
     */
    private ImageStream makeImageStream(Filter[] filters,
                                        Iterator<List<URL>> urlListIterator,
                                        Runnable completionHook) {
        switch (mStreamId) {
        case R.id.sequential:
            UiUtils.showToast(this,
                              "Sequential stream processing");
            return new ImageStreamSequential(filters,
                                             urlListIterator,
                                             completionHook);
        case R.id.parallel:
            UiUtils.showToast(this,
                              "Parallel stream processing");
            return new ImageStreamParallel(filters,
                                           urlListIterator,
                                           completionHook);

        case R.id.completablefuture1:
            UiUtils.showToast(this,
                              "CompletableFuture1 stream processing");
            return new ImageStreamCompletableFuture1(filters,
                                                    urlListIterator,
                                                    completionHook);

        case R.id.completablefuture2:
            UiUtils.showToast(this,
                              "CompletableFuture2 stream processing");
            return new ImageStreamCompletableFuture2(filters,
                                                    urlListIterator,
                                                    completionHook);
        }
        // This should never happen!
        return null;
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
     * Helper method to properly transition to the results activity.
     */
    private void goToResultActivity() {
    	setButtonsEnabled(true);
        displayImages();
    }

    /**
     * Hook method that is called first when the activity context has
     * been created.  
     */
    @Override
    protected void onStart() {
        // Call up to the super class.
    	super.onStart();

        // Set the mSuggestions in onStart() so we are sure the
        // context exists and will not throw a NullPointerException.
        mSuggestions = 
            new ArrayAdapter<String>(this,
                                     R.layout.suggestion_item,
                                     sSUGGESTIONS);
    }
	
    /**
     * Adds a List of URLs to the ListView to allow for variable
     * number of URL Lists to process (i.e., variable number of
     * iteration cycles by the ImageStreamGang).
     */
    @SuppressLint("InflateParams")
    public void addURLs(View view) {
    	// Create the new list from R.layout.list_item.
        AutoCompleteTextView newList = 
            (AutoCompleteTextView) 
            LayoutInflater.from(this).inflate (R.layout.list_item,
                                               null);
        
        // Set the adapter to the given suggestions.
        newList.setAdapter(mSuggestions);
        
        // Add the view and invalidate it so that the layout is
        // redrawn by the framework.
        mListUrlGroups.addView(newList);
        mListUrlGroups.invalidate();
        
        // Set the appropriate buttons to visible
        mRunButton.setVisibility(View.VISIBLE);
        mClearListsButton.setVisibility(View.VISIBLE);
    }
    
    /**
     * Checks to see if all the lists are empty.
     */
    private boolean isEmpty() {
    	int listCount = mListUrlGroups.getChildCount();

    	for (int i = 0; i < listCount; ++i) {
            // Obtain a reference to the child list and check if text
            // has been entered.
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
     * forceful) way to keep multiple ImageStreamGang objects from
     * being started accidentally.
     */
    private void setButtonsEnabled(boolean enabled) {
    	LinearLayout buttonLayout = 
            (LinearLayout) findViewById(R.id.defaultButtonLayout);
    	int buttonCount = buttonLayout.getChildCount();

    	for (int i = 0; i < buttonCount; ++i) 
            buttonLayout.getChildAt(i).setEnabled(enabled);
    	
    	buttonLayout = 
            (LinearLayout) findViewById(R.id.userButtonLayout);
    	buttonCount = buttonLayout.getChildCount();

    	for (int i = 0; i < buttonCount; ++i) 
            buttonLayout.getChildAt(i).setEnabled(enabled);
    	
    	buttonLayout =
            (LinearLayout) findViewById(R.id.buttonLayoutBottom);
    	buttonCount = buttonLayout.getChildCount();

    	for (int i = 0; i < buttonCount; ++i) 
            buttonLayout.getChildAt(i).setEnabled(enabled);
    }

    /**
     * Clear the lists of URLs.
     */
    public void clearLists(View view) {
    	mListUrlGroups.removeAllViews();
    	mListUrlGroups.invalidate();
    	
    	// Set the appropriate buttons to visible
        mRunButton.setVisibility(View.INVISIBLE);
        mClearListsButton.setVisibility(View.INVISIBLE);
    }
	
    /**
     * Delete the previously downloaded pictures and directories.
     */
    public void clearFilterDirectories(View view) {
    	setButtonsEnabled(false);

        int deletedFiles = 0;

        for (Filter filter : mFilters) 
            deletedFiles += deleteSubFolders
                (new File(Options.instance().getDirectoryPath(), 
                          filter.getName()).getAbsolutePath());

        setButtonsEnabled(true);
        UiUtils.showToast(this,
                          deletedFiles
                          + " previously downloaded file(s) deleted");
    }
	
    /**
     * A helper method that recursively deletes files in a specified
     * directory. 
     */
    private int deleteSubFolders(String path) {
        int deletedFiles = 0;
        File currentFolder = new File(path);        
        File files[] = currentFolder.listFiles();

        if (files == null) 
            return 0;

        // Android does not allow you to delete a directory with child
        // files, so we need to write code that handles this
        // recursively.
        for (File f : files) {          
            if (f.isDirectory()) 
                deletedFiles += deleteSubFolders(f.toString());
            f.delete();
            deletedFiles++;
        }
        currentFolder.delete();
        return deletedFiles;
    }

    /**
     * Called by Android framework when menu option is clicked.
     * 
     * @param item Selected menu item.
     * @return true
     */
    public boolean chooseStream(MenuItem item) {
        // Record the user's menu choice.
        mStreamId = item.getItemId();
        return true;
    }

    /**
     * Inflates the given @a menu.
     *
     * @param menu Menu to inflate.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stream_menu,
                                  menu);

        // Always call super class method.
        return super.onCreateOptionsMenu(menu);
    }
}
