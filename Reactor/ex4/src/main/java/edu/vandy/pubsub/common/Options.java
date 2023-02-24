package edu.vandy.pubsub.common;

import ch.qos.logback.classic.Level;
import edu.vandy.pubsub.utils.Memoizer;
import edu.vandy.pubsub.utils.TimedMemoizer;
import edu.vandy.pubsub.utils.TimedMemoizerEx;
import edu.vandy.pubsub.utils.UntimedMemoizer;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.FluxSink;

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
     * Controls whether backpressure is enabled (defaults to true).
     */
    private boolean mBackPressureEnabled = true;

    /**
     * Controls how many longs are generated.
     */
    private int mCount = 100;

    /**
     * Controls the max value of the random numbers.
     */
    private int mMaxValue = Integer.MAX_VALUE;

    /**
     * The tags to use to control how {@code Options.debug()} behaves.
     */
    private List<String> mTagsList = new ArrayList<>();

    /**
     * Controls whether logging is enabled
     */
    private boolean mLoggingEnabled;

    /**
     * True if the producer and consumer should run in parallel, else
     * false.
     */
    private boolean mParallel = true;

    /**
     * The parallelism level if mParallel is true.  Defaults to 1.
     */
    private int mParallelism = 1;

    /**
     * Keeps track of the Memoizer strategy.
     * 'U' - UntimedMemoizer
     * 'T' - TimedMemoizer
     * 'X' - TimedMemoizerEx
     */
    private char mMemoizerStrategy = 'U';

    /**
     * Timeout period (in milliseconds) for the timed memoizers.
     * Defaults to 2 seconds.
     */
    private int mTimeout = 2_000;

    /**
     * Keeps track of the OverflowStrategy.
     * 'D' - DROP
     * 'B' - BUFFER
     * 'E' - ERROR
     * 'I' - IGNORE
     * 'L' - LATEST
     */
    private char mOverflowStrategy = 'I';

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
     * @return True if backpressure is enabled, else false.
     */
    public boolean backPressureEnabled() {
        return mBackPressureEnabled;
    }

    /**
     * @return True the producer and consumer should run in parallel,
     * else false.
     */
    public boolean parallel() {
        return mParallel;
    }

    /**
     * @return The parallelism level.
     */
    public int parallelism() {
        return mParallelism;
    }

    /**
     * @return Return the overflow strategy.
     */
    public FluxSink.OverflowStrategy overflowStrategy() {
        switch(mOverflowStrategy) {
        case 'D': return FluxSink.OverflowStrategy.DROP;
        case 'B': return FluxSink.OverflowStrategy.BUFFER;
        case 'E': return FluxSink.OverflowStrategy.ERROR;
        case 'I': return FluxSink.OverflowStrategy.IGNORE;
        case 'L': return FluxSink.OverflowStrategy.LATEST;
        default: throw new IllegalArgumentException("invalid OverflowStrategy");
        }
    }

    /**
     * @return The number of integers to generate.
     */
    public int count() {
        return mCount;
    }

    /**
     * @return Return the timeout.
     */
    public long timeout() {
        return mTimeout;
    }

    /**
     * @return The max value for the random numbers.
     */
    public int maxValue() {
        return mMaxValue;
    }

    /**
     * @return True if logging is enabled, else false.
     */
    public boolean loggingEnabled() {
        return mLoggingEnabled;
    }

    /**
     * Make the requested memoizer.
     */
    public static Memoizer<Integer, Integer> makeMemoizer
        (Function<Integer, Integer> function) {
        switch(instance().mMemoizerStrategy) {
        case 'U': return new UntimedMemoizer<>(function);
        case 'T': return new TimedMemoizer<>
                (function,
                 instance().mTimeout);
        case 'X': return new TimedMemoizerEx<>
                (function,
                 instance().mTimeout);
        default: throw new IllegalArgumentException("given memoizer type unknown");
        }
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
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                case "-b":
                    mBackPressureEnabled = argv[argc + 1].equals("true");
                    break;
                case "-d":
                    mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    break;
                case "-l":
                    mLoggingEnabled = argv[argc + 1].equals("true");
                        break;
                case "-c":
                    mCount = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-m":
                    mMaxValue = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-M":
                    mMemoizerStrategy = argv[argc + 1].charAt(0);
                    break;
                case "-o":
                    mOverflowStrategy = argv[argc + 1].charAt(0);
                    break;
                case "-p":
                    mParallel = argv[argc + 1].equals("true");
                    break;
                case "-P":
                    mParallelism = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-t":
                    mTimeout = Integer.parseInt(argv[argc + 1]);
                    break;
                case "-T":
                    mTagsList = Pattern
                        .compile(",")
                        .splitAsStream(argv[argc + 1])
                        .collect(toList());
                    break;
                default:
                    printUsage();
                    return;
                }
            if (mMaxValue - mCount <= 0)
                throw new IllegalArgumentException("maxValue - count must be greater than 0");
        }
    }

    /**
     * Print out usage and default values.
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-b [true|false]"
                           + "-c [n] "
                           + "-d [true|false] "
                           + "-l [true|false] "
                           + "-m [maxValue] "
                           + "-M [T|U|X]"
                           + "-o [B|D|E|I|L]"
                           + "-p [true|false]"
                           + "-P [parallelism]"
                           + "-t [timeoutInMillis]"
                           + "-T [tag,...]");
    }

    /**
     * Print the {@code element} and the {@code operation} along with
     * the current thread name to aid debugging and comprehension.
     *
     * @param element The given element
     * @param operation The Reactor operation being performed
     * @return The element parameter
     */
    public static <T> T logIdentity(T element, String operation) {
        System.out.println("["
                           + Thread.currentThread().getName()
                           + "] "
                           + operation
                           + " -- " 
                           + element);
        return element;
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
