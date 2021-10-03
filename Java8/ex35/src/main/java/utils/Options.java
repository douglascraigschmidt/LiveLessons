package utils;

import java.io.File;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /** 
     * The singleton {@link Options} instance.
     */
    private static Options mUniqueInstance = null;

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Number of iterations to emit for the outer stream.
     */
    private int mIterations = 5;

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
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                case "-i":
                    mIterations = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-d":
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    break;
                default:
                    printUsage();
                    return;
                }
        }
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
    }

    /**
     * @return The number of iterations for the outer stream.
     */
    public int iterations() {
        return mIterations;
    }
}
