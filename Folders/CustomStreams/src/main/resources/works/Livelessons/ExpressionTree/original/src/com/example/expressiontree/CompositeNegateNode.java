package com.example.expressiontree;

/**
 * @class CompositeNegateNode
 *
 * @brief A node containing only a right child.  The meaning of this
 *        node is -right (e.g., -5, -7, etc).  It plays the role of a
 *        "Composite" in the Composite pattern.
 */
public class CompositeNegateNode extends CompositeUnaryNode {
    /** Ctor */
    public CompositeNegateNode(ComponentNode right) {
        super(right);
    }

    /** Return the printable character stored in the node. */
    public int item() {
        return '-';
    }

    /** 
     * Define the @a accept() operation used for the Visitor pattern
     * to accept the @a visitor.
     */
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
