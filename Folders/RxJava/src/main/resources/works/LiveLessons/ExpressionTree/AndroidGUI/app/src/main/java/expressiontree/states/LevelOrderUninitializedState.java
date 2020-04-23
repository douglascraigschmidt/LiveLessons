package expressiontree.states;

import expressiontree.tree.TreeOps;

/**
 * A state formated level order without an expression tree. 
 */
class LevelOrderUninitializedState 
      extends UninitializedState {
    /**
     * Process the @a expression using a level-order
     * interpreter and update the state of the @a context to
     * the @a LevelOrderInitializedState.
     */
    public void makeTree(TreeOps context, String expression) {
        throw new IllegalStateException("LevelOrderUninitializedState.makeTree() not yet implemented");
    }
}
		  
