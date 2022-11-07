package expressiontree.commands;

import expressiontree.tree.TreeOps;

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
    private List<UserCommand> macroCommands =
        new ArrayList<>();

    /** 
     * Expression mInput by the user.
     */
    private String expr;

    /**
     * Constructor that provides the appropriate @a TreeOps and
     * sequence of commands.
     */
    MacroCommand(TreeOps context,
                 List<UserCommand> macroCommands) {
        super(context);
        this.macroCommands = macroCommands;
    }

    /**
     * Run the event loop.
     */
    public void execute() {
        macroCommands.forEach(UserCommand::execute);
    }

    /** 
     * Print the valid commands available to users. 
     */
    public void printValidCommands(boolean verboseField) {
        // No menu to print in succinct mode.
    }
}
