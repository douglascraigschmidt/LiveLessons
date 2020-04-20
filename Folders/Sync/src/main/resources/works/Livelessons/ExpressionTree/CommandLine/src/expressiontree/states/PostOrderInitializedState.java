package expressiontree.states;

import expressiontree.tree.TreeContext;
import expressiontree.utils.TreeOperations;

/**
 * A state formated post-order and containing an expression tree.
 */
class PostOrderInitializedState
      extends PostOrderUninitializedState {
    /**
     * Print the current expression tree in the {@code context}
     * using the designed {@code traversalOrder}.
     */
    public void print(TreeContext context, String traversalOrder) {
        if (traversalOrder.equals(""))
            // Default to post-order if user doesn't explicitly request
            // a print order.
            traversalOrder = "post-order";

        TreeOperations.print(context.tree().iterator(traversalOrder));
    }

    /** 
     * Evaluate the yield of the current expression tree in the {@code
     * context} using the designed {@code traversalOrder}.
     */
    public void evaluate(TreeContext context, String traversalOrder) {
        throw new IllegalArgumentException("PostOrderInitializedState.evaluate() not yet implemented");
    }
}
		  
