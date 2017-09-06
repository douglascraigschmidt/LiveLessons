package com.example.expressiontree;

/**
 * @class CompositeUnaryNode
 *
 * @brief Defines a right child (but not a left one) and thus is
 *        useful for unary operations.  It plays the role of a
 *        "Composite" in the Composite pattern.
 */
public class CompositeUnaryNode extends ComponentNode {
    /** Reference to the right child. */
    private ComponentNode right;
	
    /** Ctor */
    public CompositeUnaryNode(ComponentNode right) {
        this.right = right;
 
    }
	
    /** Return the right child. */
    public ComponentNode right() {
        return this.right;
    }
}
