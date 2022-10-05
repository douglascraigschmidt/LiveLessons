package utils;

import static java.util.AbstractMap.SimpleImmutableEntry;

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
     * Maximum value of random numbers.
     */
    private int mMaxValue = 1000000000;

    /**
     * Number of threads to use.
     */
    private static int mNumberOfThreads = 10;

    /**
     * Number of random numbers generated.
     */
    private int mRandomNumberCount = 100;

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
                case "-d":
                    mDiagnosticsEnabled = argv[++argc].equals("true");
                    break;
                case "-m":
                    mMaxValue = Integer.parseInt(argv[++argc]);
                    break;
                case "-r":
                    mRandomNumberCount = Integer.parseInt(argv[++argc]);
                    break;
                case "-t":
                    mNumberOfThreads = Integer.parseInt(argv[++argc]);
                    break;
                default:
                    printUsage(argv[argc]);
                    return;
                }
        }
    }

    /**
     * Print out usage and default values.
     */
    public void printUsage(String arg) {
        System.out.println(arg + " is an invalid argument\n" + "Usage: ");
        System.out.println("-d [true|false]");
        System.out.println("-m [max value]");
        System.out.println("-r [ransom number count]");
        System.out.println("-t [number of threads]");
    }

    /**
     * Print the result.
     *
     * @param result The result to print.
     */
    public static void printResult(SimpleImmutableEntry<Long, Long> result) {
        if (mUniqueInstance.mDiagnosticsEnabled)
            System.out.println("Prime candidate "
                               + result.getKey()
                               + " has a smallest factor of "
                               + result.getValue());
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }

    /**
     *
     * @return
     */
    public int randomNumberCount() {
        return mRandomNumberCount;
    }

    /**
     *
     * @return
     */
    public int maxValue() {
        return mMaxValue;
    }

    /**
     *
     * @return
     */
    public int numberOfThreads() {
        return mNumberOfThreads;
    }
}

