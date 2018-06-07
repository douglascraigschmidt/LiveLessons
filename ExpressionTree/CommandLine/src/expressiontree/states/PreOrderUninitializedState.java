package expressiontree.states;

import expressiontree.tree.TreeContext;

/**
 * A state formated pre-order without an expression tree.
 */
class PreOrderUninitializedState
      extends UninitializedState {
    /**
     * Process the {@code expression} using a pre-order interpreter
     * and update the state of the {@code context} to the {@code
     * PreOrderUninitializedState}.
     */
    public void makeTree(TreeContext context, String format) {
        throw new IllegalStateException("PreOrderUninitializedState.makeTree() not yet implemented");
    }
}
	  	  
