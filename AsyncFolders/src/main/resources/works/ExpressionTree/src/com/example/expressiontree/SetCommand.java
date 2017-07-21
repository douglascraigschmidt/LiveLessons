package com.example.expressiontree;

/**
 * @class SetCommand
 *
 * @brief Sets a variable into the SymbolTable stored inside of
 *        TreeOps.  This plays the role of the "ConcreteCommand" in
 *        the Command pattern.
 */
public class SetCommand extends UserCommand {
    /** Format to use for the evaluation. */
    private String keyValuePair;

    /** 
     * Constructor that provides the appropriate @a TreeOps and
     * the requested format.
     */
    SetCommand(TreeOps context, 
               String keyValuePair) {
        super.treeOps = context;
        this.keyValuePair = keyValuePair;
    }
	
    /** Evaluate the expression tree. */
    public void execute() throws Exception {
        treeOps.set (keyValuePair);
    }
	
    public void printValidCommands(boolean verboseField) {
        Platform platform = Platform.instance();
    	platform.disableAll(verboseField);
    	platform.outputLine("");

        /** 
         * If we've never had a format command called successfully,
         * then we have a different set of valid commands.
         */
        char abc[] = { 'a','b','c' };
        int step = 1;
        int substep = 0;
        String formatMessagePartOne="format";
        String formatMessagePartTwo="[in-order]";
		
        platform.outputMenu("", "", "");

        if(!treeOps.formatted()) {
            platform.outputMenu(step++ +".",
                                formatMessagePartOne,
                                formatMessagePartTwo);
            platform.outputMenu(step++ +".",
                                "expr",
                                "[expression]");
            platform.outputMenu(step+ "a.",
                                "eval",
                                "[post-order]");
            platform.outputMenu(step++ + "b.",
                                "print",
                                "[in-order | pre-order | post-order| level-order]");
            step = 0;
        }

        if(treeOps.formatted()) 
            platform.outputMenu(step +".",
                                formatMessagePartOne,
                                formatMessagePartTwo);
		
        platform.outputMenu(step + abc[substep++] + "",
                            "set",
                            "[variable=value]");
			
        platform.outputMenu(step +".", 
                            formatMessagePartOne,
                            formatMessagePartTwo);
        platform.outputMenu(step +".",
                            formatMessagePartOne,
                            formatMessagePartTwo);
		
        platform.outputMenu(step + abc[substep++]+ ".",
                            "quit",
                            "");
        platform.outputMenu("",
                            "",
                            "");
    }
}
