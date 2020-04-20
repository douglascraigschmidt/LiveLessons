package com.example.expressiontree;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @class LevelOrderIterator
 *
 * @brief Iterates through an @a Tree in level-order.  Plays
 *        the role of the "ConcreteStrategy" in the Strategy pattern
 *        that defines the pre-order iteration algorithm. 
 */
public class LevelOrderIterator implements Iterator<ExpressionTree> {
    /** Queue of expression trees. */
    private Queue <ExpressionTree> queue =
        new LinkedList<ExpressionTree>();
    
    /** Ctor */
    public LevelOrderIterator(ExpressionTree tree) {
        if(!tree.isNull()) 
            queue.add(tree);
    }
	
    /** Moves iterator to the next expression tree in the stack. */
    public ExpressionTree next() {
        /** Store the current front element in the queue. */
        ExpressionTree result = queue.peek();
		
        if (!queue.isEmpty()) {
            /** 
             * We need to pop the node off the stack before
             * pushing the children, or else we'll revisit this
             * node later.
             */
            ExpressionTree temp = queue.remove();

            /**
             * Note the order here: right first, then left. Since
             * this is LIFO, this results in the left child being
             * the first evaluated, which fits into the Pre-order
             * traversal strategy.
             */
            if (!temp.right().isNull())
                queue.add (temp.right());
            if (!temp.left().isNull())
                queue.add (temp.left());
        }
		
        return result;
    }
	
    /** Checks if the queue is empty. */
    public boolean hasNext() {
        return !queue.isEmpty();
    }

    /** Removes an expression tree from the front of the queue. */
    public void remove() {
        queue.remove();
    }
}
