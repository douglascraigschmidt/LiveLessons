package com.example.expressiontree;

import java.util.Scanner;

/**
 * @class InputHandler
 *
 * @brief Provides an abstract class for handling input events and
 *        commands associated with the expression tree application.
 *
 *        This class defines methods for use in the Template Method
 *        pattern that is used to process user input commands.
 *
 * @see   @ VerboseModeInputHandler and @ SuccinctModeInputHandler.
 */
public abstract class InputHandler {
    /** EditText object recognized on both platforms. */
    public static Object globalInput;
    
    /** TextView object recognized on both platforms.  */
    public static Object globalOutput;
	
    /** Classifies the Handler as verbose. */
    static boolean verboseField;
	
    /** An activity object for the verbose mode event handler. */
    public static Object activityField;
	
    /** The context where the expression tree state resides. */
    protected TreeOps treeOps;
	
    /** A factory for creating a command. */
    protected UserCommandFactory userCommandFactory;
	
    /** Handle to last valid command that was executed. */
    protected UserCommand lastValidCommand ;
	
    /** Constructor. */
    InputHandler() {
        treeOps = new TreeOps();
        userCommandFactory = new UserCommandFactory(treeOps);
        UserCommand f = new NullCommand(treeOps);
        lastValidCommand = f;
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
        globalInput = input;
        globalOutput = output;
        verboseField = verbose;
        activityField = activity;
		
        if(verbose) 
            return new VerboseModeInputHandler();
        else 
            return new SuccinctModeInputHandler();
    }
	
    /** 
     * This method is called back when input is available.  It
     * implements the Template Method pattern to perform the sequence
     * of steps associated with processing expression tree application
     * commands.
     */
    public void handleInput() throws Exception {
        /** Call a hook method to obtain user input. */
        promptUser();
		
        /** Retrieves input from user. */
        String input = retrieveInput();
        
        if (input.equals(""))
            InputDispatcher.instance().endInputDispatching();
        else {
            /** Call a hook method to make a command based on the user input. */
            UserCommand command = makeUserCommand(input);

            /** Call a hook method to execute the command. */
            executeCommand(command);

            /** Saves last command to a variable. */
            lastValidCommand = command;
		
            /** Displays the menu associated the the command. */
            command.printValidCommands(verboseField);
        }
    }

    /** 
     * This hook method is a placeholder for prompting the user for
     * input.
     */
    protected abstract void promptUser();

    /** This hook method retrieves input. */
    String retrieveInput() {
        return Platform.instance().retrieveInput(verboseField);
    }

    /**
     * This hook method is a placeholder for making a command based
     * on the user input.
     */
    protected abstract UserCommand makeUserCommand(String userInput);

    /** This hook method executes a command. */
    void executeCommand(UserCommand command) throws Exception {
        command.execute();
    }
}
