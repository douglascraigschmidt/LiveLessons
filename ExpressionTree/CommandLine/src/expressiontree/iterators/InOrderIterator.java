package expressiontree.iterators;

import expressiontree.composites.ParenNode;
import expressiontree.tree.ExpressionTree;

import java.util.Iterator;
import java.util.Stack;

import static expressiontree.composites.ComponentNode.mCurrentTokenPrecedence;
import static expressiontree.composites.ComponentNode.mTopOfStackPrecedence;

/**
 * Iterates through a {@code Tree} using in-order traversal.  Plays
 * the role of the "ConcreteStrategy" in the Strategy pattern that
 * defines the in-order iteration algorithm.
 * 
 * This implementation also correctly adds left ('(') and right (')')
 * parentheses to the in-order traversal so that it obeys the proper
 * precedence rules of the expression tree.
 */
public class InOrderIterator 
       implements Iterator<ExpressionTree> {
    /** 
     * Stack of trees. 
     */
    private Stack<ExpressionTree> mNodeStack= new Stack<>();

    /**
     * Stack of getLeftChild ('(') and getRightChild (')') parens.
     */
    private Stack<ExpressionTree> mParenStack =
            new Stack<>();

    /**
     * Keeps track of the right most number in a parenthesized
     * expression.
     */
    private Stack<ExpressionTree> mLastNodeStack =
            new Stack<>();

    /**
     * An ExpressionTree node that prints a left ('(') paren.
     */
    private final static ExpressionTree mLParen =
        new ExpressionTree(new ParenNode('('));

    /**
     * An ExpressionTree node that prints a left (')') paren.
     */
    private final static ExpressionTree mRParen =
        new ExpressionTree(new ParenNode(')'));

    /** 
     * Constructor initializes the iterator.
     */
    InOrderIterator(ExpressionTree tree) {
        if(!tree.isNull()) {
            // Push the root of the tree on the stack.
            mNodeStack.push(tree);
		
            // Keep pushing until we get to the left most child.
            while (!mNodeStack.peek().getLeftChild().isNull()) {
                // Get the current root of the tree.
                ExpressionTree root = mNodeStack.peek();

                // Check for (and then handle) the case where the left
                // child has a lower precendence than the root node.
                checkAndHandleLowerPrecedenceChild(root,
                                                   root.getLeftChild());

                // Push the getLeftChild node onto the stack.
                mNodeStack.push(root.getLeftChild());
            }
        }
    }
	
    /** 
     * Moves iterator to the next expression tree in the stack. 
     */
    public ExpressionTree next() {
        // If the "lparen" stack is not empty then pop the lparen node
        // off it and return it as the next node in the tree.
        if (!mParenStack.empty()) {
            return mParenStack.pop();
        } 
        // If the node stack is empty but the "last node" stack is not
        // then pop that getItem from that stack and return the rparen
        // node.
        else if (mNodeStack.empty() && !mLastNodeStack.empty()) {
            mLastNodeStack.pop();
            return mRParen;
        } else {
            // Store the result to be returned.
            ExpressionTree result = mNodeStack.peek();

            // If the "last node" stack's not empty and its top item
            // == result then pop the stack and return rparen node.
            if (!mLastNodeStack.empty()
                && result.getRoot() == mLastNodeStack.peek().getRoot()) {
                // Push an rparen (')') onto the paren stack.
                mParenStack.push(mRParen);

                // Get rid of the item on the node stack since it's being
                // returned via mLastNodeStack.pop().
                mNodeStack.pop();

                // Return the item on the "last node" stack.
                return mLastNodeStack.pop();
            }

            // See if there's anything left in the stack.
            else if (!mNodeStack.isEmpty()) {
                // Go to the right child if possible.
                if (!mNodeStack.peek().getRightChild().isNull()) {
                    // Check for (and then handle) the case where the
                    // right child has a lower precendence than the
                    // root node.
                    checkAndHandleLowerPrecedenceChild(result,
                                                       mNodeStack.peek().getRightChild());

                    // Push the right child node onto the stack and
                    // pop the old parent (it's been visited now).
                    mNodeStack.push(mNodeStack.pop().getRightChild());

                    // Keep pushing until we get to the left most
                    // child.
                    while (!mNodeStack.peek().getLeftChild().isNull()) {
                        // Get the current root of the tree.
                        ExpressionTree root = mNodeStack.peek();

                        // Check for (and then handle) the case where
                        // the left child has a lower precendence than
                        // the root node.
                        checkAndHandleLowerPrecedenceChild(root,
                                                           root.getLeftChild());

                        // Push the left node onto the stack.
                        mNodeStack.push(root.getLeftChild());
                    }
                } else
                    // Remove the top item (which will be returned
                    // via 'result' from the previous peek()).
                    mNodeStack.pop();
            }

            // Return the top item on the stack.
            return result;
        }
    }
	
    /** 
     * @return True if there are remaining items to iterate over, else false.
     */
    public boolean hasNext() {
        // There's more to do if any of the stacks aren't empty.
        return !mNodeStack.empty()
            || !mLastNodeStack.empty() 
            || !mParenStack.empty();
    }

    /** 
     * Removes an expression tree from the top of the stack. 
     */
    public void remove() {
        mNodeStack.pop();
    }

    /**
     * Checks for the case where a subtree {@code root} has higher precedence than
     * its child, in which case it's necessary to add the appropriate items to the
     * paren stack and the last node stack.
     * @param root The root of the subtree.
     * @param child The child (either left or right) of the subtree.
     */
    private void checkAndHandleLowerPrecedenceChild(ExpressionTree root,
                                                    ExpressionTree child) {
        // See if the root of the tree has high precedence than the
        // right child.
        if (mTopOfStackPrecedence[root.getRoot().getType()]
            > mCurrentTokenPrecedence[child.getRoot().getType()]) {
            // The child is the root of a tree that must be
            // parenthesized, so push a '(' onto the "lparen" stack.
            mParenStack.push(mLParen);

            // Locate the last node of the parenthesized expression.
            while (!child.getRightChild().isNull())
                child = child.getRightChild();

            // Push last node onto the "last node" stack.
            mLastNodeStack.push(child);
        } 
    }
}
