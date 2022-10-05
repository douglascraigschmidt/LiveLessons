package expressiontree.tree;

import expressiontree.iterators.IteratorFactory;
import expressiontree.composites.ComponentNode;
import expressiontree.visitors.Visitor;

import java.util.Iterator;

/**
 * API for the Composite pattern that's used to contain all the
 * operator and operand nodes in the expression tree.  This class
 * plays the role of the "Abstraction" in the Bridge pattern and
 * delegates to the appropriate "Implementor" to perform the
 * expression tree operations.
 */
public class ExpressionTree
       implements Iterable<ExpressionTree> {
   /**
     * Base implementor. 
     */
    ComponentNode mRoot;

    /** 
     * A factory class capable of creating iterators dynamically. 
     */
    private IteratorFactory mTreeIteratorFactory
        = new IteratorFactory();

    /**
     * Constructor that takes a {@code Node} that contains all the
     * nodes in the expression tree.
     */
    public ExpressionTree(ComponentNode root) {
        mRoot = root;
    }

    /** 
     * Returns whether the tree is null. 
     */
    public boolean isNull() {
        return mRoot == null;
    }

    /** 
     * Returns root. 
     */
    public ComponentNode getRoot() {
        return mRoot;
    }

    /** 
     * Returns the root's getItem.
     */
    public int getItem() throws Exception {
        return mRoot.getItem();
    }

    /** 
     * Returns a new ExpressionTree containing the tree's getLeftChild child.
     */
    public ExpressionTree getLeftChild() {
        return new ExpressionTree(mRoot.getLeftChild());
    }

    /** 
     * Returns a new ExpressionTree containing the tree's getRightChild child.
     */
    public ExpressionTree getRightChild() {
        return new ExpressionTree(mRoot.getRightChild());
    }

    /**
     * Accepts a {@code visitor}.
     */
    public void accept(Visitor visitor) {
        mRoot.accept(visitor);
    }

    /** 
     * Returns an {@code Iterator} that supports the requested
     * traveralOrder.
     */
    public Iterator<ExpressionTree> iterator(String traversalOrderRequest) {
        // Use the TreeIteratorFactory to create the requested
        // iterator.
        return mTreeIteratorFactory.iterator(this,
                                             traversalOrderRequest);
    }

    /**
     * Returns an {@code Iterator} that supports pre-order traversal.
     */
    @Override
    public Iterator<ExpressionTree> iterator() {
        return iterator(IteratorFactory.PRE_ORDER);
    }
}
