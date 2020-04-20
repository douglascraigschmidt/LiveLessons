package expressiontree.nodes;

import expressiontree.visitors.Visitor;

/**
 * This interface defines a simple default implementation of
 * an expression tree node.  This interface plays the role of the
 * "Component" in the Composite pattern.  The methods in this interface
 * are provided with default implementations so that subclasses in the
 * Composite pattern needn't implement methods they don't care about.
 */
public interface ComponentNode {
    /** 
     * This method is a no-op in this abstract base class. 
     */
    default int item() {
        throw new UnsupportedOperationException
            ("ComponentNode::item() called improperly");
    }

    /** 
     * Return the mRight child (returns 0 if called directly).
     */
    default ComponentNode right() {
        return null;
    }
  
    /** 
     * Return the mLeft child (returns 0 if called directly).
     */
    default ComponentNode left() {
        return null;
    }
  
    /**
     * Accept a visitor to perform some action on the node's item
     * completely arbitrary visitor template (throws an exception if
     * called directly).
     */
    default void accept (Visitor visitor) {
        throw new UnsupportedOperationException
            ("ComponentNode::accept() called improperly");
    }
}
