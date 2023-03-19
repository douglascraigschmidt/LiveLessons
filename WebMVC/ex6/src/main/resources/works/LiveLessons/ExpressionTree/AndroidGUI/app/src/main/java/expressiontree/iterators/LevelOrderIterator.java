package expressiontree.iterators;

import expressiontree.tree.ExpressionTree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Iterates through an @a Tree in level-order.  Plays the role of the
 * "ConcreteStrategy" in the Strategy pattern that defines the
 * pre-order iteration algorithm.
 */
public class LevelOrderIterator 
       implements Iterator<ExpressionTree> {
    /** 
     * Queue of expression trees. 
     */
    private Queue <ExpressionTree> mQueue =
        new LinkedList<ExpressionTree>();
    
    /** 
     * Constructor.
     */
    public LevelOrderIterator(ExpressionTree tree) {
        if (!tree.isNull())
            mQueue.add(tree);
    }
	
    /** 
     * Moves iterator to the next expression tree in the stack. 
     */
    public ExpressionTree next() {
        // Store the current front element in the mQueue.
        ExpressionTree result = mQueue.peek();
		
        if (!mQueue.isEmpty()) {
            // We need to pop the node off the stack before
            // pushing the children, or else we'll revisit this
            // node later.
            ExpressionTree temp = mQueue.remove();

            // Note the order here: right first, then left. Since
            // this is LIFO, this results in the left child being
            // the first evaluated, which fits into the Pre-order
            // traversal strategy.
            if (!temp.right().isNull())
                mQueue.add (temp.right());
            if (!temp.left().isNull())
                mQueue.add (temp.left());
        }
		
        return result;
    }
	
    /** 
     * Checks if the mQueue is empty.
     */
    public boolean hasNext() {
        return !mQueue.isEmpty();
    }

    /** 
     * Removes an expression tree from the front of the mQueue.
     */
    public void remove() {
        mQueue.remove();
    }
}
