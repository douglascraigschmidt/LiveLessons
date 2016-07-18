package livelessons.imagestreamgang.utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import livelessons.imagestreamgang.R;

import static java.util.stream.Collectors.toList;
import static livelessons.imagestreamgang.utils.ExceptionUtils.rethrowFunction;

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
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * The path to the external storage directory in Android.
     */
    private final static String EXTERNAL_PATH =
        Environment.getExternalStorageDirectory().toString();

    /**
     * Suggestions of default URLs that are supposed to be presented
     * to the user via AutoCompleteTextView.
     */
    private final String[] sSUGGESTIONS = new String[] {        
        "http://www.dre.vanderbilt.edu/~schmidt/ka.png,"
        + "http://www.dre.vanderbilt.edu/~schmidt/uci.png,"
        + "http://www.dre.vanderbilt.edu/~schmidt/gifs/dougs-small.jpg",
        "http://www.cs.wustl.edu/~schmidt/gifs/lil-doug.jpg,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/wm.jpg,"
        + "http://www.cs.wustl.edu/~schmidt/gifs/ironbound.jpg"
    };
    
    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * Return the suggestions.
     */
    public String[] getSuggestions() {
        return sSUGGESTIONS;
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

    /**
     * Returns the appropriate list of URLs, i.e., either pointing to
     * the local device or to a remote server.
     */
    protected List<List<URL>> getDefaultUrlList(Context context,
                                                boolean local)
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
    	List<List<URL>> variableNumberOfInputURLs = new ArrayList<>();

        // Convert all the suggestion strings into URLs.
        for (String suggestedUrls : sSUGGESTIONS)
            variableNumberOfInputURLs.add
                (convertStringToUrls(suggestedUrls));

    	return variableNumberOfInputURLs;
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    protected List<List<URL>> getDefaultResourceUrlList(Context context)
            throws MalformedURLException {
        // Create a two-dimensional array of URLs to images on the local device.
        URL[][] urlsArray = new URL[][] {
            {
                new URL(getResourcesUrl(context, R.raw.ka)),
                new URL(getResourcesUrl(context, R.raw.uci)),
                new URL(getResourcesUrl(context, R.raw.dougs_small))
            },
            {    
                new URL(getResourcesUrl(context, R.raw.lil_doug)),
                new URL(getResourcesUrl(context, R.raw.wm)),
                new URL(getResourcesUrl(context, R.raw.ironbound))
            }
        };

        List<List<URL>> variableNumberOfInputURLs = new ArrayList<>();

        for (URL[] urls : urlsArray) {
            // Create a new List of URLs containing the next URLs from
            // the array.
            variableNumberOfInputURLs.add(Arrays.asList(urls));
        }

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
            new ArrayList<>();

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

                    // Convert the input string into a list of URLs
                    // and add it to the main list.
                    variableNumberOfInputURLs.add
                        (convertStringToUrls(child.getText().toString()));
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
     * Create a new URL list from a @a stringOfUrls that contains a
     * list of URLs separated by commas and add them to the URL list
     * that's returned.
     */
    private List<URL> convertStringToUrls(String stringOfUrls) {
        // Create a Function that returns a new URL object when applied.
        Function<String, URL> urlFactory = rethrowFunction(URL::new);
        return
            // Create a regular expression for the "," separator.
            Pattern.compile(",")

            // Use the regular expression to split the strongOfUrls into a Stream<String>.
            .splitAsStream(stringOfUrls)

            // Convert each String in the Stream to a URL.
            .map(urlFactory::apply)

            // Create a list of URLs.
            .collect(toList());
        /*

        return
            // Convert the stringOfUrls parameter into a Stream.
            Stream.of(stringOfUrls)

            // Split the stringOfUrls into a string[].
            .map(s -> s.split(","))

            // Convert the string[] into a flatted Stream<String>.
            .flatMap(Arrays::stream)


            .map(s -> (urlFactory.apply(s)))

            // Create a list of URLs.
            .collect(toList());
            */
        /*
        List<URL> urls = new ArrayList<>();

        for (StringTokenizer tokenizer =
                 new StringTokenizer(stringOfUrls, ", ");
             tokenizer.hasMoreTokens();
             )
            try {
                // Create a new URL containing the next URL from the
                // stream.
                urls.add(new URL(tokenizer.nextToken().trim()));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        */
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
                if (argv[argc].equals("-d"))
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
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
        System.out.println("Usage: ");
        System.out.println("-d [true|false]");
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
    private static String getResourcesUrl(Context context, int resId)
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
    private static Uri getResourcesUri(Context context, int resId) {
        return Uri.parse(NetUtils.RESOURCE_BASE
                + context.getResources().getResourcePackageName(resId)
                + '/'
                + context.getResources().getResourceTypeName(resId)
                + '/'
                + context.getResources().getResourceEntryName(resId));
    }
}
