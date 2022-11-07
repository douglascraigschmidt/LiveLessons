package expressiontree.states;

import expressiontree.tree.TreeOps;

class InOrderUninitializedState
      extends UninitializedState {
    /** 
     * Process the @a expression using a in-order interpreter
     * and update the state of @a mTreeOps to the @a
     * InOrderInitializedState.
     */
    public void makeTree(TreeOps treeOps, String inputExpression) {
        // Use the Interpreter and Builder patterns to create
        // the expression tree designated by user mInput.
        treeOps.tree(treeOps.interpreter().interpret(inputExpression));

        // Transition to the InOrderInitializedState. 
        treeOps.state(new InOrderInitializedState());
    }
}

