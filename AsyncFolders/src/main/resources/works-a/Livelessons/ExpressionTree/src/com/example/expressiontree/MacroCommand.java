package com.example.expressiontree;

import java.util.Vector;

/**
 * @class MacroCommand
 *
 * @brief Execute a sequence of commands.  This plays the role of the
 *        "ConcreteCommand" in the Command pattern.
 */
public class MacroCommand extends UserCommand {
    /** Vector of commands that are executed as a macro. */
    private Vector<UserCommand> macroCommands =
        new Vector<UserCommand>();

    /** Expression input by the user. */
    private String expr;

    /** 
     * Constructor that provides the appropriate @a TreeOps and
     * sequence of commands.
     */
    MacroCommand(TreeOps context,
                 Vector<UserCommand> macroCommands) {
        super.treeOps = context;
        this.macroCommands = macroCommands;
    }

    /** Quit the event loop. */
    public void execute() throws Exception {
        for(UserCommand c : macroCommands)
            c.execute();
    }

    /** Print the valid commands available to users. */
    public void printValidCommands(boolean verboseField) {
        /** No menu to print in succinct mode. */
    }
}
