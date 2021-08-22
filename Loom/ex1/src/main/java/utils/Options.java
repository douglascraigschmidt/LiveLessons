package utils;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.lang.Integer;
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
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Number of Thread and random Integer objects to create.
     */
    private int mNumberOfElements = 2000;

    /**
     * Create virtual threads if true, otherwise create platform
     * threads if false.
     */
    private boolean mVirtualThreads = true;

    /**
     * @return The one and only singleton uniqueInstance
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * @return True if debugging output is generated.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * @return The number of {@link Thread} objects to create
     */
    public int numberOfElements() {
        return mNumberOfElements;
    }

    /**
     * @return True if virtual threads requested, otherwise platform
     * threads.
     */
    public boolean virtualThreads() {
        return mVirtualThreads;
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
                case "-n":
                    mNumberOfElements = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-t":
                    mVirtualThreads = argv[argc + 1].equals("v");
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
        System.out.println("-n [numberOfElements]");
        System.out.println("-t [p|v]");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
