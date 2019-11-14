package utils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /** 
     * The singleton @a Options instance. 
     */
    private static Options mUniqueInstance = null;

    /**
     * The path to the image directory.
     */
    private static final String IMAGE_DIRECTORY_PATH =
        "DownloadImages";

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
        + "doug_circle.png",
          "NYNY.jpg,"
        + "parrot.jpg,"
        + "rush-logo.jpg,"
        + "complaints.jpg,"
        + "doc.jpg,"
        + "java-ace.gif,"
        + "bee-patterns.jpg,"
    };

    /**
     * Prefix for all the URLs.
     */
    private static String sURL_PREFIX =
        "http://www.dre.vanderbilt.edu/~schmidt/gifs/";
    // "http://www.cse.wustl.edu/~schmidt/gifs/";

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
        return new File(IMAGE_DIRECTORY_PATH).getAbsolutePath();
    }

    /**
     * Returns a list of URLs.
     */
    public List<URL> getUrlList() {
        return Stream
            // Convert the array of strings into a stream of strings.
            .of(mDefaultImageNames)

            // Map each string in the list into a list of URLs.
            .flatMap(this::convertStringToUrls)

            // Create and return a list of a list of URLs.
            .collect(toList());
    }

    /**
     * Create a new URL list from a @a stringOfUrls that contains the
     * sURL_PREFIX list of names separated by commas and add them to
     * the URL list that's returned.
     */
    private Stream<URL> convertStringToUrls(String stringOfNames) {
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
            .map(urlFactory);
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
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false]");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
        // Create the path to the image directory.
        File imageDirectory =
            new File(getDirectoryPath());

        // Create the image directory.
        imageDirectory.mkdirs();
    }
}
