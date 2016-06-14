package livelessons.imagestreamgang;

import android.content.Context;
import android.os.Environment;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import livelessons.imagestreamgang.utils.UiUtils;

/**
 * @class Options
 * 
 * @brief This class implements the Singleton pattern to handle
 *        command-line option processing.
 */
public class Options {
    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /**
     * An enumeration of each different input source.
     */
    public static enum InputSource {
    	DEFAULT, // The default input source.
        USER,    // Input from a user-defined source, such as the Android UI
        FILE,    // Input from a delimited file.
        NETWORK, // Input from a network call
        ERROR    // Returned if source is unrecognized.
    }
    
    /**
     * Pathname for the file containing URLs to download.
     */
    private String mPathname = "defaultUrls.txt";
    
    /**
     * Separator that indicates the division of lists in the
     * URL file. Defaults to an empty line
     */
    private String mSeparator = "";
    
    /**
     * Input Source selection. Determines where the list
     * of URL lists will come from
     */
    private String mInputSource = "DEFAULT";

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * The path to the external storage directory in Android.
     */
    final static String EXTERNAL_PATH = 
        Environment.getExternalStorageDirectory().toString();

    /** 
     * Method to return the one and only singleton uniqueInstance. 
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * Return pathname for the file containing the URLs to download.
     */
    public String getURLFilePathname() {
        return mPathname;
    }
    
    /**
     * Return the path to the external storage directory in Android.
     */
    public String getDirectoryPath() {
        return EXTERNAL_PATH;
    }

    /**
     * Return the separator the indicates the break between 
     * different lists in the input URL file
     */
    public String getSeparator() {
		return mSeparator;
	}
    
    /**
     * Takes a string input and returns the corresponding InputSource.
     */
    public InputSource getInputSource(String inputSource) {
        if (inputSource.equalsIgnoreCase("DEFAULT")) 
            return InputSource.DEFAULT;
        else if (inputSource.equalsIgnoreCase("USER")) 
            return InputSource.USER;
        else if (inputSource.equalsIgnoreCase("FILE")) 
            return InputSource.FILE;
        else if (inputSource.equalsIgnoreCase("NETWORK"))
        	return InputSource.NETWORK;
        else 
            return InputSource.ERROR;
    }

    /**
     * Return an Iterator over one or more input URL Lists.
     */
    public Iterator<List<URL>> getUrlIterator(Context context,
                                              LinearLayout listUrlGroups,
                                              InputSource source) {
    	List<List<URL>> urlLists = getUrlLists(context, 
                                               listUrlGroups,
                                               source);
    	return urlLists != null && urlLists.size() > 0
            ? urlLists.iterator() 
            : null;
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    protected List<List<URL>> getDefaultUrlList() throws MalformedURLException {
    	List<List<URL>> variableNumberOfInputURLs = 
            new ArrayList<List<URL>>();

        URL[] urls1 = {        
            new URL("http://www.dre.vanderbilt.edu/~schmidt/ka.png"),
            new URL("http://www.dre.vanderbilt.edu/~schmidt/uci.png"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/douglass.jpg")
        };
        URL[] urls2 = {
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/lil-doug.jpg"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/wm.jpg"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/ironbound.jpg")
        };
        variableNumberOfInputURLs.add(Arrays.asList(urls1));
        variableNumberOfInputURLs.add(Arrays.asList(urls2));

    	return variableNumberOfInputURLs;
    }

    /**
     * Gets the list of lists of URLs from which we want to download
     * images.
     */
    public List<List<URL>> getUrlLists(Context context,
                                       LinearLayout listUrlGroups,
                                       InputSource source) {
    	List<List<URL>> variableNumberOfInputURLs = 
            new ArrayList<List<URL>>();
        	
    	try {
            switch (source) {
            // If the user selects the defaults source, return the
            // default list of URL lists.
            case DEFAULT:
                variableNumberOfInputURLs = getDefaultUrlList();
                break;
	            
            // Take input from the Android UI.
            case USER:
                // Iterate over the children of the LinearLayout that
                // holds the list of URL lists.
                int numChildViews = 
                    listUrlGroups.getChildCount();
                for (int i = 0; i < numChildViews; ++i) {
                    AutoCompleteTextView child = (AutoCompleteTextView)
                        listUrlGroups.getChildAt(i);
                        
                    // Create a new URL list and add each URL
                    // separated by commas to the list
                    List<URL> urls = new ArrayList<URL>();
                    StringTokenizer tokenizer =
                        new StringTokenizer(child.getText().toString(), ", ");

                    while (tokenizer.hasMoreTokens()) 
                        urls.add(new URL(tokenizer.nextToken().trim()));
                        
                    // Add the list of URLs to the main list
                    variableNumberOfInputURLs.add(urls);
                }

                break;
    			
            default:
                UiUtils.showToast(context,
                                  "Invalid Source");
                return null;
            }
    	} catch (MalformedURLException e) {
            UiUtils.showToast(context,
                              "Invalid URL");
            return null;
    	}
    	
    	return variableNumberOfInputURLs;
    }

    /**
     * Returns whether debugging output is generated.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String argv[]) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                if (argv[argc].equals("-f"))
                    mPathname = argv[argc + 1];
                else if (argv[argc].equals("-d"))
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                else if (argv[argc].equals("-s"))
                	mSeparator = argv[argc + 1];
                else if (argv[argc].equals("-i"))
                	mInputSource = argv[argc + 1];
                else {
                    printUsage();
                    return false;
                }
            return true;
        } else
            return false;
    }

    /** 
     * Print out usage and default values. 
     */
    public void printUsage() {
        System.out.println("\nHelp Invoked on ");
        System.out.println("[-hfs] ");
        System.out.println("");

        System.out.println("Usage: ");
        System.out.println("-d [true|false]");
        System.out.println("-f URL-file-pathame");
        System.out.println("-h: invoke help");
        System.out.println("-i: URL-list-input-source [ DEFAULT | USER | FILE ]");
        System.out.println("-s URL-list-separator");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
