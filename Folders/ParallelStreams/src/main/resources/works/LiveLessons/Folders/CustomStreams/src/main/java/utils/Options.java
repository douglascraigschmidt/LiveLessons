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
     * Stores whether the user wants verbose output or not.  Default
     * is no verbose output.
     */
    private boolean mVerbose = false;

    /**
     * Stores whether the user wants to create a recursive
     * spliterators.  Default is false (i.e., a batch spliterator
     * is used by default).
     */
    private boolean mRecursiveSpliterator = false;

    /**
     * A singleton should have a private constructor.
     */
    private Options() {
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-r")) 
                mRecursiveSpliterator = true;
            else if (args[i].equals("-v")) 
                mVerbose = true;
            else {
                printUsage();
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the mVerbose field to the value of @a verbose.
     */
    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }

    /**
     * @return True if the user wants "verbose" output, else false
     */
    public boolean getVerbose() {
        return mVerbose;
    }

    /**
     * @return True if the user wants to create a recursive
     * spliterator else false
     */
    public boolean useRecursiveSpliterator() {
        return mRecursiveSpliterator;
    }
    
    /** 
     * Print out usage and default values. 
     */
    private void printUsage() {
        System.out.println("Options usage: \n-r -v");
    }
}
