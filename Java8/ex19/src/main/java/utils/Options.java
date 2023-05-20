package utils;

/**
 * This class implements the Singleton pattern to handle
 * command-line option processing.
 */
public class Options {
    /**
     * The singleton @a Options instance.
     */
    private static Options mUniqueInstance = null;

    /**
     * The max depth for the webcrawler.
     */
    private int mMaxDepth = 2;

    /**
     * Starting point for the web crawling.
     */
    private String mRootUrl = "http://www.dre.vanderbilt.edu/~schmidt/imgs";

    /**
     * Starting point for local file crawling.
     */
    private final String mPathUri = "index.html";

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * A helper class that simplifies the web-based and local crawler
     * implementations.
     */
    private JSuper mJSuper;

    /**
     * Controls whether web-based or local crawling is performed.
     */
    private boolean mLocal = true;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * Return the max depth for the webcrawler.
     */
    public int maxDepth() {
        return mMaxDepth;
    }

    /**
     * Return the Uri that's used to initiate the crawling.
     */
    public String getRootUri() {
        return mLocal ? mPathUri : mRootUrl;
    }

    /**
     *
     */
    public JSuper getJSuper() {
        return mJSuper;
    }

    /**
     * Returns whether debugging output is generated.
     */
    public boolean getDiagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * Sets whether debugging output is generated.
     */
    public boolean setDiagnosticsEnabled(boolean enable) {
        return mDiagnosticsEnabled = enable;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc++)
                switch (argv[argc]) {
                    case "-d" -> mDiagnosticsEnabled = argv[++argc].equals("true");
                    case "-l" -> mLocal = true;
                    case "-m" -> mMaxDepth = Integer.parseInt(argv[++argc]);
                    case "-u" -> mRootUrl = argv[++argc];
                    case "-w" -> mLocal = false;
                    default -> {
                        printUsage(argv[argc]);
                        return;
                    }
                }

            // Set whether JSuper performs web-based or local
            // crawling.
            mJSuper = mLocal
                ? new JSuper(true)
                : new JSuper(false);
        }
    }

    /**
     * Conditionally prints the {@link String} depending on the current
     * setting of the {@link Options} singleton.
     */
    public static boolean print(int depth, String string) {
        if (Options.instance().getDiagnosticsEnabled()) {
            String s = "[thr "
                + Thread.currentThread().threadId()
                + ", depth "
                + depth
                + "]"
                + string;
            System.out.println(s);
        }
        return false;
    }


    /**
     * Log the results, regardless of whether an exception occurred or
     * not.
     *
     * @param totalImages The total number of images at this level
     *                    (this param may be null if an exception was
     *                    thrown)
     * @param ex The exception that occurred (this param may be null
     *           if no exception was thrown)
     * @param pageUri The URL that we're counting at this point
     * @param depth The current depth of the recursive processing
     */
    public static void logResults(Integer totalImages,
                                  Throwable ex,
                                  String pageUri,
                                  int depth) {
        if (totalImages != null)
            print(depth,
                  ": found "
                  + totalImages
                  + " images "
                  + pageUri);
        else
            print(depth,
                  ": exception " 
                  + ex.getMessage());
    }

    /**
     * Print out usage and default values.
     */
    public void printUsage(String arg) {
        System.out.println("""
            %s is an invalid argument
            Usage:
            -d [true|false]
            -l
            -m [maxDepth]
            -u [startingRootUrl]
            -w""");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
