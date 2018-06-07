package expressiontree.tree;

import expressiontree.iterators.IteratorFactory;
import expressiontree.nodes.ComponentNode;
import expressiontree.visitors.Visitor;

import java.util.Iterator;

/**
 * Interface for the Composite pattern that is used to contain all the
 * operator and operand nodes in the expression tree.  Plays the role
 * of the "Abstraction" in the Bridge pattern and delegates to the
 * appropriate "Implementor" that performs the expression tree
 * operations, after first synchronizing each call.
 */
public class SynchronizedExpressionTree 
       extends ExpressionTree {
    /** 
     * A factory class capable of creating iterators dynamically. 
     */
    private IteratorFactory mTreeIteratorFactory
        = new IteratorFactory();

    /**
     * Ctor that takes a @a Node * that contains all the nodes in the
     * expression tree.
     */
    public SynchronizedExpressionTree(ComponentNode root) {
        super(root);
    }

    /**
     * Returns whether a the tree is null. 
     */
    public boolean isNull() {
        boolean temp;
        synchronized(this) {
            temp = super.isNull();
        }
        return temp;
    }

    /**
     * Returns root. 
     */
    public ComponentNode getRoot() {
        ComponentNode temp;
        synchronized(this) {
            temp = mRoot;
        }
        return temp;
    }

    /** 
     * Returns the root getItem.
     */
    public int getItem() throws Exception {
        int temp;
        synchronized(this) {
            temp = super.getItem();
        }
        return temp;
    }

    /** 
     * Returns the tree's mLeft node.
     */
    public ExpressionTree getLeftChild() {
        ExpressionTree temp;
        synchronized(this) {
            temp = super.getLeftChild();
        }
        return temp;
    }

    /**
     * Returns the tree's mRight node.
     */
    public ExpressionTree getRightChild() {
        ExpressionTree temp;
        synchronized(this) {
            temp = super.getRightChild();
        }
        return temp;
    }

    /** 
     * Accepts a @a visitor. 
     */
    public void accept(Visitor visitor) {
        synchronized(this) {
            super.accept(visitor);
        }
    }

    /** 
     * Returns the designated iterator after requesting it from
     * factory method.
     */
    public Iterator<ExpressionTree> makeIterator(String traversalOrder) {
        Iterator<ExpressionTree> temp;
        synchronized(this) {
            temp = mTreeIteratorFactory.makeIterator(this,
                                                    traversalOrder);
        }
        return temp;
    }
}
