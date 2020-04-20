package expressiontree.visitors;

import expressiontree.platspecs.Platform;
import expressiontree.nodes.*;

/**
 * This class serves as a visitor that print the contents of each type
 * of node in an expression tree.  This class plays the role of the
 * "ConcreteVisitor" in the Visitor pattern.
 */
public class PrintVisitor 
       implements Visitor {
    /** 
     * Constructor.
     */
    public PrintVisitor() {    
    }

    /**
     * Visits a @a LeafNode and prints it contents. 
     */
    public void visit(LeafNode node) {
        Platform.instance().addString(node.item() + " ");
    }

    /**
     * Visit a @a CompositeNegateNode and prints its contents. 
     */
    public void visit(CompositeNegateNode node) {
        Platform.instance().addString("-");
    }

    /** 
     * Visit a @a CompositeAddNode and prints its contents. 
     */
    public void visit(CompositeAddNode node) {
        Platform.instance().addString("+ ");
    }

    /**
     * Visit a @a CompositeSubtractNode and prints its contents. 
     */
    public void visit(CompositeSubtractNode node) {
        Platform.instance().addString("- ");
    }

    /** 
     * Visit a @a CompositeDivideNode and prints its contents. 
     */
    public void visit(CompositeDivideNode node) {
        Platform.instance().addString("/ ");
    }

    /** 
     * Visit a @a CompositeMultiplyNode and print its contents. 
     */
    public void visit(CompositeMultiplyNode node) {
        Platform.instance().addString("* ");
    }
}
