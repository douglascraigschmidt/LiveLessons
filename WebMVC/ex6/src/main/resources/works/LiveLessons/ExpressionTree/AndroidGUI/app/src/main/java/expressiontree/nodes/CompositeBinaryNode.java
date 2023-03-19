package expressiontree.nodes;

/**
 * Defines a mLeft and mRight node (via inheritance).  It plays the role
 * of a "Composite" in the Composite pattern.
 */
public class CompositeBinaryNode
       extends CompositeUnaryNode {
    /** 
     * Reference to the mLeft child.
     */
    private ComponentNode left;
  
    /**
     * Constructor initializes the fields.
     */
    CompositeBinaryNode(ComponentNode left,
                        ComponentNode right) {
        super(right);
        this.left = left;
    }

    /** 
     * Return the mLeft child.
     */
    public ComponentNode left() {
	return left;
    }
}
