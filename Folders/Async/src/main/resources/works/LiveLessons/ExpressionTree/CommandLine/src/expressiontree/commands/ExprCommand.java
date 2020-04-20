package expressiontree.commands;

import expressiontree.platspecs.Platform;
import expressiontree.tree.TreeContext;

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
     * Constructor that provides the appropriate @a TreeContext and the
     * requested expression.
     */
    ExprCommand(TreeContext context, String newexpr) {
        super(context);
        mExpr = newexpr;
    }

    /** 
     * Create the desired expression tree. 
     */
    public void execute() {
        super.mTreeContext.makeTree(mExpr);
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
                            "[in-order | post-order]");
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
