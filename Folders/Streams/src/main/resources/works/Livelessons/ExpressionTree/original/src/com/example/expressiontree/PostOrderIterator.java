package com.example.expressiontree;

import java.util.Iterator;
import java.util.Stack;

/**
 * @class PostOrderIterator
 * 
 * @brief Iterates through an @a Tree in post-order.  Plays
 *        the role of the "ConcreteStrategy" in the Strategy pattern
 *        that defines the post-order iteration algorithm.
 */
public class PostOrderIterator implements Iterator<ExpressionTree> {
    /** Stack of expression trees. */
    private Stack <ExpressionTree> stack = new Stack<ExpressionTree>();
	
    /** Ctor */
    public PostOrderIterator(ExpressionTree tree) {
        if(!tree.isNull()) {
            stack.push(tree);
			
            /** 
             * The Commented code does not work on unary operator
             * nodes with no left child, but a right child - or at
             * least, there is a certain depth that this will not go
             * down.
             */
            while(!tree.isNull()) {
                if(!tree.right().isNull())
                    stack.push(tree.right());
                if(!tree.left().isNull()) {
                    /** 
                     * If there was a left, then update
                     * current this is the case for all
                     * non-negations.
                     */
                    stack.push(tree.left());
                    tree = tree.left();
                }
                else {
                    /** 
                     * If there was not a left, then
                     * current = current.right_ this
                     * handles cases of unary nodes, like
                     * negations.
                     */
                    tree = tree.right();
                }
            }
        }
    }
	
    /** Proceeds to next expression tree in the stack. */
    public ExpressionTree next() {
        ExpressionTree result = stack.peek();
        if (!stack.isEmpty()) {
            /**
             * We need to pop the node off the stack before
             * pushing the children, or else we'll revisit this
             * node later.
             */

            ExpressionTree temp = stack.pop();

            if(!stack.isEmpty() 
               && stack.peek().left().getRoot() != temp.getRoot()
               && stack.peek().right().getRoot() != temp.getRoot()) {
                temp = stack.peek();
 
                while(!temp.isNull()) {
                    if(!temp.right().isNull())
                        stack.push(temp.right());
                    if(!temp.left().isNull()) {
                        /**
                         * If there was a left, then
                         * update temp this is the
                         * case for all non-negations.
                         */
                        stack.push(temp.left());
                        temp = temp.left();
                    } else
                        /**
                         * If there was not a left, then
                         * temp = temp->right this
                         * handles cases of unary nodes,
                         * like negations.
                         */
                        temp = temp.right();
                }
            }
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
