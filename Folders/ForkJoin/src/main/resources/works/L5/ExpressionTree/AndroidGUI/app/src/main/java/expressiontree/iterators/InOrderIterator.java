package expressiontree.iterators;

import expressiontree.tree.ExpressionTree;

import java.util.Iterator;
import java.util.Stack;

/**
 * Iterates through an @a Tree using in-order traversal.  Plays the
 * role of the "ConcreteStrategy" in the Strategy pattern that defines
 * the pre-order iteration algorithm.
 */
public class InOrderIterator 
       implements Iterator<ExpressionTree> {
    /** 
     * Stack of trees. 
     */
    private Stack <ExpressionTree> mStack= new Stack<>();
    
    /** 
     * Constructor.
     */
    InOrderIterator(ExpressionTree tree) {
        if(!tree.isNull()) {
            mStack.push(tree);
		
            while(!mStack.peek().left().isNull()) 
                mStack.push(mStack.peek().left());
        }
    }
	
    /** 
     * Moves iterator to the next expression tree in the stack. 
     */
    public ExpressionTree next() {
        ExpressionTree result = mStack.peek();
		
        if (!mStack.isEmpty()) {
            // If we have nodes greater than ourselves.
            if (!mStack.peek().right().isNull()) {
                // Push the mRight child node onto the stack
                // and pop the old parent(it's been visited
                // now). 
                mStack.push(mStack.pop().right());

                // Keep pushing until we get to the mLeft most
                // child.
                while(!mStack.peek().left().isNull())
                    mStack.push(mStack.peek().left());
            } else
                mStack.pop();
        }
	
        return result;
    }
	
    /** 
     * Returns false if stack is empty. 
     */
    public boolean hasNext() {
        return !mStack.empty();
    }

    /** 
     * Removes an expression tree from the top of the stack. 
     */
    public void remove() {
        mStack.pop();
    }
}
