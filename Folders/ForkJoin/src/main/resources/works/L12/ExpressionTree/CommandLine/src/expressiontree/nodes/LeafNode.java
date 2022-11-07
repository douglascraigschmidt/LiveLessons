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
     * Return the type of the node.
     */
    public int getType() {
        return sNUMBER;
    }

    /**
     * Return the getItem stored in the node.
     */
    public int getItem() {
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
