package expressiontree.commands;

import expressiontree.tree.TreeContext;

/**
 * Plays the role of the "Command" in the Command pattern to define an
 * API for "ConcreteCommand" implementations that perform an operation
 * on the expression tree when it's executed.
 */
public abstract class UserCommand {
    /** 
     * Holds the expression tree that is the target of the
     * commands. 
     */
    TreeContext mTreeContext;

    /**
     * Constructor.
     *
     * @param treeContext TreeContext instance
     */
    UserCommand(TreeContext treeContext) {
        mTreeContext = treeContext;
    }

    /** 
     * Runs the command. 
     */
    public abstract void execute();

    /** 
     * Print the valid commands available to users. 
     */
    public abstract void printValidCommands(boolean verboseField);
}
