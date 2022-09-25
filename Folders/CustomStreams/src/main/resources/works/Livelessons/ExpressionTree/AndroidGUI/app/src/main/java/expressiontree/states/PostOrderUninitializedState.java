package expressiontree.states;

import expressiontree.tree.TreeOps;

/**
 * A state formated post order without an expression tree. 
 */
class PostOrderUninitializedState 
      extends UninitializedState {
    /** 
     * Process the @a expression using a post-order
     * interpreter and update the state of the @a context to
     * the @a PostOrderInitializedState.
     */
    public void makeTree(TreeOps context, String expression) {
        throw new IllegalStateException("PostOrderUninitializedState.makeTree() not yet implemented");
    }
}

