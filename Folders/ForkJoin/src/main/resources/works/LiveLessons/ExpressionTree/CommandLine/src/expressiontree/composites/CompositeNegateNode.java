package expressiontree.composites;

import expressiontree.visitors.Visitor;

/**
 * A node containing only a mRight child.  The meaning of this node is
 * -mRight (e.g., -5, -7, etc).  It plays the role of a "Composite" in
 * the Composite pattern.
 */
public class CompositeNegateNode
       extends CompositeUnaryNode {
    /**
     * Constructor.
     */
    public CompositeNegateNode(ComponentNode right) {
        super(right);
    }

    /**
     * Return the type of the node.
     */
    public int getType() {
        return sNEGATION;
    }

    /**
     * Return the printable character stored in the node. 
     */
    public int getItem() {
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
