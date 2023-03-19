package utils;

/**
 * This class implements the Singleton pattern to handle
 * command-line option processing.
 */
public class Options {
    /** The singleton @a Options instance. */
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
    private String mPathUri = "index.html";

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
    public boolean parseArgs(String argv[]) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc++)
                switch (argv[argc]) {
                case "-d":
                    mDiagnosticsEnabled = argv[++argc].equals("true");
                    break;
                case "-l":
                    mLocal = true;
                    break;
                case "-m":
                    mMaxDepth = Integer.valueOf(argv[++argc]);
                    break;
                case "-u":
                    mRootUrl = argv[++argc];
                    break;
                case "-w":
                    mLocal = false;
                    break;
                default:
                    printUsage(argv[argc]);
                    return false;
                }

            // Set whether JSuper performs web-based or local
            // crawling.
            mJSuper = mLocal
                ? new JSuper(true)
                : new JSuper(false);

            return true;
        } else
            return false;
    }

    /**
     * Print out usage and default values.
     */
    public void printUsage(String arg) {
        System.out.println(arg + " is an invalid argument\n" + "Usage: ");
        System.out.println("-d [true|false]");
        System.out.println("-l");
        System.out.println("-m [maxDepth]");
        System.out.println("-u [startingRootUrl]");
        System.out.println("-w");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
