package livelessons.imagestreamgang.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.FrameLayout;
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
     * The floating action button used to add urls
     */
    private FloatingActionButton mAddFab;

    /**
     * The floating action button used to run the ImageStreamGang using the user input
     */
    private FloatingActionButton mRunFab;

    /**
     * Boolean uesd to keep track so we know which addFab animation to use
     */
    private boolean mAnimatetoX;

    /**
     * Menu used to show/hide menu items
     */
    private Menu mMenu;

    /**
     * Menu item used to show/hide delete action bar item
     */
    private MenuItem mDeleteIcon;

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

        // Set mAnimatetoX to true so that we know we have to animate addFab from + to X
        mAnimatetoX = true;
		
        // Cache a LinearLayout where each element is an
        // AutoCompleteTextview that holds a comma-separated list of
        // URLs to download.
        mListUrlGroups =
            (LinearLayout) findViewById(R.id.listOfURLLists);

        // Cache references to the buttons
        mAddFab = (FloatingActionButton) findViewById(R.id.add_fab);
        mRunFab = (FloatingActionButton) findViewById(R.id.run_fab);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);


        // Initialize the Options singleton.
        Options.instance().parseArgs(null);
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
    	/*if (view.getVisibility() != View.VISIBLE)                             // Need to comment this out if using mini floating action buttons
            return; // no - op*/
    	
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

            // Make the delete menu item visible
            menuVisible();

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
                                     Options.instance().getSuggestions());
    }

    /**
     * FAB animator that displays the FAB.
     * @param fab The FAB to be displayed
     */
    public static void showFab(FloatingActionButton fab) {
        fab.show();
        fab.animate()
                .translationY(0)
                .setInterpolator(new DecelerateInterpolator(2))
                .start();
    }

    /**
     * FAB animator that hides the FAB.
     * @param fab The FAB to be hidden
     */
    public static void hideFab (FloatingActionButton fab) {
        fab.hide();
        fab.animate()
                .translationY(fab.getHeight() + 100)
                .setInterpolator(new AccelerateInterpolator(2))
                .start();
    }

    /**
     * Uses addURLs to inflate autocompleteview while changing the FAB icon
     */
    public void fabAdd(View view) {
        if (mAnimatetoX) {
            // Rotate the FAB from + to 'X'.
            int animRedId = R.anim.fab_rotate_forward;

            // Load and start the animation.
            mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                    animRedId));

            // Call the addURLs method to inflate the autocompletetextview
            int id = addURLs(view);

            // Set aAnimateX accordingly
            mAnimatetoX = false;

            // Reveal mRunFab
            showFab(mRunFab);

        } else {
            // Hide the add fab
            hideAddFab(view);
        }
    }

    /**
     * Hides the add fab
     */
    public void hideAddFab(View view) {
        // We need to rotate the other way and set mAnimateX accordingly
        mAnimatetoX = true;

        // Rotate the FAB from 'X' to '+'.
        int animRedId = R.anim.fab_rotate_backward;

        // Load and start the animation.
        mAddFab.startAnimation(AnimationUtils.loadAnimation(this,
                animRedId));

        clearLists(view);

        // Hide mRunFab
        hideFab(mRunFab);
    }

    /**
     * Adds a List of URLs to the ListView to allow for variable
     * number of URL Lists to process (i.e., variable number of
     * iteration cycles by the ImageStreamGang).
     */
    @SuppressLint("InflateParams")
    public int addURLs(View view) {
    	// Create the new list from R.layout.list_item.
        AutoCompleteTextView newList = 
            (AutoCompleteTextView) 
            LayoutInflater.from(this).inflate (R.layout.list_item,
                                               null);

        // Generate id for view so that it can be referenced later
        newList.setId(view.generateViewId());
        
        // Set the adapter to the given suggestions.
        newList.setAdapter(mSuggestions);
        
        // Add the view and invalidate it so that the layout is
        // redrawn by the framework.
        mListUrlGroups.addView(newList);
        mListUrlGroups.invalidate();

        // Return the id
        return newList.getId();
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
    	
    	/*buttonLayout =
            (LinearLayout) findViewById(R.id.buttonLayoutBottom);
    	buttonCount = buttonLayout.getChildCount();

    	for (int i = 0; i < buttonCount; ++i) 
            buttonLayout.getChildAt(i).setEnabled(enabled);*/
    }

    /**
     * Clear the lists of URLs.
     */
    public void clearLists(View view) {
    	mListUrlGroups.removeAllViews();
    	mListUrlGroups.invalidate();
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
     * This method checks whether there are files present that need to be deleted
     * @return Returns a boolean indicating whether such files are present or not
     */
    public boolean filesPresent() {
        int filesToDelete = 0;

        for (Filter filter : mFilters)
            filesToDelete += filesInSubDirectoriesPresent
                    (new File(Options.instance().getDirectoryPath(),
                            filter.getName()).getAbsolutePath());

        return filesToDelete > 0;
    }

    /**
     * A helper method that checks whether there are files to be deleted recursively
     */
    public int filesInSubDirectoriesPresent(String path) {
        int filesToDelete = 0;
        File currentFolder = new File(path);
        File files[] = currentFolder.listFiles();

        if (files == null)
            return 0;

        // Android does not allow you to delete a directory with child
        // files, so we need to write code that handles this
        // recursively.
        for (File f : files) {
            if (f.isDirectory())
                filesToDelete += deleteSubFolders(f.toString());
            filesToDelete++;
        }
        return filesToDelete;
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
     * Called by Android framework when menu option is clicked.
     *
     * @param item Selected menu item.
     */
    public void runWithDefaultURLs(MenuItem item) {
        // Record the user's menu choice.
        runURLs(item.getActionView(), Options.InputSource.DEFAULT);
    }

    /**
     * Called by Android framework when menu option is clicked.
     *
     * @param item Selected menu item.
     */
    public void runWithDefaultLocal(MenuItem item) {
        // Record the user's menu choice.
        runURLs(item.getActionView(), Options.InputSource.DEFAULT_LOCAL);
    }

    /**
     * Clears the directories on the click of the menu item without animating the mini fab
     */
    public void clearFilterDirectories(MenuItem item) {

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

        // Make the delete menu item invisible
        menuInvisible();
    }

    /**
     * Inflates the given @a menu.
     *
     * @param menu Menu to inflate.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Cache menu
        mMenu = menu;

        getMenuInflater().inflate(R.menu.stream_menu,
                                  menu);

        // Always call super class method.
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Is called before inflating menu bar
     * @param menu Menu to inflate
     * @return true to inflate the menu bar or false to leave it
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (filesPresent()) {
            menu.findItem(R.id.delete_menu).setVisible(true);
        }
        else {
            menu.findItem(R.id.delete_menu).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);
        chooseStream(item);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Makes the delete menu item visible
     */
    public void menuVisible() {
        mDeleteIcon = mMenu.findItem(R.id.delete_menu);
        mDeleteIcon.setVisible(true);
        //this.invalidateOptionsMenu();
    }

    /**
     * Makes the delete menu item invisible
     */
    public void menuInvisible() {
        mDeleteIcon = mMenu.findItem(R.id.delete_menu);
        mDeleteIcon.setVisible(false);
    }
}
