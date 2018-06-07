package expressiontree.states;

import expressiontree.tree.TreeContext;

/**
 * A state formated level-order without an expression tree.
 */
class LevelOrderUninitializedState 
      extends UninitializedState {
    /**
     * Process the {@code expression} using a level-order interpreter
     * and update the state of the {@code context} to the {@code
     * LevelOrderInitializedState}.
     */
    public void makeTree(TreeContext context, String expression) {
        throw new IllegalStateException("LevelOrderUninitializedState.makeTree() not yet implemented");
    }
}
		  
