package expressiontree.nodes;

/**
 * Defines a mLeft and mRight node (via inheritance).  It plays the role
 * of a "Composite" in the Composite pattern.
 */
public class CompositeBinaryNode
       extends CompositeUnaryNode {
    /** 
     * Reference to the getLeftChild child.
     */
    private ComponentNode mLeft;
  
    /**
     * Constructor initializes the fields.
     */
    CompositeBinaryNode(ComponentNode left,
                        ComponentNode right) {
        super(right);
        mLeft = left;
    }

    /** 
     * Return the getLeftChild child.
     */
    public ComponentNode getLeftChild() {
	return mLeft;
    }
}
