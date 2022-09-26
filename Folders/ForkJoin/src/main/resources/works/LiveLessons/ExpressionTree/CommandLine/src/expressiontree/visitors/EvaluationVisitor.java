package expressiontree.visitors;

import expressiontree.platspecs.Platform;
import expressiontree.composites.*;

import java.util.Stack;

/**
 * This plays the role of a visitor for evaluating nodes in an
 * expression tree that is being iterated in post-order fashion (and
 * does not work correctly with any other iterator).  This class plays
 * the role of the "ConcreteVisitor" in the Visitor pattern.
 */
public class EvaluationVisitor 
       implements Visitor {
    /** 
     * Stack containing the integral total of the expression tree
     * that's being visited.
     */
    private Stack<Integer> mStack =
        new Stack<>();

    /** 
     * Constructor.
     */
    public EvaluationVisitor() {
    }

    /**
     * Visit a @a ParenNode.
     */
    public void visit(ParenNode node) {
        // No-op;
    }

    /**
     * Visit a {@code LeafNode}.
     */
    public void visit(LeafNode node) {
        mStack.push(node.getItem());
    }

    /** 
     * Visit a  CompositeSubtractNode. 
     */
    public void visit(CompositeNegateNode node) {
        if (mStack.size() >= 1)
            mStack.push(-mStack.pop());
    }

    /** 
     * Visit a {@code CompositeAddNode}.
     */
    public void visit(CompositeAddNode node) {
        if (mStack.size() >= 2)
            mStack.push(mStack.pop() + mStack.pop());
    }

    /** 
     * Visit a {@code CompositeSubtractNode}. 
     */
    public void visit(CompositeSubtractNode node) {
        if(mStack.size() >= 2) {
            int rhs = mStack.pop();
            mStack.push(mStack.pop() - rhs);
        }
    }

    /** 
     * Visit a {@code CompositeDivideNode}.
     */
    public void visit(CompositeDivideNode node) {
        if (mStack.size() >= 2) {
            if (mStack.peek() != 0) {
                int rhs = mStack.pop();
                mStack.push(mStack.pop() / rhs);
            } else {
                Platform platform = Platform.instance();
                platform.errorLog("EvaluationVisitor",
                                  "\n\n**: Division by zero is not allowed. ");
                platform.errorLog("EvaluationVisitor",
                                  "Resetting evaluation visitor.\n\n");
                reset();
            }
        }
    }

    /** 
     * Visit a {@code CompositeMultiplyNode}. 
     */
    public void visit(CompositeMultiplyNode node) {
        if (mStack.size () >= 2)
            mStack.push(mStack.pop () * mStack.pop ());
    }

    /** 
     * Print the total of the evaluation. 
     */
    public int total() {
        if(!mStack.isEmpty())
            return mStack.peek();
        else
            return 0;
    }

    /** 
     * Resets the evaluation to it can be reused. 
     */
    public void reset() {
        mStack.clear();
    }
}
