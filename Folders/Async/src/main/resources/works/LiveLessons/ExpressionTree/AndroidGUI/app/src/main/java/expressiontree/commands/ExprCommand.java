package expressiontree.commands;

import expressiontree.platspecs.Platform;
import expressiontree.tree.TreeOps;

/**
 * Set the desired expression, e.g., "1+2*3".  This plays the role of
 * the "ConcreteCommand" in the Command pattern.
 */
public class ExprCommand 
        extends UserCommand {
    /** 
     * Requested expression. 
     */
    private String mExpr;

    /** 
     * Constructor that provides the appropriate @a TreeOps and the
     * requested expression.
     */
    ExprCommand(TreeOps context, String newexpr) {
        super(context);
        mExpr = newexpr;
    }

    /** 
     * Create the desired expression tree. 
     */
    public void execute() {
        super.mTreeOps.makeTree(mExpr);
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
