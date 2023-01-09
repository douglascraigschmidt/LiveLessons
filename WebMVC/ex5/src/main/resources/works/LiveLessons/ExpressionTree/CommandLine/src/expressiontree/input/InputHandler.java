package expressiontree.input;

import expressiontree.tree.TreeContext;
import expressiontree.commands.NullCommand;
import expressiontree.commands.UserCommand;
import expressiontree.commands.UserCommandFactory;
import expressiontree.platspecs.Platform;

/**
 * Provides an abstract class for handling input events and commands
 * associated with the expression tree processing app.
 *
 * This class defines methods for use in the Template Method pattern
 * that is used to process user input commands.
 *
 * @see VerboseModeInputHandler and SuccinctModeInputHandler.
 */
public abstract class InputHandler {
    /** 
     * Input object recognized on Android and command-line platforms.
     */
    private static Object sGlobalInput;
    
    /**
     * Output object recognized on Android and command-line platforms.
     */
    private static Object sGlobalOutput;
	
    /** 
     * Classifies the InputHandler as verbose (true) or succinct
     * (false).
     */
    static boolean sVerboseField;

    /**
     * A factory for creating a command. 
     */
    UserCommandFactory mUserCommandFactory;
	
    /** 
     * Handle to last valid command that was executed. 
     */
    private UserCommand mLastValidCommand;
	
    /**
     * Constructor. 
     */
    InputHandler() {
        // The context where the expression tree state resides.
        TreeContext treeContext = new TreeContext();

        // The factory used to create commands.
        mUserCommandFactory = new UserCommandFactory(treeContext);

        // Handle to last valid command that was executed.
        mLastValidCommand = new NullCommand(treeContext);
    }

    /**
     * Factory that creates an InputHandler configured according to
     * the parameters.
     *
     * @param verbose Determine whether to use verbose (true) or succinct (false) mode.
     * @param platform Provides input and output according to the configured runtime platform.
     */
    static InputHandler makeHandler(boolean verbose,
                                    Platform platform) {
        sGlobalInput = platform.getInputSource();
        sGlobalOutput = platform.getOutputSink();
        sVerboseField = verbose;

        if (verbose) 
            return new VerboseModeInputHandler();
        else 
            return new SuccinctModeInputHandler();
    }

    /**
     * Factory that initializes the InputHandler from an existing
     * {@code inputHandler}.
     *
     * @param platform Provides input and output according to the configured runtime platform.
     * @param inputHandler InputHandler to use for testing.
     * @return The {@code inputHandler} parameter.
     */
    public static InputHandler makeHandler(Platform platform,
                                           InputHandler inputHandler) {
        sGlobalInput = platform.getInputSource();
        sGlobalOutput = platform.getOutputSink();
        return inputHandler;
    }
	
    /** 
     * This method is called back when input is available.  It
     * implements the Template Method pattern to perform the sequence
     * of steps associated with processing expression tree application
     * commands.
     */
    public void handleInput() throws Exception {
        // Call hook method to obtain user input.
        promptUser();
		
        // Retrieve input from user.
        String input = retrieveInput();

        // The empty string indicates it's time to shutdown the event
        // loop.
        if (input.equals(""))
            InputDispatcher.instance().endInputDispatching();
        else {
            // Call hook method to make command based on user input.
            UserCommand command = makeUserCommand(input);

            // Call hook method to execute the command.
            executeCommand(command);

            // Save last command to a variable. 
            mLastValidCommand = command;
		
            // Display the menu associated the the command.
            command.printValidCommands(sVerboseField);
        }
    }

    /** 
     * This hook method is a placeholder for prompting the user for
     * input.
     */
    protected abstract void promptUser();

    /**
     * This hook method retrieves input.
     */
    protected String retrieveInput() {
        return Platform.instance().retrieveInput(sVerboseField);
    }

    /**
     * This hook method is a placeholder for making a command based on
     * the user input.
     */
    protected abstract UserCommand makeUserCommand(String userInput);

    /**
     * This hook method executes a command. 
     */
    protected void executeCommand(UserCommand command) throws Exception {
        command.execute();
    }
}
