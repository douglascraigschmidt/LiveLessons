package utils;

import java.lang.Integer;

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
     * Number of {@link Thread} and random {@link Integer} objects to create.
     */
    private int mNumberOfElements = 2000;

    /**
     * Create virtual threads if true, otherwise create platform
     * threads if false.
     */
    private boolean mVirtualThreads = true;

    /**
     * The iteration when a diagnostic should be printed.
     */
    private int mPrintDiagnosticOnIteration = 100;

    /**
     * @return The one and only singleton uniqueInstance
     */
    public static Options instance() {
        if (mUniqueInstance == null)
            mUniqueInstance = new Options();

        return mUniqueInstance;
    }

    /**
     * @return True if debugging output is generated
     */
    public boolean diagnosticsEnabled() {
        return mDiagnosticsEnabled;
    }

    /**
     * @return The number of {@link Thread} and random {@link Integer}
     *         objects to create
     */
    public int numberOfElements() {
        return mNumberOfElements;
    }

    /**
     * @return True if virtual threads requested, otherwise platform
     * threads
     */
    public boolean virtualThreads() {
        return mVirtualThreads;
    }

    /**
     * @return True if {@code i} modulus the print diagnostic == 0, else false
     */
    public boolean printDiagnostic(int i) {
        return mDiagnosticsEnabled
                && (i % mPrintDiagnosticOnIteration) == 0;
    }

    /**
     * Parse command-line arguments and set the appropriate values.
     */
    public void parseArgs(String[] argv) {
        if (argv != null) {
            for (int argc = 0; argc < argv.length; argc += 2)
                switch (argv[argc]) {
                    case "-d" -> mDiagnosticsEnabled = argv[argc + 1].equals("true");
                    case "-n" -> mNumberOfElements = Integer.parseInt(argv[argc + 1]);
                    case "-p" -> mPrintDiagnosticOnIteration = Integer.parseInt(argv[argc + 1]);
                    case "-t" -> mVirtualThreads = argv[argc + 1].equals("v");
                    default -> {
                        printUsage();
                        return;
                    }
                }
        }
    }

    /**
     * Print out usage and default values.
     */
    private void printUsage() {
        System.out.println("Usage: ");
        System.out.println("-d [true|false]\n"
                           + "-i [iteration]\n"
                           + "-n [numberOfElements]\n"
                           + "-t [p|v]");
    }

    /**
     * Make the constructor private for a singleton.
     */
    private Options() {
    }

    /**
     * Print the {@link String} param after first prepending
     * it with the calling thread name.
     */
    public static void display(String string) {
        System.out.println("["
                           + Thread.currentThread()
                           + "] "
                           + string);
    }
}
