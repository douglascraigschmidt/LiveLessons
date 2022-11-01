package expressiontree.iterators;

import expressiontree.tree.ExpressionTree;

import java.util.Iterator;
import java.util.Stack;

/**
 * Iterates through a {@code Tree} in post-order.  Plays the role of
 * the "ConcreteStrategy" in the Strategy pattern that defines the
 * post-order iteration algorithm.
 */
public class PostOrderIterator 
       implements Iterator<ExpressionTree> {
    /** 
     * Stack of expression trees. 
     */
    private Stack <ExpressionTree> mStack = new Stack<>();
	
    /** 
     * Constructor.
     */
    PostOrderIterator(ExpressionTree tree) {
        if(!tree.isNull()) {
            mStack.push(tree);
			
            // The Commented code does not work on unary operator
            // nodes with no mLeft child, but a mRight child - or at
            // least, there is a certain depth that this will not go
            // down.
            while(!tree.isNull()) {
                if(!tree.getRightChild().isNull())
                    mStack.push(tree.getRightChild());
                if(!tree.getLeftChild().isNull()) {
                    // If there was a mLeft, then update current this
                    // is the case for all non-negations.
                    mStack.push(tree.getLeftChild());
                    tree = tree.getLeftChild();
                }
                else {
                    // If there was not a mLeft, then current =
                    // current.right_ this handles cases of unary
                    // nodes, like negations.
                    tree = tree.getRightChild();
                }
            }
        }
    }
	
    /** 
     * Proceeds to next expression tree in the mStack.
     */
    public ExpressionTree next() {
        ExpressionTree result = mStack.peek();
        if (!mStack.isEmpty()) {
            // We need to pop the node off the mStack before pushing
            // the children, or else we'll revisit this node later.
            ExpressionTree temp = mStack.pop();

            if(!mStack.isEmpty()
               && mStack.peek().getLeftChild().getRoot() != temp.getRoot()
               && mStack.peek().getRightChild().getRoot() != temp.getRoot()) {
                temp = mStack.peek();
 
                while(!temp.isNull()) {
                    if(!temp.getRightChild().isNull())
                        mStack.push(temp.getRightChild());
                    if(!temp.getLeftChild().isNull()) {
                        // If there was a mLeft, then update temp this
                        // is the case for all non-negations.
                        mStack.push(temp.getLeftChild());
                        temp = temp.getLeftChild();
                    } else
                        // If there was not a mLeft, then temp =
                        // temp.mRight this handles cases of unary
                        // nodes, like negations.
                        temp = temp.getRightChild();
                }
            }
        }
	
        return result;
    }
	
    /** 
     * Returns false if mStack is empty.
     */
    public boolean hasNext() {
    	return !mStack.empty();
    }

    /** 
     * Removes an expression tree from the top of the mStack.
     */
    public void remove() {
        mStack.pop();
    }
}
