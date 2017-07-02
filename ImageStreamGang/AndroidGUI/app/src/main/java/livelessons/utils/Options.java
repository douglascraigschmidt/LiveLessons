package livelessons.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import livelessons.platspec.PlatSpec;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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
        USER,           // Input from a user-defined source.
        FILE,           // Input from a delimited file.
        NETWORK,        // Input from a network call
        ERROR           // Returned if source is unrecognized.
    }

    /**
     * Keep track of the source of the inputs.
     */
    private InputSource mInputSource = InputSource.DEFAULT_LOCAL;

    /**
     * Default image names to use for testing.
     */
    private final String[] mDefaultImageNames = new String[] {
          "ka.png,"
        + "uci.png,"
        + "dougs_small.jpg,"
        + "kitten.png,"
        + "schmidt_coursera.jpg,"
        + "dark_rider.jpg,"
        + "doug.jpg",
          "lil_doug.jpg,"
        + "ironbound.jpg,"
        + "wm.jpg,"
        + "robot.png,"
        + "ace_copy.jpg,"
        + "tao_copy.jpg,"
        + "doug_circle.png"
    };

    /**
     * Prefix for all the URLs.
     */
    private static String sURL_PREFIX =
        //        "http://www.dre.vanderbilt.edu/~schmidt/gifs/";
        "http://www.cse.wustl.edu/~schmidt/gifs/";

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * Return the path for the directory where images are stored.
     */
    public String getDirectoryPath() {
        return PlatSpec.getDirectoryPath();
    }

    /**
     * Return the suggestions.
     */
    public String[] getSuggestions() {
        return Stream
            // Convert the array of strings (of comma-separated image
            // names) to a stream of strings.
            .of(mDefaultImageNames)

            // Map each string of image names into string of
            // comma-separated URL strings.
            .map(stringOfImageNames
                 -> Pattern
                     // Create a regular expression for the ","
                     // separator.
                     .compile(",")

                     // Use regular expression to split
                     // stringOfNames into a Stream<String>.
                     .splitAsStream(stringOfImageNames)

                     // Concatenate the url prefix with each name.
                     .map(name -> sURL_PREFIX + name)

                     // Combine all the URL strings together with
                     // a ',' between them.
                     .collect(joining(",")))

            // Convert the stream back into an array of strings.
            .toArray(String[]::new);
    }

    /**
     * Set the input source.
     */
    public void setInputSource(Options.InputSource inputSource) {
        mInputSource = inputSource;
    }

    /**
     * Takes a string input and returns the corresponding InputSource.
     */
    private InputSource getInputSource(String inputSource) {
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
    public Iterator<List<URL>> getUrlIterator(Object obj,
                                              Object listUrlGroups) {
    	List<List<URL>> urlLists = getUrlLists(obj,
                                               listUrlGroups);
    	return urlLists != null && urlLists.size() > 0
            ? urlLists.iterator()
            : null;
    }

    /**
     * Return an Iterator over one or more input URL Lists.
     */
    public Iterator<List<URL>> getUrlIterator() {
        List<List<URL>> urlLists =
            getUrlLists(null, null);
    	return urlLists != null && urlLists.size() > 0
            ? urlLists.iterator()
            : null;
    }

    /**
     * Gets the list of lists of URLs from which the user wants to
     * download images.
     */
    private List<List<URL>> getUrlLists(Object obj,
                                        Object listUrlGroups) {
    	try {
            switch (mInputSource) {
                // If the user selects the defaults source, return the
                // default list of remote URL lists.
            case DEFAULT:
                return getDefaultUrlList(obj, false);

                // If the user selects the default_local source, return the
                // default list of local URL lists.
            case DEFAULT_LOCAL:
                return getDefaultUrlList(obj, true);

                // Take input from the Android UI.
            case USER:
                return PlatSpec.getUrlLists(obj,
                                            listUrlGroups);

            default:
                System.out.println("Invalid Source");
                return null;
            }
    	} catch (MalformedURLException e) {
            System.out.println("Invalid URL");
            return null;
    	}
    }

    /**
     * Returns the appropriate list of URLs, i.e., either pointing to
     * the local device or to a remote server.
     */
    private List<List<URL>> getDefaultUrlList(Object context, boolean local)
        throws MalformedURLException {
        return local
            ? getDefaultResourceUrlList(context)
            : getDefaultUrlList(context);
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     * @param context
     */
    private List<List<URL>> getDefaultUrlList(Object context)
        throws MalformedURLException {
        return Stream
            // Convert the array of strings into a stream of strings.
            .of(mDefaultImageNames)

            // Map each string in the list into a list of URLs.
            .map(strings -> convertStringToUrls(context, strings))

            // Create and return a list of a list of URLs.
            .collect(toList());
    }

    /**
     * Returns a List of default URL Lists that is usable in either
     * platform.
     */
    private List<List<URL>> getDefaultResourceUrlList(Object context)
        throws MalformedURLException {
        return PlatSpec.getDefaultResourceUrlList(context,
                                                  mDefaultImageNames);
    }

    /**
     * Create a new URL list from a @a stringOfUrls that contains the
     * sURL_PREFIX list of names separated by commas and add them to
     * the URL list that's returned.
     */
    public List<URL> convertStringToUrls(Object context, String stringOfNames) {
        // Create a Function that returns a new URL object when
        // applied and which converts checked URL exceptions into
        // runtime exceptions.
        Function<String, URL> urlFactory = 
            ExceptionUtils.rethrowFunction(URL::new);

        return Pattern
            // Create a regular expression for the "," separator.
            .compile(",")

            // Use regular expression to split stringOfNames into a
            // Stream<String>.
            .splitAsStream(stringOfNames)

            // Concatenate the url prefix with each name.
            .map(name -> sURL_PREFIX + name)

            // Convert each string in the stream to a URL.
            .map(urlFactory::apply)

            // Create a list of URLs.
            .collect(toList());
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
                switch (argv[argc]) {
                case "-d":
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    break;
                case "-s":
                    mInputSource = getInputSource(argv[argc + 1]);
                    break;
                default:
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
        System.out.println("-s [DEFAULT|DEFAULT_LOCAL|USER|FILE]");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
