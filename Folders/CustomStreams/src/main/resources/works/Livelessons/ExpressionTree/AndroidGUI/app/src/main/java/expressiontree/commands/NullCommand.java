package expressiontree.commands;

import expressiontree.platspecs.Platform;
import expressiontree.tree.TreeOps;

/**
 * No-op command.  This plays the role of the "ConcreteCommand" in the
 * Command pattern.
 */
public class NullCommand 
       extends UserCommand {
    /**
     * Constructor that provides the appropriate {@code TreeOps} and
     * the requested format.
     */
    public NullCommand(TreeOps context) {
        super(context);
    }

    /** 
     * Set the desired format. 
     */
    public void execute() {
        // no-op.
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
                            "format",
                            "[post-order]");
        platform.outputMenu("1b.",
                            "set",
                            "[variable=value]");
        platform.outputMenu("2.",
                            "expr",
                            "[expression]");
        platform.outputMenu("3a.",
                            "eval",
                            "[post-order]");
        platform.outputMenu("3b.",
                            "print",
                            "[in-order | pre-order | post-order| level-order]");
        platform.outputMenu("0c.",
                            "quit",
                            "");
        platform.outputMenu("",
                            "",
                            "");
    }
}
