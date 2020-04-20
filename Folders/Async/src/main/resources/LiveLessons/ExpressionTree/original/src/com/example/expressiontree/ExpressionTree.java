package com.example.expressiontree;

import java.util.Iterator;

/**
 * @class ExpressionTree
 *
 * @brief Interface for the Composite pattern that is used to contain
 *        all the operator and operand nodes in the expression tree.
 *        Plays the role of the "Abstraction" in the Bridge pattern
 *        and delegates to the appropriate "Implementor" that performs
 *        the expression tree operations.
 */
public class ExpressionTree {
    /** Base implementor. */
    protected ComponentNode root = null;

    /** A factory class capable of creating iterators dynamically. */
    private IteratorFactory treeIteratorFactory
        = new IteratorFactory();

    /**
     * Ctor that takes a @a Node * that contains all the nodes in the
     * expression tree.
     */
    public ExpressionTree(ComponentNode root) {
        this.root = root;
    }

    /** Returns whether a the tree is null. */
    public boolean isNull() {
        return root == null;
    }

    /** Returns root. */
    public ComponentNode getRoot() {
        return root;
    }

    /** Returns the root item. */
    public int item() throws Exception {
        return root.item();
    }

    /** Returns the tree's left node. */
    public ExpressionTree left() {
        return new ExpressionTree(root.left());
    }

    /** Returns the tree's right node. */
    public ExpressionTree right() {
        return new ExpressionTree(root.right());
    }

    /** Accepts a @a visitor. */
    public void accept(Visitor visitor) {
        root.accept(visitor);
    }

    /** 
     * Returns an @a Iterator that supports the requested
     * traveralOrder.
     */
    public Iterator<ExpressionTree> makeIterator
        (String traversalOrderRequest) {
        /* 
         * Use the TreeIteratorFactory to create the requested
         * iterator.
         */
        return treeIteratorFactory.makeIterator
            (this,
             traversalOrderRequest);
    }
}
