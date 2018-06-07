package expressiontree.states;

import expressiontree.tree.TreeContext;

/**
 * A state formated post-order without an expression tree.
 */
class PostOrderUninitializedState 
      extends UninitializedState {
    /** 
     * Process the {@code inputExpression} using a post-order interpreter
     * and update the state of the {@code context} to the {@code
     * PostOrderInitializedState}.
     */
    public void makeTree(TreeContext treeContext, String inputExpression) {
        // Use the Interpreter and Builder patterns to create
        // the expression tree designated by user mInput.
        treeContext.tree(treeContext.interpreter().interpret(inputExpression));

        // Transition to the InOrderInitializedState. 
        treeContext.state(new InOrderInitializedState());
    }
}

