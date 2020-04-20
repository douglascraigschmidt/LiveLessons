package expressiontree.iterators;

import expressiontree.tree.ExpressionTree;

import java.util.Iterator;
import java.util.Stack;

/**
 * Iterates through an @a Tree in pre-order.  Plays the role of the
 * "ConcreteStrategy" in the Strategy pattern that defines the
 * pre-order iteration algorithm.
 */
public class PreOrderIterator 
       implements Iterator<ExpressionTree> {
    /** 
     * Stack of expression trees. 
     */
    private Stack <ExpressionTree> mStack = new Stack<>();
	
    /** 
     * Constructor.
     */
    public PreOrderIterator(ExpressionTree tree) {
        if (!tree.isNull()) 
            mStack.push(tree);
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
	
            // Note the order here: mRight first, then left. Since this
            // is LIFO, this results in the left child being the first
            // evaluated, which fits into the Pre-order traversal
            // strategy.
            if (!temp.right().isNull())
                mStack.push(temp.right());
            if (!temp.left().isNull())
                mStack.push(temp.left());
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
