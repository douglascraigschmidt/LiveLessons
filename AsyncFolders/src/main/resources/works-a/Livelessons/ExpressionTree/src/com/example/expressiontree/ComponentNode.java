package com.example.expressiontree;

/**
 * @class ComponentNode
 *
 * @brief An abstract base class defines a simple abstract
 *        implementation of an expression tree node.  This class plays
 *        the role of the "Component" in the Composite pattern.  The
 *        methods in this class are not defined as pure virtual so
 *        that subclasses in the Composite pattern don't have to
 *        implement methods they don't care about.
 */
public abstract class ComponentNode {
    /** This method is a no-op in this abstract base class. */
    public int item() {
        throw new UnsupportedOperationException
            ("ComponentNode::item() called improperly");
    }

    /** Return the right child (returns 0 if called directly). */
    public ComponentNode right() {
        return null;
    }
  
    /** Return the left child (returns 0 if called directly). */
    public ComponentNode left() {
        return null;
    }
  
    /**
     * Accept a visitor to perform some action on the node's item
     * completely arbitrary visitor template (throws an exception if
     * called directly).
     */
    void accept (Visitor visitor) {
        throw new UnsupportedOperationException
            ("ComponentNode::accept() called improperly");
    }
}
