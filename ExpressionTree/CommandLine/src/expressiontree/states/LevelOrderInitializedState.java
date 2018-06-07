package expressiontree.states;

import expressiontree.tree.TreeContext;
import expressiontree.utils.TreeOperations;

/**
 * A state formated level-order and containing an expression tree.
 */
class LevelOrderInitializedState
      extends LevelOrderUninitializedState {
    /**
     * Print the current expression tree in the {@code context} using
     * the designed {@code format}.
     */
    public void print(TreeContext context, String traversalOrder) {
        if (traversalOrder.equals(""))
            // Default to level-order if user doesn't explicitly request
            // a print order.
            traversalOrder = "level-order";

        TreeOperations.print(context.tree().makeIterator(traversalOrder));
    }
			  	
    /** 
     * Evaluate the yield of the current expression tree in the {@code
     * context} using the designed {@code traversalOrder}.
     */
    public void evaluate(TreeContext context, String traversalOrder) {
        throw new IllegalArgumentException("LevelOrderInitializedState.evaluate() not yet implemented");
    }
}

