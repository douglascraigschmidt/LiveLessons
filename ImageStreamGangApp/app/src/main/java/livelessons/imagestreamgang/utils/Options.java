package livelessons.imagestreamgang.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.AnyRes;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import livelessons.imagestreamgang.R;

/**
 * This class implements the Singleton pattern to handle
 * command-line option processing.
 */
public class Options {
    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /**
     * An enumeration of each different input source.
     */
    public enum InputSource {
    	DEFAULT,        // The default remote input source.
        DEFAULT_LOCAL,  // The default local input source.
        USER,           // Input from a user-defined source, such as the Android UI
        FILE,           // Input from a delimited file.
        NETWORK,        // Input from a network call
        ERROR           // Returned if source is unrecognized.
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
        else if (inputSource.equalsIgnoreCase("DEFAULT_LOCAL"))
            return InputSource.DEFAULT_LOCAL;
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

    protected List<List<URL>> getDefaultUrlList(Context context, boolean local)
        throws MalformedURLException {
        return local
               ? getDefaultResourceUrlList(context)
               : getDefaultUrlList();
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    protected List<List<URL>> getDefaultUrlList()
            throws MalformedURLException {
        URL[] urls1 = {
            new URL("http://www.dre.vanderbilt.edu/~schmidt/ka.png"),
            new URL("http://www.dre.vanderbilt.edu/~schmidt/uci.png"),
            new URL("http://www.dre.vanderbilt.edu/~schmidt/gifs/dougs_small.jpg")
        };
        URL[] urls2 = {
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/lil_doug.jpg"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/wm.jpg"),
            new URL("http://www.cs.wustl.edu/~schmidt/gifs/ironbound.jpg")
        };

    	List<List<URL>> variableNumberOfInputURLs = new ArrayList<>();
        variableNumberOfInputURLs.add(Arrays.asList(urls1));
        variableNumberOfInputURLs.add(Arrays.asList(urls2));
    	return variableNumberOfInputURLs;
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    protected List<List<URL>> getDefaultResourceUrlList(Context context)
            throws MalformedURLException {
        URL[] urls1 = {
                new URL(getResourcesUrl(context, R.raw.ka)),
                new URL(getResourcesUrl(context, R.raw.uci)),
                new URL(getResourcesUrl(context, R.raw.dougs_small))
        };
        URL[] urls2 = {
                new URL(getResourcesUrl(context, R.raw.lil_doug)),
                new URL(getResourcesUrl(context, R.raw.wm)),
                new URL(getResourcesUrl(context, R.raw.ironbound))
        };

        List<List<URL>> variableNumberOfInputURLs = new ArrayList<>();
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
            // default list of remote URL lists.
            case DEFAULT:
                variableNumberOfInputURLs = getDefaultUrlList(context, false);
                break;

            // If the user selects the default_local source, return the
            // default list of local URL lists.
            case DEFAULT_LOCAL:
                variableNumberOfInputURLs = getDefaultUrlList(context, true);
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
        System.out.println("-i: URL-list-input-source [ DEFAULT | DEFAULT_LOCAL | USER | FILE ]");
        System.out.println("-s URL-list-separator");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }

    /**
     * Returns a URL String that will map to any application resource.
     *
     * @param context Any context.
     * @param resId Any resource id
     * @return A String URL that maps to the specified resource
     * @throws Resources.NotFoundException
     */
    private static String getResourcesUrl(Context context, @AnyRes int resId)
            throws Resources.NotFoundException {
        return getResourcesUri(context, resId).toString();
    }

    /**
     * Returns a Uri that will map to any application resource.
     *
     * @param context Any context
     * @param resId Any resource id
     * @return A Uri that maps to the specified resource
     * @throws Resources.NotFoundException
     */
    private static Uri getResourcesUri(Context context, @AnyRes int resId) {
        return Uri.parse(NetUtils.RESOURCE_BASE
                + context.getResources().getResourcePackageName(resId)
                + '/'
                + context.getResources().getResourceTypeName(resId)
                + '/'
                + context.getResources().getResourceEntryName(resId));
    }
}
