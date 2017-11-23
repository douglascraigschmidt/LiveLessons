package expressiontree.input;

import expressiontree.tree.TreeOps;
import expressiontree.commands.NullCommand;
import expressiontree.commands.UserCommand;
import expressiontree.commands.UserCommandFactory;
import expressiontree.platspecs.Platform;

/**
 * Provides an abstract class for handling mInput events and commands
 * associated with the expression tree application.
 *
 * This class defines methods for use in the Template Method pattern
 * that is used to process user mInput commands.
 *
 * @see VerboseModeInputHandler and SuccinctModeInputHandler.
 */
public abstract class InputHandler {
    /** 
     * EditText object recognized on both platforms. 
     */
    private static Object mGlobalInput;
    
    /**
     * TextView object recognized on both platforms.  
     */
    private static Object mGlobalOutput;
	
    /** 
     * Classifies the Handler as verbose. 
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
        TreeOps treeOps = new TreeOps();
        mUserCommandFactory = new UserCommandFactory(treeOps);
        mLastValidCommand = new NullCommand(treeOps);
    }

    /**
     * Factory that creates the appropriate subclass of @a
     * InputHandler, i.e., @a VerboseModeInputHandler or @a *
     * SuccinctModeInputHandler.
     */
    static InputHandler makeHandler(boolean verbose,
                                    Object input,
                                    Object output,
                                    Object activity) {
        mGlobalInput = input;
        mGlobalOutput = output;
        sVerboseField = verbose;

        if(verbose) 
            return new VerboseModeInputHandler();
        else 
            return new SuccinctModeInputHandler();
    }
	
    /** 
     * This method is called back when mInput is available.  It
     * implements the Template Method pattern to perform the sequence
     * of steps associated with processing expression tree application
     * commands.
     */
    void handleInput() throws Exception {
        // Call a hook method to obtain user mInput.
        promptUser();
		
        // Retrieves mInput from user.
        String input = retrieveInput();
        
        if (input.equals(""))
            InputDispatcher.instance().endInputDispatching();
        else {
            // Call a hook method to make a command based on the user
            // mInput.
            UserCommand command = makeUserCommand(input);

            // Call a hook method to execute the command. 
            executeCommand(command);

            // Saves last command to a variable. 
            mLastValidCommand = command;
		
            // Displays the menu associated the the command. 
            command.printValidCommands(sVerboseField);
        }
    }

    /** 
     * This hook method is a placeholder for prompting the user for
     * mInput.
     */
    protected abstract void promptUser();

    /**
     * This hook method retrieves mInput.
     */
    private String retrieveInput() {
        return Platform.instance().retrieveInput(sVerboseField);
    }

    /**
     * This hook method is a placeholder for making a command based on
     * the user mInput.
     */
    protected abstract UserCommand makeUserCommand(String userInput);

    /**
     * This hook method executes a command. 
     */
    private void executeCommand(UserCommand command) throws Exception {
        command.execute();
    }
}
