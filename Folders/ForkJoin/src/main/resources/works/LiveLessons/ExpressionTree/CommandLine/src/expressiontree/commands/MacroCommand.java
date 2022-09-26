package expressiontree.commands;

import expressiontree.tree.TreeContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Execute a sequence of commands.  This plays the role of the
 * "ConcreteCommand" in the Command pattern.
 */
public class MacroCommand 
       extends UserCommand {
    /** 
     * Vector of commands that are executed as a macro. 
     */
    private List<UserCommand> mMacroCommands;

    /**
     * Constructor that provides the appropriate @a TreeContext and
     * sequence of commands.
     */
    MacroCommand(TreeContext context,
                 List<UserCommand> macroCommands) {
        super(context);
        mMacroCommands = macroCommands;
    }

    /**
     * Run the event loop.
     */
    public void execute() {
        mMacroCommands.forEach(UserCommand::execute);
    }

    /** 
     * Print the valid commands available to users. 
     */
    public void printValidCommands(boolean verboseField) {
        // No menu to print in succinct mode.
    }
}
