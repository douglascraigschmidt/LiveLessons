package folders.common;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * The options singleton.
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
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Keeps track of whether to run the tests sequentially or not.
     * Defaults to true.
     */
    private boolean mSequential = true;

    /**
     * Keeps track of whether to run the tests concurrently or not.
     * Defaults to false.
     */
    private boolean mConcurrent = false;

    /**
     * Keeps track of whether logging is enabled.  Default is false.
     */
    private boolean mLoggingEnabled = false;

    /**
     * Keeps track of whether to run the tests in parallel or not.
     * Default is false;
     */
    private boolean mParallel = false;

    /**
     * Keeps track of whether to run the tests remotely.
     */
    private boolean mRemote;

    /**
     * Keeps track of whether to run the tests locally.
     */
    private boolean mLocal;

    /**
     * @return True if debugging output is printed, else false.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * The tags to use to control how {@code Options.debug()} behaves.
     */
    private List<String> mTagsList = new ArrayList<>();

    /**
     * A singleton should have a private constructor.
     */
    private Options() {
        // Disable the verbose/annoying Spring "debug" logging.
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public boolean parseArgs(String[] argv) {
        // Process the arguments.
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                case "-c":
                    mConcurrent = argv[argc + 1].equals("true");
                    break;
                case "-d":
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    break;
                case "-l":
                    mLocal = argv[argc + 1].equals("true");
                    break;
                case "-p":
                    mParallel = argv[argc + 1].equals("true");
                    break;
                case "-r":
                    mRemote = argv[argc + 1].equals("true");
                    break;
                case "-s":
                    mSequential = argv[argc + 1].equals("true");
                    break;
                case "-T":
                    mTagsList = Pattern
                        .compile(",")
                        .splitAsStream(argv[argc + 1])
                        .collect(toList());
                    break;
                case "-v":
                    mLoggingEnabled = argv[argc + 1].equals("true");
                    break;
                default:
                    printUsage();
                    return false;
                }
        }

        return true;
    }

    /** 
     * Print out usage and default values. 
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-c [true|false]"
                           + "-d [true|false] "
                           + "-l [true|false] "
                           + "-p [true|false]"
                           + "-r [true|false]"
                           + "-T [tag,...]"
                           + "-v [true|false]");
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String string) {
        if (sInstance.mDiagnosticsEnabled)
            System.out.println("[" +
                    Thread.currentThread().getName()
                    + "] "
                    + string);
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String tag, String string) {
        if (sInstance.mDiagnosticsEnabled
            && sInstance.mTagsList.contains(tag))
            Options.debug(string);
    }

    /**
     * Print the string with thread information included.
     */
    public static void print(String string) {
        System.out.println("[" +
                           Thread.currentThread().getName()
                           + "] "
                           + string);
    }

    /**
     *
     * @return
     */
    public boolean sequential() {
        return mSequential;
    }

    /**
     *
     * @return
     */
    public boolean concurrent() {
        return mConcurrent;
    }

    /**
     *
     * @return True if remote testing is enabled, else false.
     */
    public boolean remote() {
        return mRemote;
    }

    /**
     *
     * @return True if local testing is enabled, else false.
     */
    public boolean local() {
        return mLocal;
    }

    /**
     *
     * @return
     */
    public boolean parallel() {
        return mParallel;
    }
}
