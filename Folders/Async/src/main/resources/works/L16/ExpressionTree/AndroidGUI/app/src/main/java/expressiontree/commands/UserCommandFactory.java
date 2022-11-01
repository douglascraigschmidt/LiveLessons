package expressiontree.commands;

import expressiontree.tree.TreeOps;

import java.util.*;

/**
 * Implementation of the Factory Method pattern that dynamically
 * allocates the appropriate @a UserCommand object requested by
 * caller.  This variant of the pattern doesn't use inheritance, so it
 * plays the role of the ConcreteCreator in the Factory Method
 * pattern.
 */
public class UserCommandFactory {
    /** 
     * Holds the expression tree that is the target of the commands.
     */
    private TreeOps mTreeOps;
	
    /** 
     * This interface uses the Command pattern to create @a
     * UserCommand implementations at runtime.
     */
    @FunctionalInterface
    private interface UserCommandFactoryCommand {
        public UserCommand execute(String param);
    }
	
    /**
     * Map used to validate mInput requests for @a UserCommand
     * implementations and dispatch the execute() method of the
     * requested user command.
     */
    private HashMap<String, UserCommandFactoryCommand> commandMap =
        new HashMap<>();

    /** 
     * Constructor. 
     */
    public UserCommandFactory(final TreeOps treeOps) {
    	// Initialize the TreeOps member. 
        this.mTreeOps = treeOps;
   
    	// A "format" string maps to a command object that creates
        // an @a FormatCommand implementation.
        commandMap.put("format",
                       param -> new FormatCommand(treeOps, param));
        
    	// An "expr" string maps to a command object that creates
        // an @a ExprCommand implementation.
        commandMap.put("expr",
                       param -> new ExprCommand(treeOps, param));
        
    	// A "print" string maps to a command object that creates
        // an @a PrintCommand implementation.
        commandMap.put("print",
                       param -> new PrintCommand(treeOps, param));
		
    	// An "eval" string maps to a command object that creates
        // an @a EvalCommand implementation.
        commandMap.put("eval",
                       param -> new EvalCommand(treeOps, param));
        
    	// A "set" string maps to a command object that creates a @a
        // SetCommand implementation.
        commandMap.put("set",
                       param -> new SetCommand(treeOps, param));
		
    	// A "macro" string maps to a command object that creates a @a
        // MacroCommand implementation.
        commandMap.put("macro", param -> {
            // A MacroCommand contains a "in-order"
            // FormatCommand, the user mInput expression, and a
            // "post-order" EvalCommand.  It's used to
            // implement "Succinct Mode".
            return new MacroCommand(treeOps, Arrays
                    .asList(new FormatCommand(treeOps,
                                     "in-order"),
                            new ExprCommand(treeOps,
                                            param),
                            new EvalCommand(treeOps,
                                    "post-order")));
        });
		
    	// A "quit" string maps to a command object that creates a @a
        // QuitCommand implementation.
        commandMap.put("quit",
                       param -> new QuitCommand(treeOps));
    }

    /** 
     * Create a new @a UserCommand object based on the caller's
     * designated @a inputString.
     */
    public UserCommand makeUserCommand(String inputString) {
        String parameters = "";
        String commandRequest = inputString;

        int spacePos = inputString.indexOf(' ');
        if (spacePos >= 0) {
            parameters = inputString.substring(spacePos + 1);
            commandRequest = inputString.substring(0, 
                                                   spacePos);
        } else // There's only a command, but no parameters.
            ; 

        // Try to find the pre-allocated factory command. 
        UserCommandFactoryCommand command = 
            commandMap.get(commandRequest);

        if(command != null)
            // If we find it then execute it. 
            return command.execute(parameters);
        else
            // Otherwise, the user gave an unknown request, so we'll
            // quit.
            return new QuitCommand(mTreeOps);
    }
}
