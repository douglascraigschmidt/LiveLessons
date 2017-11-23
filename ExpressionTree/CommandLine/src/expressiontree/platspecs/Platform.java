package expressiontree.platspecs;

/** 
 * Each platform (e.g., Android, command-line, etc.) subclasses from
 * this Singleton, which allows a client to send and retrieve data
 * from the expression tree processing app in a manner that isn't
 * exposed to the bulk of the app code.
 */
public abstract class Platform {
    /** 
     * The singleton {@code Platform} instance. 
     */
    private static Platform uniqueInstance = null;

    /** 
     * Method to return the one and only singleton instance. 
     */
    public static Platform instance() {
        return uniqueInstance;
    }

    /** 
     * Method that sets a new Platform singleton and returns the one
     * and only singleton instance.
     */
    public static Platform instance(Platform platform) {
        return uniqueInstance = platform;
    }

    /** 
     * Displays a line followed by a new line character. 
     */
    public abstract String outputLine(String line);

    /** 
     * Retrieves textual mInput previously determined by the user.
     */
    public abstract String retrieveInput(boolean verbose);
	
    /**
     * Displays the mInput string without a new line character.
     */
    public abstract String outputString(String input);
	
    /** 
     * Returns the name of the platform in a string. e.g., Android or
     * a JVM.
     */
    public abstract String platformName();

    /**
     * Depending on the platform, creates a proper menu. 
     */
    public abstract void outputMenu(String numeral,
                                    String option,
                                    String selection);
    
    /** 
     * Enables the respective option in Android (no-op in
     * commandLine).
     */
    public abstract void enableOption(String option);
    
    /** 
     * Implemented during the verbose android GUI.  Disables
     * unnecessary Radiobuttons so the prompt can initialize the
     * appropriate user interface.
     */
    public abstract void disableAll(boolean verbose);
	
    /**
     * Allows for the functionality of a print visitor within the
     * Android Verbose mode.  Continuously modifies the editText
     * instead of replacing its contents.
     */
    public abstract String addString(String Input);
    
    /**
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public abstract void errorLog(String javaFile, String errorMessage);

    /**
     * Make the constructor protected for a singleton.
     */
    protected Platform() {}
}
