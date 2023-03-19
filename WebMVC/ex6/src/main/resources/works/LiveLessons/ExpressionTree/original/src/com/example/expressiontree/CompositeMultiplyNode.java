package com.example.expressiontree;

/**
 * @class CompositeMultiplyNode
 *
 * @brief A node containing left and right children.  The meaning of
 *        this node is left * right.  It plays the role of a
 *        "Composite" in the Composite pattern.
 */
public class CompositeMultiplyNode extends CompositeBinaryNode {
    /** Ctor */
    public CompositeMultiplyNode(ComponentNode left,
                                 ComponentNode right) {
        super(left, right);
    }
  
    /** Return the printable character stored in the node. */
    public int item() {
	return '*';
    }

    /** 
     * Define the @a accept() operation used for the Visitor pattern
     * to accept the @a visitor. 
     */
    public void accept(Visitor visitor) {
	visitor.visit(this);
    }
}
