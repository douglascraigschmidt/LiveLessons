package example.imagetaskgang;

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
    public boolean mDiagnosticsEnabled = false;

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
     * Return the separator the indicates the break between 
     * different lists in the input URL file
     */
    public String getSeparator() {
		return mSeparator;
	}
    
    /**
     * Return the Input Source selection
     */
    public String getInputSource() {
		return mInputSource;
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
