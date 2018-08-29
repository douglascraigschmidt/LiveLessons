package expressiontree.interpreter.inorder;

import expressiontree.nodes.*;

import static expressiontree.nodes.ComponentNode.*;

/**
 * Base class for the various parse tree subclasses.  The class plays
 * the role of the AbstractExpression in the Interpreter pattern and
 * the role of the Builder in the Builder pattern.
 */
abstract class Symbol {
    /*
     * The following fields comprise the state of each Symbol.
     */

    /**
     * Symbol getType for each Symbol.
     */
    private int mSymbolType;

    /** 
     * Left symbol in the parse tree.
     */
    protected Symbol mLeft;

    /** 
     * Right symbol in the parse tree.
     */
    protected Symbol mRight;

    /** 
     * Constructor. 
     */
    Symbol(Symbol left,
           Symbol right,
           int symbolType) {
        mLeft = left;
        mRight = right;
        mSymbolType = symbolType;
    }

    /** 
     * @return Return the type of the Symbol.
     */
    public int getType() {
        return mSymbolType;
    }

    /** 
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    ComponentNode build() { return null; }
}

/**
 * A parse tree node that handles number terminal expressions.
 */
class Number
      extends Symbol {
    /**
     * Value of the number.
     */
    private int mNumber;

    /** 
     * Constructor.
     */
    Number(String input) {
        this(Integer.parseInt(input));
    }


    /**
     * Constructor.
     */
    Number(int input) {
        super(null,
              null,
              sNUMBER);
        mNumber = input;
    }

    /** 
     * Builds a {@code LeafNode} a la the Builder pattern.
     */
    ComponentNode build() {
        return new LeafNode(mNumber);
    }
}

/**
 * A parse tree node that handles operator non-terminal expressions.
 */
abstract class Operator
         extends Symbol {
    /** 
     * Constructor.
     */
    Operator(Symbol left,
             Symbol right,
             int tokenClass) {
        super(left,
              right,
              tokenClass);
    }
}

/**
 * A parse tree node that handles unary operator non-terminal
 * expressions.
 */
abstract class UnaryOperator
         extends Symbol {
    /** 
     * Constructor.
     */
    UnaryOperator(Symbol right,
                  int tokenClass) {
        super(null,
              right,
              tokenClass);
    }

    /** 
     * Abstract method for building a {@code UnaryOperator} node a la
     * the Builder pattern.
     */
    abstract ComponentNode build();
}

/**
 * A parse tree node that handles the unary minus operator
 * non-terminal expression.
 */
class Negate
      extends UnaryOperator {
    /** 
     * Constructor.
     */
    Negate() {
        super(null,
              sNEGATION);
    }

    /** 
     * Method for building a {@code Negate} node a la the Builder
     * pattern.
     */
    ComponentNode build() {
        return new CompositeNegateNode(mRight.build());
    }
}

/**
 * A parse tree node that handles the binary add operator non-terminal
 * expression.
 */
class Add
      extends Operator {
    /**
     * Constructor.
     */
    Add() {
        super(null, 
              null, 
              sADDITION);
    }

    /** 
     * Method for building an {@code Add} node a la the Builder pattern.
     */
    ComponentNode build() {
        return new CompositeAddNode(mLeft.build(),
                                    mRight.build());
    }
}

/**
 * A parse tree node that handles the binary subtract operator
 * non-terminal expression.
 */
class Subtract
      extends Operator {
    /**
     * Constructor.
     */
    Subtract() {
        super(null, 
              null,
              sSUBTRACTION);
    }

    /** 
     * Method for building a {@code Subtract} node a la the Builder
     * pattern.
     */
    ComponentNode build() {
        return new CompositeSubtractNode(mLeft.build(),
                                         mRight.build());
    }
}

/**
 * A parse tree node that handles the binary multiply operator
 * non-terminal expression.
 */
class Multiply
      extends Operator {
    /** 
     * Constructor.
     */
    Multiply() {
        super(null, 
              null,
              sMULTIPLICATION);
    }

    /** 
     * Method for building a {@code Multiply} node a la the Builder
     * pattern.
     */
    ComponentNode build() {
        return new CompositeMultiplyNode(mLeft.build(),
                                         mRight.build());
    }
}

/**
 * A parse tree node that handles the binary divide operator
 * non-terminal expression.
 */
class Divide
      extends Operator {
    /**
     * Constructor.
     */
    Divide() {
        super(null, 
              null, 
              sDIVISION);
    }

    /**
     * Method for building a {@code Divide} node a la the Builder
     * pattern.
     */
    ComponentNode build() {
        return new CompositeDivideNode(mLeft.build(),
                                       mRight.build());
    }
}

/**
 * A parse tree node that handles the getLeftChild-paren terminal expression.
 */
class LParen
      extends Symbol {
    /**
     * Constructor.
     */
    LParen() {
        super(null,
              null,
              sLPAREN);
    }
}

/**
 * A parse tree node that handles the getRightChild-paren terminal expression.
 */
class RParen
      extends Symbol {
    /**
     * Constructor.
     */
    RParen() {
        super(null,
              null,
              sRPAREN);
    }
}

/**
 * A parse tree node that handles the beginning-of-input and
 * end-of-input delimiters.
 */
class Delimiter
      extends Symbol {
    /**
     * Constructor.
     */
    Delimiter() {
        super(null,
              null,
              sDELIMITER);
    }
}
