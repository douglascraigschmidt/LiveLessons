package utils;

/**
 * An options singleton.
 */
public class Options {
    /**
     * The one and only singleton unique instance.
     */
    private static Options sInstance;
    
    /**
     * Return the one and only singleton unique instance.
     */
    public static Options getInstance() {
        if (sInstance == null)
            sInstance = new Options();

        return sInstance;
    }

    /**
     * Stores the designated input separator.
     */
    private String mInputSeparator = "@";

    /**
     * Must have a private constructor.
     */
    private Options() {
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("-s")) {
                mInputSeparator = args[1];
                return true;
            } else {
                printUsage();
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the input separator.
     */
    public String getInputSeparator() {
        return mInputSeparator;
    }
    
    /** 
     * Print out usage and default values. 
     */
    private void printUsage() {
        System.out.println("Options usage: \n-s [input-separator]");
    }
}
