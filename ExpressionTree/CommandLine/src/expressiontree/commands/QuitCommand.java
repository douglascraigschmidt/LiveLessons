package expressiontree.commands;

import expressiontree.input.InputDispatcher;
import expressiontree.tree.TreeContext;

/**
 * Instructs the mInput dispatching loop to shut down.  This plays the
 * role of the "ConcreteCommand" in the Command pattern.
 */
public class QuitCommand 
       extends UserCommand {
    /** 
     * Constructor that provides the appropriate @a TreeContext.
     */
    QuitCommand(TreeContext context) {
        super(context);
    }

    /** 
     * Quit the mInput dispatching loop.
     */
    public void execute() {
        InputDispatcher.instance().endInputDispatching();
    }

    /** 
     * Print the valid commands available to users. 
     */
    public void printValidCommands(boolean verboseField) {
    }
}
