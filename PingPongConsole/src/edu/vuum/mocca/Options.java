package edu.vuum.mocca;

/**
 * @class Options
 * 
 * @brief This class implements the Singleton pattern to handle
 *        command-line option processing.
 */
public class Options {
    /**
     * The singleton @a Options instance. 
     */
    private static Options mUniqueInstance = null;

    /** 
     * Maximum number of iterations to run the program (defaults to
     * 10).
     */
    private int mMaxIterations = 10;

    /** 
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /** 
     * Number of iterations to run the program. 
     */
    public int maxIterations() {
        return mMaxIterations;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String argv[])
    {
        for (int argc = 0; argc < argv.length; argc += 2)
            if (argv[argc].equals("-i"))
                mMaxIterations = Integer.parseInt(argv[argc + 1]);
            else {
                printUsage();
                return false;
            }

        return true;
    }

    /** 
     * Print out usage and default values. 
     */
    public void printUsage() {
        System.out.println("\nHelp Invoked on ");
        System.out.println("[-hist] ");
        System.out.println("");
        System.out.println("");

        System.out.println("Usage: ");
        System.out.println("-h: invoke help ");
        System.out.println("-i max-number-of-iterations ");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
