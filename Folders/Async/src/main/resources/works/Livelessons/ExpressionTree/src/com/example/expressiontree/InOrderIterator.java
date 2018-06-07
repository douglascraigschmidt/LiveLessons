package com.example.expressiontree;

import java.util.Iterator;
import java.util.Stack;

/**
 * @class InOrderIterator
 * 
 * @brief Iterates through an @a Tree using in-order traversal.  Plays
 *        the role of the "ConcreteStrategy" in the Strategy pattern
 *        that defines the pre-order iteration algorithm.
 */
public class InOrderIterator implements Iterator<ExpressionTree> {
    /** Stack of trees. */
    private Stack <ExpressionTree> stack 
        = new Stack<ExpressionTree>();
    
    /** Ctor */
    public InOrderIterator(ExpressionTree tree) {
        if (!tree.isNull()) {
            stack.push(tree);
		
            while(!stack.peek().left().isNull()) {
                ExpressionTree root = stack.peek();

                stack.push(stack.peek().left());
            }
        }
    }
	
    /** Moves iterator to the next expression tree in the stack. */
    public ExpressionTree next() {
        ExpressionTree result = stack.peek();
		
        if (!stack.isEmpty()) {
            /** If we have nodes greater than ourselves. */
            if (!stack.peek().right().isNull()) {
                /**
                 * Push the right child node onto the stack
                 * and pop the old parent(it's been visited
                 * now). 
                 */
                stack.push(stack.pop().right());

                /** 
                 * Keep pushing until we get to the left most
                 * child.
                 */
                while(!stack.peek().left().isNull())
                    stack.push(stack.peek().left());
            }
            else
                stack.pop();
        }
	
        return result;
    }
	
    /** Returns false if stack is empty. */
    public boolean hasNext() {
        return !stack.empty();
    }

    /** Removes an expression tree from the top of the stack. */
    public void remove() {
        stack.pop();
    }
}
