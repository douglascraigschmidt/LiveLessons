package expressiontree.states;

import expressiontree.tree.TreeOps;

/**
 * A state formated post order and containing an expression
 * tree.
 */
class PostOrderInitializedState
      extends PostOrderUninitializedState {
    /**
     * Print the current expression tree in the @a context
     * using the designed @a format.
     */
    public void print(TreeOps context, String format) {
        context.tree().print(format);
    }

    /** 
     * Evaluate the yield of the current expression tree
     * in the @a context using the designed @a format.
     */
    public void evaluate(TreeOps context, String param) {
        throw new IllegalArgumentException("PostOrderInitializedState.evaluate() not yet implemented");
    }
}
		  
