package expressiontree.nodes;

import expressiontree.visitors.Visitor;

/**
 * Defines a terminal node of type integer.  It plays the role of the
 * "Leaf" in the Composite pattern.
 */
public class LeafNode 
       implements ComponentNode {
    /** 
     * Integer value associated with the operand. 
      */
    private int mItem;
  
    /**
       Constructor. 
    */
    public LeafNode(int item) {
        mItem = item;
    }

    /**
     * Constructor.
     */
    public LeafNode(String item) {
        mItem = Integer.parseInt(item);
    }

    /**
     * Return the mItem stored in the node.
     */
    public int item() {
        return mItem;
    }

    /**
     * Define the {@code accept()} operation used for the Visitor
     * pattern.
     */
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
