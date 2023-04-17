package zippyisms.common;

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
     * Controls whether debugging is enabled, defaults to false.
     */
    private boolean mDebuggingEnabled;

    /**
     * Method to return the one and only singleton uniqueInstance.
     */
    public static Options instance() {
        if (sInstance == null)
            sInstance = new Options();

        return sInstance;
    }

    /**
     * @return True if deubbing is enabled, else false.
     */
    public boolean debuggingEnabled() {
        return mDebuggingEnabled;
    }

    /**
     * Print the debug string with thread information included if
     * diagnostics are enabled.
     */
    public static void debug(String tag, String string) {
        if (sInstance.mDebuggingEnabled)
            System.out.println(tag
                    + " ["
                    + Thread.currentThread().threadId()
                    + "] "
                    + string);
    }

    /**
     * Print the string with thread information included.
     */
    public static void print(String tag, String string) {
        System.out.println(tag
                + " ["
                + Thread.currentThread().threadId()
                + "] "
                + string);
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                if (argv[argc].equals("-d")) {
                    mDebuggingEnabled = argv[argc + 1].equals("true");
                } else {
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
        System.out.println("""
                -d [true|false]""");
    }

    /**
     * Print the {@code element} and the {@code operation} along with
     * the current thread name to aid debugging and comprehension.
     *
     * @param element   The given element
     * @param operation The Reactor operation being performed
     * @return The element parameter
     */
    public static <T> T logIdentity(T element, String operation) {
        System.out.println("["
                + Thread.currentThread().threadId()
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
    }
}
