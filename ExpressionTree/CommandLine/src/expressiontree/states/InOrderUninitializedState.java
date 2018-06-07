package expressiontree.states;

import expressiontree.tree.TreeContext;

/**
 * A state formated in-order without an expression tree.
 */
class InOrderUninitializedState
      extends UninitializedState {
    /** 
     * Process the {@code expression} using a in-order interpreter
     * and update the state of {@code mTreeOps} to the {@code
     * InOrderInitializedState}.
     */
    public void makeTree(TreeContext treeContext, String inputExpression) {
        // Use the Interpreter and Builder patterns to create
        // the expression tree designated by user mInput.
        treeContext.tree(treeContext.interpreter().interpret(inputExpression));

        // Transition to the InOrderInitializedState. 
        treeContext.state(new InOrderInitializedState());
    }
}

