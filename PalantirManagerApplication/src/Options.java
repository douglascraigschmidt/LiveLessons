import java.io.File;
import java.nio.*;
import java.util.*;

/**
 * @class Options
 * 
 * @brief This class implements the Singleton pattern to handle the
 *        processing of command-line options.
 */
public class Options {
    /** The singleton @a Options instance. */
    private static Options mUniqueInstance = null;

    /** 
     * Number of Palantiri to allocate in the LeasePool (defaults to
     * 3).
     */
    private int mNumberOfPalantiri = 3;

    /** 
     * Number of Beings who will gaze at the Palantiri (defaults to
     * 5).
     */
    private int mNumberOfBeings = 5;

    /** 
     * Lease duration (defaults to 3000 milliseconds).
     */
    private int mLeaseDuration = 3000;

    /** 
     * Number of gazing iterations for each Being (defaults to 10).
     */
    private int mGazingIterations = 10;

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
     * Returns number of Palantiri to allocate in the LeasePool.
     */
    public int numberOfPalantiri() {
        return mNumberOfPalantiri;
    }

    /** 
     * Returns the number of Beings who will gaze at the Palantiri.
     */
    public int numberOfBeings() {
        return mNumberOfBeings;
    }

    /** 
     * Returns the lease duration.
     */
    public int leaseDuration() {
        return mLeaseDuration;
    }

    /**
     * Returns number of gazing iterations for each Being.
     */
    public int gazingIterations() {
        return mGazingIterations;
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
                if (argv[argc].equals("-b"))
                    mNumberOfBeings = Integer.parseInt(argv[argc + 1]);
                else if (argv[argc].equals("-d"))
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                else if (argv[argc].equals("-i"))
                    mGazingIterations = Integer.parseInt(argv[argc + 1]);
                else if (argv[argc].equals("-l"))
                    mLeaseDuration = Integer.parseInt(argv[argc + 1]);
                else if (argv[argc].equals("-p"))
                    mNumberOfPalantiri = Integer.parseInt(argv[argc + 1]);
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
        System.out.println("Help Invoked on Options [-bhlp] \n");
        System.out.println("Usage: ");
        System.out.println("-b number-of-Beings");
        System.out.println("-d [true|false]");
        System.out.println("-h: invoke help");
        System.out.println("-i: gazing-iterations");
        System.out.println("-l lease-duration");
        System.out.println("-p number-of-Palantiri");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }
}
