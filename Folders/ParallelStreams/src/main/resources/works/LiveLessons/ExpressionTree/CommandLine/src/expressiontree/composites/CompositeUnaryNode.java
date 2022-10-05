package expressiontree.composites;

import expressiontree.composites.ComponentNode;

/**
 * Defines a mRight child (but not a mLeft one) and thus is useful for
 * unary operations.  It plays the role of a "Composite" in the
 * Composite pattern.
 */
public class CompositeUnaryNode
       implements ComponentNode {
    /** 
     * Reference to the getRightChild child.
     */
    private ComponentNode mRight;
	
    /**
     * Constructor.
     */
    CompositeUnaryNode(ComponentNode right) {
        mRight = right;
    }
	
    /** 
     * Return the getRightChild child.
     */
    public ComponentNode getRightChild() {
        return mRight;
    }
}
