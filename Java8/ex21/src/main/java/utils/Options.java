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
     * Max number of integers to process.
     */
    private int mMaxIntegers = 200_000_000;

    /**
     * The number of output elements to print.
     */
    private int mOutputLimit = 1_000;

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
     * @return True if debugging output should be generated, else
     * false
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * @return The maximum number of integers to process.
     */
    public int maxIntegers() {
        return mMaxIntegers;
    }

    /**
     * @return The limit of the numbers to output.
     */
    public int outputLimit() {
        return mOutputLimit;
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
                case "-l":
                    mOutputLimit = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-m":
                    mMaxIntegers = Integer.parseInt(argv[argc + 1]);
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
        System.out.println("-l [outputLimit]");
        System.out.println("-m [maxIntegers]");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
