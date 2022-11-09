package utils;

import java.util.concurrent.CompletableFuture;

/**
 * An options singleton.
 */
public class Options {
    /**
     * The input "works", which is a large recursive folder containing
     * thousands of subfolders and files.
     */
    public static final String sWORKS = "works";

    /**
     * A completed completable future to nothing.
     */
    public static final CompletableFuture<Void> sVoid
        = CompletableFuture.completedFuture(null);

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
     * Stores whether the user wants to run the tests in parallel.  Default
     * is not-parallel.
     */
    private boolean mParallel = false;

    /**
     * Must have a private constructor.
     */
    private Options() {
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-p")) 
                mParallel = true;
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
     * @return True if the user wants to run the tests in parallel,
     * else false
     */
    public boolean getParallel() {
        return mParallel;
    }
    
    /** 
     * Print out usage and default values. 
     */
    private void printUsage() {
        System.out.println("Options usage: \n-p -v");
    }

    /**
     * Display {@code string} if the program is run in verbose mode.
     */
    public static void display(String string) {
        if (Options.getInstance().getVerbose())
            System.out.println(string);
    }
}
