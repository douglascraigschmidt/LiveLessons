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
 * operations, after first logging the start and finish of each call.
 */
public class InstrumentedExpressionTree
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
    public InstrumentedExpressionTree(ComponentNode root) {
        super(root);
    }

    /** 
     * Returns whether a the tree is null. 
     */
    public boolean isNull() {
        System.out.println("starting isNull() call");
        boolean temp = super.isNull();
        System.out.println("finished isNull() call");
        return temp;
    }

    /** 
     * Returns root. 
     */
    public ComponentNode getRoot() {
        System.out.println("starting getRoot() call");
        ComponentNode temp = mRoot;
        System.out.println("finished getRoot() call");
        return temp;
    }

    /** 
     * Returns the root getItem.
     */
    public int getItem() throws Exception {
        System.out.println("starting mLeft() call");
        int temp = super.getItem();
        System.out.println("finished mLeft() call");
        return temp;
    }

    /**
     * Returns the tree's mLeft node.
     */
    public ExpressionTree getLeftChild() {
        System.out.println("starting mLeft() call");
        ExpressionTree temp = super.getLeftChild();
        System.out.println("finished mRight() call");
        return temp;
    }

    /** 
     * Returns the tree's mRight node.
     */
    public ExpressionTree getRightChild() {
        System.out.println("starting mRight() call");
        ExpressionTree temp = super.getRightChild();
        System.out.println("finished mRight() call");
        return temp;
    }

    /** 
     * Accepts a @a visitor. 
     */
    public void accept(Visitor visitor) {
        System.out.println("starting accept() call");
        super.accept(visitor);
        System.out.println("finished accept() call");
    }

    /** 
     * Returns the designated iterator after requesting it from
     * factory method. 
     */
    public Iterator<ExpressionTree> makeIterator(String traversalOrder) {
        System.out.println("starting makeIterator() call");
        Iterator<ExpressionTree> temp =
            mTreeIteratorFactory.makeIterator(this,
                                             traversalOrder);
        System.out.println("finished makeIterator() call");
        return temp;
    }
}
