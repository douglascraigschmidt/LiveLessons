package expressiontree.nodes;

import expressiontree.visitors.Visitor;

/**
 * This interface defines a simple default implementation of
 * an expression tree node.  This interface plays the role of the
 * "Component" in the Composite pattern.  The methods in this interface
 * are provided with default implementations so that subclasses in the
 * Composite pattern needn't implement methods they don't care about.
 */
public interface ComponentNode {
    /*
     * The following constants uniquely identify the type of
     * each terminal symbol.
     */
    public final static int sMULTIPLICATION = 0;
    public final static int sDIVISION = 1;
    public final static int sADDITION = 2;
    public final static int sSUBTRACTION = 3;
    public final static int sNEGATION = 4;
    public final static int sLPAREN = 5;
    public final static int sRPAREN = 6;
    public final static int sID = 7;
    public final static int sNUMBER = 8;
    public final static int sDELIMITER = 9;

    /**
     *
     */
    public final static int mTopOfStackPrecedence[] = {
        12, 11, 7, 6, 10, 2, 3, 15, 14, 1
    };

    /**
     *
     */
    public final static int mCurrentTokenPrecedence[] = {
        9, 8, 5, 4, 13, 18, 2, 17, 16, 1
    };

    /**
     * This method is a no-op in this abstract base class.
     */
    default int getType() {
        throw new UnsupportedOperationException
                ("ComponentNode::type() called improperly");
    }

    /** 
     * This method is a no-op in this abstract base class. 
     */
    default int getItem() {
        throw new UnsupportedOperationException
            ("ComponentNode::getItem() called improperly");
    }

    /** 
     * Return the mRight child (returns 0 if called directly).
     */
    default ComponentNode getRightChild() {
        return null;
    }
  
    /** 
     * Return the mLeft child (returns 0 if called directly).
     */
    default ComponentNode getLeftChild() {
        return null;
    }
  
    /**
     * Accept a visitor to perform some action on the node's getItem
     * completely arbitrary visitor template (throws an exception if
     * called directly).
     */
    default void accept (Visitor visitor) {
        throw new UnsupportedOperationException
            ("ComponentNode::accept() called improperly");
    }
}
