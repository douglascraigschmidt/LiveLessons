package expressiontree.nodes;

import expressiontree.visitors.Visitor;

/**
 * Defines a terminal node of type paren (either getLeftChild '(' or getRightChild
 * ')').  It plays the role of the "Leaf" in the Composite pattern.
 */
public class ParenNode 
       implements ComponentNode {
    /** 
     * Char value associated with the paren, i.e., '(' or ')'.
      */
    private char mParen;
  
    /**
       Constructor. 
    */
    public ParenNode(char paren) {
        mParen = paren;
    }

    /**
     * Return the type of the node.
     */
    public int getType() {
        if (mParen == '(')
            return sLPAREN;
        else
            return sRPAREN;
    }

    /**
     * Return the paren stored in the node.
     */
    public int getItem() {
        return mParen;
    }

    /**
     * Define the {@code accept()} operation used for the Visitor
     * pattern.
     */
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
