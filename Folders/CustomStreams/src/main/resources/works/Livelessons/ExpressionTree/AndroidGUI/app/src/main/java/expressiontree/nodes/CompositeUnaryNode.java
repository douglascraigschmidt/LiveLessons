package expressiontree.nodes;

import expressiontree.nodes.ComponentNode;

/**
 * Defines a mRight child (but not a mLeft one) and thus is useful for
 * unary operations.  It plays the role of a "Composite" in the
 * Composite pattern.
 */
public class CompositeUnaryNode
       implements ComponentNode {
    /** 
     * Reference to the mRight child.
     */
    private ComponentNode mRight;
	
    /**
     * Constructor.
     */
    CompositeUnaryNode(ComponentNode right) {
        mRight = right;
    }
	
    /** 
     * Return the mRight child.
     */
    public ComponentNode right() {
        return mRight;
    }
}
