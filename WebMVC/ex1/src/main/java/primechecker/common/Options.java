package primechecker.common;

import ch.qos.logback.classic.Level;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    private boolean mDebugging = false;

    /**
     * Controls whether connections will be pooled.
     */
    private boolean mPoolConnections = true;

    /**
     * Controls how many longs are generated.
     */
    private int mCount = 100;

    /**
     * Controls the max value of the random numbers.
     */
    private int mMaxValue = Integer.MAX_VALUE;

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
    public boolean getDebug() {
        return mDebugging;
    }

    /**
     * @return The number of integers to generate.
     */
    public int getCount() {
        return mCount;
    }

    /**
     * Set the number of integers to generate.
     */
    public void setCount(int count) {
        mCount = count;
    }

    /**
     * @return The max value for the random numbers.
     */
    public int maxValue() {
        return mMaxValue;
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String string) {
        if (sInstance.mDebugging)
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
        if (sInstance.mDebugging)
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
                    case "-d" -> mDebugging = argv[argc + 1].equals("true");
                    case "-c" -> mCount = Integer.parseInt(argv[argc + 1]);
                    case "-m" -> mMaxValue = Integer.parseInt(argv[argc + 1]);
                    default -> {
                        printUsage();
                        return;
                    }
                }

            var count = System.getenv("COUNT");
            if (count != null)
                setCount(Integer.parseInt(count));

            if (mMaxValue - mCount <= 0)
                throw new IllegalArgumentException("maxValue - count must be greater than 0");
        }
    }

    /**
     * Iterate through the original List of prime candidates and
     * display both each prime candidate and the corresponding prime
     * result.
     *
     * @param primeCandidates A {@link List} of prime candidates
     * @param results A {@link List} containing the results of the
     *                primality checks
     */
    public static void displayResults(List<Integer> primeCandidates,
                                      List<Integer> results) {
        // Iterate through the original List of prime candidates and
        // conditionally print each prime candidate and the
        // corresponding prime result.
        for (int i = 0; i < primeCandidates.size(); i++) {
            var original = primeCandidates.get(i);
            var result = results.get(i);

            // assert original.equals(result);

            Options.debug("Result for "
                         + original
                         + " = "
                         + result);
        }
    }

    /**
     * Print out usage and default values.
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-c [n] "
                           + "-d [true|false] "
                           + "-m [maxValue] ");
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
