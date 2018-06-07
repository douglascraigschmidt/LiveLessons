package expressiontree.states;

import expressiontree.tree.TreeContext;
import expressiontree.utils.TreeOperations;

/**
 * A state formated pre-order and containing an expression tree.
 */
class PreOrderInitializedState 
      extends PreOrderUninitializedState {
    /**
     * Print the current expression tree in the {@code context}
     * using the designed {@code traversalOrder}.
     */
    public void print(TreeContext context, String traversalOrder) {
        if (traversalOrder.equals(""))
            // Default to pre-order if user doesn't explicitly request
            // a print order.
            traversalOrder = "pre-order";

        TreeOperations.print(context.tree().makeIterator(traversalOrder));
    }
		  	
    /** 
     * Evaluate the yield of the current expression tree in the {@code
     * context} using the designed {@code traversalOrder}.
     */
    public void evaluate(TreeContext context, String traversalOrder) {
        throw new IllegalArgumentException("PreOrderInitializedState.evaluate() not yet implemented");
    }
}
