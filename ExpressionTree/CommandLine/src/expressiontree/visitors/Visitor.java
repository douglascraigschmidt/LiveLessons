package expressiontree.visitors;

import expressiontree.nodes.*;

/**
 * Abstract base class for all visitors to all classes that derive
 * from @a ComponentNode.  This class plays the role of the "Visitor"
 * in the Visitor pattern.
 */
public interface Visitor {
    /** 
     * Visit a @a LeafNode. 
     */
    void visit(LeafNode node);

    /**
     * Visit a @a ParenNode.
     */
    void visit(ParenNode node);

    /**
     * Visit a @a CompositeNegateNode. 
     */
    void visit(CompositeNegateNode node);

    /**
     * Visit a @a CompositeAddNode. 
     */
    void visit(CompositeAddNode node);

    /** 
     * Visit a @a CompositeSubtractNode. 
     */
    void visit(CompositeSubtractNode node);

    /**
     * Visit a @a CompositeDivideNode. 
     */
    void visit(CompositeDivideNode node);

    /** 
     * Visit a @a CompositeMultiplyNode. 
     */
    void visit(CompositeMultiplyNode node);
}
