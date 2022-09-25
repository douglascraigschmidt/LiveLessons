package com.example.expressiontree;

/**
 * @class CompositeBinaryNode
 *
 * @brief Defines a left and right node (via inheritance).  It plays
 *        the role of a "Composite" in the Composite pattern.
 */
public class CompositeBinaryNode extends CompositeUnaryNode {
    /** Reference to the left child. */
    private ComponentNode left;
  
    /** Ctor */
    public CompositeBinaryNode(ComponentNode left,
                               ComponentNode right) {
        super(right);
        this.left = left;
    }

    /** Return the left child. */
    public ComponentNode left() {
	return left;
    }
}
