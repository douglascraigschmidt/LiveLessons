package com.example.expressiontree;

import java.util.HashMap;
import java.util.Vector;

/**
 * @class UserCommandFactory
 *
 * @brief Implementation of the Factory Method pattern that
 *        dynamically allocates the appropriate @a UserCommand object
 *        requested by caller.  This variant of the pattern doesn't
 *        use inheritance, so it plays the role of the ConcreteCreator
 *        in the Factory Method pattern.
 */
public class UserCommandFactory {
    /** 
     * Holds the expression tree that is the target of the commands.
     */
    private TreeOps treeOps;
	
    /** 
     * This interface uses the Command pattern to create @a
     * UserCommand implementations at runtime.
     */
    private static interface IUserCommandFactoryCommand {
        public UserCommand execute(String param);
    }
	
    /**
     * Map used to validate input requests for @a UserCommand
     * implementations and dispatch the execute() method of the
     * requested user command.
     */
    private HashMap<String, IUserCommandFactoryCommand> commandMap =
        new HashMap<String, IUserCommandFactoryCommand>();

    /** Ctor */
    UserCommandFactory(final TreeOps treeOps) {   	
    	/** Initialize the TreeOps member. */
        this.treeOps = treeOps;
   
    	/** 
         * A "format" string maps to a command object that creates
         * an @a FormatCommand implementation.
         */
        commandMap.put("format", new IUserCommandFactoryCommand() {
                public UserCommand execute(String param) {
                    return new FormatCommand(treeOps, param);
                }
            });
        
    	/** 
         * An "expr" string maps to a command object that creates
         * an @a ExprCommand implementation.
         */
        commandMap.put("expr", new IUserCommandFactoryCommand() { 
                public UserCommand execute(String param) {
                    return new ExprCommand(treeOps, param);
                }
            });
        
    	/** 
         * A "print" string maps to a command object that creates
         * an @a PrintCommand implementation.
         */
        commandMap.put("print", new IUserCommandFactoryCommand() {
                public UserCommand execute(String param) {
                    return new PrintCommand(treeOps, param);
                }
            });
		
    	/** 
         * An "eval" string maps to a command object that creates
         * an @a EvalCommand implementation.
         */
        commandMap.put("eval", new IUserCommandFactoryCommand() {
                public UserCommand execute(String param) {
                    return new EvalCommand(treeOps, param);
                }
            });
        
    	/** 
         * A "set" string maps to a command object that creates a @a
         * SetCommand implementation.
         */
        commandMap.put("set", new IUserCommandFactoryCommand() {
                public UserCommand execute(String param) {
                    return new SetCommand(treeOps, param);
                }
            });
		
    	/** 
         * A "macro" string maps to a command object that creates a @a
         * MacroCommand implementation.
         */
        commandMap.put("macro", new IUserCommandFactoryCommand() {
                public UserCommand execute(String param) {
                    Vector <UserCommand> macroCommands =
                        new Vector <UserCommand>();

                    /** 
                     * A MacroCommand contains a "in-order"
                     * FormatCommant, the user input expression, and a
                     * "post-order" EvalCommand.  It's used to
                     * implement "Succinct Mode".
                     */
                    macroCommands.add(new FormatCommand(treeOps,
                                                        "in-order"));
                    macroCommands.add(new ExprCommand(treeOps,
                                                      param));
                    macroCommands.add(new EvalCommand(treeOps,
                                                      "post-order"));

                    return new MacroCommand(treeOps,
                                            macroCommands);
                }
            });
		
    	/** 
         * A "quit" string maps to a command object that creates a @a
         * QuitCommand implementation.
         */
        commandMap.put("quit", new IUserCommandFactoryCommand() {
                public UserCommand execute(String param) {
                    return new QuitCommand(treeOps);
                }
            });
    }

    /** 
     * Create a new @a UserCommand object based on the caller's
     * designated @a inputString.
     */
    public UserCommand makeUserCommand(String inputString) {
        String parameters = "";
        String commandRequest = inputString;

        int spacepos = inputString.indexOf(' ');
        if (spacepos >= 0) {
            parameters = inputString.substring(spacepos + 1);
            commandRequest = inputString.substring(0, 
                                                   spacepos);
        } else /** There's only a command, but no parameters. */
            ; 

        /** Try to find the pre-allocated factory command. */
        IUserCommandFactoryCommand command = 
            commandMap.get(commandRequest);

        if(command != null)
            /** If we find it then execute it. */
            return command.execute(parameters);
        else
            /** 
             * Otherwise, the user gave an unknown request, so we'll
             * quit.
             */
            return new QuitCommand(treeOps);
    }
}
