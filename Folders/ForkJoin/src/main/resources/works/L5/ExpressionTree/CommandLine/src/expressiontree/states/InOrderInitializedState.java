package expressiontree.states;

import expressiontree.tree.TreeContext;
import expressiontree.utils.TreeOperations;

/**
 * A state formatted in-order and containing an expression tree.
 */
class InOrderInitializedState 
    extends InOrderUninitializedState {
    /**
     * Print the current expression tree in the {@code context} using
     * the designed {@code traversalOrder}.
     */
    public void print(TreeContext context, String traversalOrder) {
        if (traversalOrder.equals(""))
            // Default to in-order if user doesn't explicitly request
            // a print order.
            traversalOrder = "in-order";

        TreeOperations.print(context.tree().iterator(traversalOrder));
    }
			  	
    /** 
     * Evaluate the yield of the current expression tree in the {@code
     * context} using the designed {@code traversalOrder}.
     */
    public void evaluate(TreeContext context, String traversalOrder) {
        if (traversalOrder.equals(""))
            // Default to post-order if user doesn't explicitly
            // request an eval order.
            traversalOrder = "post-order";
        else if (!traversalOrder.equals("post-order"))
            throw new IllegalArgumentException(traversalOrder 
                                               + " evaluation is not supported yet");

        TreeOperations.evaluate(context.tree().iterator(traversalOrder));
    }
}

