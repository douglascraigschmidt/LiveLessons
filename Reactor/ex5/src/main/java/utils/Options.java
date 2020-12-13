package utils;

import reactor.core.publisher.FluxSink;
import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toList;

/**
 * This class implements the Singleton pattern to handle command-line
 * option processing.
 */
public class Options {
    /**
     * Logging tag.
     */
    private static final String TAG = Options.class.getName();

    /** 
     * The singleton {@code Options} instance.
     */
    private static Options sInstance = null;

    /**
     * Controls whether debugging output will be generated (defaults
     * to false).
     */
    private boolean mDiagnosticsEnabled = false;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (sInstance == null)
            sInstance = new Options();

        return sInstance;
    }

    /**
     * @return True if debugging output is printed, else false.
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
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
        if (sInstance.mDiagnosticsEnabled)
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
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
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
        System.out.println("-d [true|false] ");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
        // Disable the verbose/annoying Spring "debug" logging.
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger)
            LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(Level.toLevel("error"));
    }
}
