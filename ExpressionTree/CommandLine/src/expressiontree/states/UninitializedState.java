package expressiontree.states;

import expressiontree.tree.TreeContext;

/**
 * A state without an initialized context or given format.
 */
public class UninitializedState 
       implements State {
    /** 
     * Sets the input format for the state. 
     */
    public void format(TreeContext context, String newFormat) {
        if (newFormat.equals(""))
            // Default to in-order if user doesn't explicitly
            // request a format order.
            newFormat = "in-order";
        else if (!(newFormat.equals("in-order")
                   || newFormat.equals("post-order")))
            throw new IllegalArgumentException(newFormat 
                                               + " format is not supported yet");

        // Transition to the designated UninitializedState. 
        context.state(mUninitializedStateFactory.makeUninitializedState(newFormat));
    }

    /** 
     * A state factory responsible for building uninitialized states.
     */        
    private static UninitializedStateFactory mUninitializedStateFactory =
        new UninitializedStateFactory();
}
