package expressiontree.states;

import expressiontree.tree.TreeOps;

/**
 * A state formated pre-order and containing an expression
 * tree.
 */
class PreOrderInitializedState 
      extends PreOrderUninitializedState {
    /** 
     * Print the current expression tree in the @a context
     * using the designed @a format.
     */
    public void print(TreeOps context, String format) {
        context.tree().print(format);
    }
		  	
    /** 
     * Evaluate the yield of the current expression tree in
     * the @a context using the designed @a format.
     */
    public void evaluate(TreeOps context, String format) {
        throw new IllegalArgumentException("PreOrderInitializedState.evaluate() not yet implemented");
    }
}
