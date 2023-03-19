package expressiontree.commands;

import expressiontree.platspecs.Platform;
import expressiontree.tree.TreeOps;

/**
 * Evaluates the expression tree in the desired mFormat, e.g.,
 * "in-order," "pre-order," "post-order", or "level-order".  This
 * plays the role of the "ConcreteCommand" in the Command pattern.
 */
public class EvalCommand 
       extends UserCommand {
    /** 
     * Format to use for the evaluation. 
     */
    private String mFormat;

    /** 
     * Constructor that provides the appropriate TreeOps and the
     * requested mFormat.
     */
    EvalCommand(TreeOps context,
                String format) {
        super(context);
        mFormat = format;
    }

    /**
     * Evaluate the expression tree. 
     */
    public void execute() {
        super.mTreeOps.evaluate(mFormat);
    }

    /** 
     * Creates a menu for the user. 
     */
    public void printValidCommands(boolean verboseField) {
        Platform platform = Platform.instance();
    	platform.disableAll(verboseField);
    	platform.outputMenu("", "", "");
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
