package expressiontree.commands;

import expressiontree.platspecs.Platform;
import expressiontree.tree.TreeOps;

/**
 * Prints the expression tree in the desired format, e.g., "in-order,"
 * "pre-order," "post-order", or "level-order".  This plays the role
 * of the "ConcreteCommand" in the Command pattern.
 */
public class PrintCommand
       extends UserCommand {
    /** 
     * Format to print out the tree.
     */
    private String format;

    /**
     * Constructor that provides the appropriate @a TreeOps and the
     * requested format.
     */
    PrintCommand(TreeOps context,
                 String printFormat) {
        super(context);
        format = printFormat;
    }

    /**
     * Print the expression tree. 
     */
    public void execute() {
        super.mTreeOps.print(format);
    }

    /** 
     * Print the valid commands available to users. 
     */
    public void printValidCommands(boolean verboseField) {
        Platform platform = Platform.instance();
    	platform.disableAll(verboseField);
        platform.outputMenu("",
                            "",
                            "");
        platform.outputMenu("1a.",
                            "eval",
                            "[post-order]");
        platform.outputMenu("1b.",
                            "print",
                            "[in-order | pre-order | post-order| level-order]");
        platform.outputMenu("0a.",
                            "format",
                            "[in-order]");
        platform.outputMenu("0b.",
                            "set",
                            "[variable = value]");
        platform.outputMenu("0c.",
                            "quit",
                            "");
        platform.outputMenu("",
                            "",
                            "");
    }
}
