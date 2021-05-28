package expressiontree.commands;

import expressiontree.tree.TreeContext;

import java.util.*;

/**
 * Implementation of the Factory Method pattern that dynamically
 * allocates the appropriate {@code UserCommand} object requested by
 * caller.  This variant of the pattern doesn't use inheritance, so it
 * plays the role of the ConcreteCreator in the Factory Method
 * pattern.
 */
public class UserCommandFactory {
    /** 
     * Holds the expression tree that is the target of the commands.
     */
    private TreeContext mTreeContext;
	
    /** 
     * This interface uses the Command pattern to create {@code
     * UserCommand} implementations at runtime.
     */
    @FunctionalInterface
    private interface UserCommandFactoryCommand {
        UserCommand execute(String param);
    }
	
    /**
     * Map used to validate mInput requests for {@code UserCommand}
     * implementations and dispatch the execute() method of the
     * requested user command.
     */
    private Map<String, UserCommandFactoryCommand> commandMap =
        new HashMap<>();

    /** 
     * Constructor. 
     */
    public UserCommandFactory(final TreeContext treeContext) {
    	// Initialize the TreeContext member.
        mTreeContext = treeContext;
   
    	// A "format" string maps to a command object that creates an
        // {@code FormatCommand} implementation.
        commandMap.put("format",
                       param -> new FormatCommand(treeContext, param));
        
    	// An "expr" string maps to a command object that creates an
        // {@code ExprCommand} implementation.
        commandMap.put("expr",
                       param -> new ExprCommand(treeContext, param));

    	// A "print" string maps to a command object that creates an
        // {@code PrintCommand} implementation.
        commandMap.put("print",
                       param -> new PrintCommand(treeContext, param));
		
    	// An "eval" string maps to a command object that creates an
        // {@code EvalCommand} implementation.
        commandMap.put("eval",
                       param -> new EvalCommand(treeContext, param));
        
    	// A "set" string maps to a command object that creates a
        // {@code SetCommand} implementation.
        commandMap.put("set",
                       param -> new SetCommand(treeContext, param));
		
    	// A "macro" string maps to a command object that creates a
        // {@code MacroCommand} implementation.
        commandMap.put("macro", param -> {
                // A MacroCommand contains a "in-order"
                // FormatCommand, the user mInput expression, and a
                // "post-order" EvalCommand.  It's used to
                // implement "Succinct Mode".
                return new MacroCommand(treeContext, Arrays
                                        .asList(new FormatCommand(treeContext,
                                                                  "in-order"),
                                                new ExprCommand(treeContext,
                                                                param),
                                                new EvalCommand(treeContext,
                                                                "post-order")));
            });
		
    	// A "quit" string maps to a command object that creates a
        // {@code QuitCommand} implementation.
        commandMap.put("quit",
                       param -> new QuitCommand(treeContext));
    }

    /** 
     * Create a new {@code UserCommand} object based on the caller's
     * designated {@code inputString}.
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
            return new QuitCommand(mTreeContext);
    }
}
