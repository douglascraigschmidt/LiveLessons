package expressiontree.nodes;

import expressiontree.visitors.Visitor;

/**
 * A node containing mLeft and mRight children. The meaning of this node
 * is mLeft - mRight.  It plays the role of a "Composite" in the
 * Composite pattern.
 */
public class CompositeSubtractNode 
       extends CompositeBinaryNode {
    /** 
     * Constructor.
     */
    public CompositeSubtractNode(ComponentNode left,
                                 ComponentNode right) {
        super(left, right);
    }

    /**
     * Return the type of the node.
     */
    public int getType() {
        return sSUBTRACTION;
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
