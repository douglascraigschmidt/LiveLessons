package expressiontree.interpreter;

import expressiontree.nodes.*;

import static expressiontree.interpreter.Interpreter.mAddSubPrecedence;
import static expressiontree.interpreter.Interpreter.mNumberPrecedence;
import static expressiontree.interpreter.Interpreter.mMulDivPrecedence;
import static expressiontree.interpreter.Interpreter.mNegatePrecedence;

/**
 * Base class for the various parse tree subclasses.
 */
abstract class Symbol {
    /*
     * The following static consts set the mPrecedence levels of
     * the various operations and operands.
     */

    /**
     * Default mPrecedence.
     */
    int mPrecedence = 0;

    /** 
     * Left symbol. 
     */
    protected Symbol mLeft;

    /** 
     * Right symbol. 
     */
    protected Symbol mRight;

    /** 
     * Constructor. 
     */
    Symbol(Symbol left,
           Symbol right,
           int precedence) {
        mPrecedence = precedence;
        mLeft = left;
        mRight = right;
    }

    /** 
     * Method for returning mPrecedence level (higher value means
     * higher mPrecedence.
     */
    public int precedence() {
        return mPrecedence;
    }

    /** 
     * Abstract method for adding mPrecedence.
     */
    public abstract int addPrecedence(int accumulatedPrecedence);

    /** 
     * Abstract method for building a @a ComponentNode. 
     */
    abstract ComponentNode build();
}

/**
 * Defines a node in the parse tree for number terminal
 * expressions.
 */
class Number extends Symbol {
    /**
     * Value of Number. 
     */
    public int item;

    /** 
     * Constructor.
     */
    Number(String input) {
        super(null, null, mNumberPrecedence);
        item = Integer.parseInt(input);
    }

    /**
     * Constructor.
     */
    Number(int input) {
        super(null, null, mNumberPrecedence);
        item = input;
    }

    /** 
     * Adds mNumberPrecedence to the current mAccumulatedPrecedence
     * value.
     */
    public int addPrecedence(int accumulatedPrecedence) {
        return mPrecedence =
            mNumberPrecedence + accumulatedPrecedence;
    }

    /** 
     * Method for returning mPrecedence level (higher value means
     * higher mPrecedence).
     */
    public int precedence() {
        return mPrecedence;
    }

    /** 
     * Builds a @a LeadNode. 
     */
    ComponentNode build() {
        return new LeafNode(item);
    }
}

/**
 * Defines a base class in the parse tree for operator
 * non-terminal expressions.
 */
abstract class Operator
    extends Symbol {
    /** 
     * Constructor.
     */
    Operator(Symbol left,
             Symbol right,
             int precedence) {
        super(left, right, precedence);
    }
}

/**
 * Defines a node in the parse tree for unary operator
 * non-terminal expressions.
 */
abstract class UnaryOperator
    extends Symbol {
    /** 
     * Constructor.
     */
    UnaryOperator(Symbol right,
                  int precedence) {
        super(null, right, precedence);
    }

    /** 
     * Abstract method for building a @a UnaryOperator node. 
     */
    abstract ComponentNode build();
}

/**
 * Defines a node in the parse tree for unary minus operator
 * non-terminal expression.
 */
class Negate
      extends UnaryOperator {
    /** 
     * Constructor.
     */
    Negate() {
        super(null, mNegatePrecedence);
    }

    /** 
     * Adds mPrecedence to its current value.
     */
    public int addPrecedence(int accumulatedPrecedence) {
        return mPrecedence =
            mNegatePrecedence + accumulatedPrecedence;
    }

    /** 
     * Method for building a @a Negate node. 
     */
    ComponentNode build() {
        return new CompositeNegateNode(mRight.build());
    }

    /** 
     * Returns the current mPrecedence.
     */
    public int precedence() {
        return mPrecedence;
    }
}

/**
 * Defines a node in the parse tree for the binary add operator
 * non-terminal expression.
 */
class Add
      extends Operator {
    /**
     * Constructor.
     */
    Add() {
        super(null, null, mAddSubPrecedence);
    }

    /** 
     * Adds Precedence to its current value. 
     */
    public int addPrecedence(int accumulatedPrecedence) {
        return mPrecedence =
            mAddSubPrecedence + accumulatedPrecedence;
    }

    /** 
     * Method for building an {@code Add} node. 
     */
    ComponentNode build() {
        return new CompositeAddNode(mLeft.build(),
                                    mRight.build());
    }

    /**
     * Returns the current mPrecedence.
     */
    public int precedence() {
        return mPrecedence;
    }
}

/**
 * Defines a node in the parse tree for the binary subtract
 * operator non-terminal expression.
 */
class Subtract
      extends Operator {
    /**
     * Constructor.
     */
    Subtract() {
        super(null, null, mAddSubPrecedence);
    }

    /** 
     * Adds mPrecedence to its current value.
     */
    public int addPrecedence(int accumulatedPrecedence) {
        return mPrecedence =
            mAddSubPrecedence + accumulatedPrecedence;
    }

    /** 
     * Method for building a {@code Subtract} node.
     */
    ComponentNode build() {
        return new CompositeSubtractNode(mLeft.build(),
                                         mRight.build());
    }

    /** 
     * Returns the current mPrecedence.
     */
    public int precedence() {
        return mPrecedence;
    }
}

/**
 * Defines a node in the parse tree for the binary multiply
 * operator non-terminal expression.
 */
class Multiply
      extends Operator {
    /** 
     * Constructor.
     */
    Multiply() {
        super(null, null, mMulDivPrecedence);
    }

    /** 
     * Adds mPrecedence to its current value.
     */
    public int addPrecedence(int accumulatedPrecedence) {
        return mPrecedence =
            mMulDivPrecedence + accumulatedPrecedence;
    }

    /** 
     * Method for building a {@code Multiply} node.
     */
    ComponentNode build() {
        return new CompositeMultiplyNode(mLeft.build(),
                                         mRight.build());
    }

    /** 
     * Returns the mPrecedence.
     */
    public int precedence() {
        return mPrecedence;
    }
}

/**
 * Defines a node in the parse tree for the binary divide operator
 * non-terminal expression.
 */
class Divide
      extends Operator {
    /**
     * Constructor.
     */
    Divide() {
        super(null, null, mMulDivPrecedence);
    }

    /**
     * Returns the current mPrecedence.
     */
    public int precedence() {
        return mPrecedence;
    }

    /**
     * Adds mPrecedence to its current value.
     */
    public int addPrecedence(int accumulatedPrecedence) {
        return mPrecedence =
            mMulDivPrecedence + accumulatedPrecedence;
    }

    /**
     * Method for building a {@code Divide} node. 
     */
    ComponentNode build() {
        return new CompositeDivideNode(mLeft.build(),
                                       mRight.build());
    }
}
