package expressiontree.states;

import expressiontree.tree.TreeContext;

/**
 * Implementation of the State pattern that is used to define the
 * various states that affect how users operations are processed.
 * Plays the role of the "State" in the State pattern that is
 * used as the basis for the subclasses that actually define the
 * various states.
 */
public interface State {
    /**
     * Throws an exception if called in the wrong state.
     */
    default void format(TreeContext context, String newFormat) {
        throw new IllegalStateException("State.format() called in invalid state");
    }
	  
    /** 
     * Throws an exception if called in the wrong state.
     */
    default void makeTree(TreeContext context,
                         String expression) {
        throw new IllegalStateException("State.makeTree() called in invalid state");
    }
	  
    /** 
     * Throws an exception if called in the wrong state.
     */
    default void print(TreeContext context,
                      String format) {
        throw new IllegalStateException("State.print() called in invalid state");
    }
	  
    /** 
     * Throws an exception if called in the wrong state.
     */
    default void evaluate(TreeContext context,
                         String format) {
        throw new IllegalStateException("State.evaluate() called in invalid state");
    }
}
