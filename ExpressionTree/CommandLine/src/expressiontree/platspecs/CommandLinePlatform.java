package expressiontree.platspecs;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * This class is used to retrieve and mOutput data from a console
 * window.  It plays the role of the "Concrete Strategy" in the
 * Strategy pattern.
 */
public class CommandLinePlatform 
       extends Platform {
    /** 
     * Contains information for grabbing mInput from console window.
     */
    InputStream mInput;

    /** 
     * Contains information for outputting to console window. 
     */
    private PrintStream mOutput;

    /** 
     * Constructor initializes the fields. */
    CommandLinePlatform(Object input, Object output) {
        mInput = (InputStream) input;
        mOutput = (PrintStream) output;
    }
	
    /**
     * Retrieves mInput from console and returns the value as a string.
     */
    public String retrieveInput(boolean verbose) {
        return new Scanner(mInput).nextLine();
    }

    /** 
     * Returns the string parameter to the console window followed by
     * a line. 
     */
    public String outputLine(String line) {
    	this.mOutput.println(line);
        return line;
    }

    /** 
     * Returns the string parameter to the console window (not
     * followed by newLine character). 
     */
    public String outputString(String string) {
        this.mOutput.print(string);
        return string;
    }

    /** Returns a string revealing the platform in use. */
    public String platformName() {
        return System.getProperty("java.specification.vendor");
    }
	
    /** Returns true if we're running on a command-line platform. */
    public boolean isCommandLinePlatform() {
        return true;
    }

    /** 
     * Depending on the platform, shows the user possible
     * commands. e.g., Format [in-order].
     */
    public void outputMenu(String numeral,
                           String option,
                           String selection) {
        mOutput.println(numeral + " " + option + " " + selection);
    }

    /** 
     * Enables the respective option in Android (no-op in
     * commandLine).
     */
    public void enableOption(String option) {
    }

    /** Specific to android platform and a no-op in this class. */
    public void disableAll(boolean verbose) {
        // no-op
    }

    /** 
     * Same as outputChar in commandLine platform, 
     * but serves separate purpose for android implementation. 
     */
    public String addString(String output) {
        mOutput.print(output);
        return output;
    }

    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile, String errorMessage) {
        System.out.println(javaFile + " " + errorMessage);
    }
}
