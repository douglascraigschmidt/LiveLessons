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
     * The following constants set the getPrecedence levels of the
     * various operations and operands.
     */

    /** 
     * The getPrecedence of the '$' delimiter.
     */
    final static int sDelimiterPrecedence = 1;

    /** 
     * The getPrecedence of the '+' and '-' operators.
     */
    final static int sAddSubPrecedence = 1;

    /** 
     * The getPrecedence of the '*' and '/' operators.
     */
    final static int sMulDivPrecedence = 2;

    /** 
     * The getPrecedence of a '-' (negate) operator.
     */
    final static int sNegatePrecedence = 3;

    /** 
     * The getPrecedence of a number.
     */
    final static int sNumberPrecedence = 4;

    /**
     * The getPrecedence of a paren.
     */
    final static int sParenPrecedence = 5;

    /*
     * The following fields comprise the state of each Symbol.
     */

    /**
     * Precedence of a Symbol relative to other Symbols.
     */
    private int mPrecedence;

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
           int precedence,
           int symbolType) {
        mLeft = left;
        mRight = right;
        mPrecedence = precedence;
        mSymbolType = symbolType;
    }

    /** 
     * @return Return the type of the Symbol.
     */
    public int getType() {
        return mSymbolType;
    }

    /** 
     * @return Return precedence level (higher value == higher
     * getPrecedence).
     */
    public int getPrecedence() {
        return mPrecedence;
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
     * Value of Number.
     */
    private int mNumber;

    /** 
     * Constructor.
     */
    Number(String input, int accumulatedPrecedence) {
        this(Integer.parseInt(input), accumulatedPrecedence);
    }

    /**
     * Constructor.
     */
    Number(String input) {
        this(Integer.parseInt(input), 0);
    }

    /**
     * Constructor.
     */
    Number(int input) {
        this(input, 0);
    }

    /**
     * Constructor.
     */
    Number(int input, int accumulatedPrecedence) {
        super(null,
              null,
              sNumberPrecedence + accumulatedPrecedence,
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
             int precedence,
             int tokenClass) {
        super(left,
              right,
              precedence,
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
                  int precedence,
                  int tokenClass) {
        super(null,
              right,
              precedence,
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
    Negate(int accumulatedPrecedence) {
        super(null,
              sNegatePrecedence + accumulatedPrecedence,
              sNEGATION);
    }

    /** 
     * Constructor.
     */
    Negate() {
        this(0);
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
    Add(int accumulatedPrecedence) {
        super(null, 
              null, 
              sAddSubPrecedence + accumulatedPrecedence,
              sADDITION);
    }

    /**
     * Constructor.
     */
    Add() {
        this(0);
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
    Subtract(int accumulatedPrecedence) {
        super(null, 
              null,
              sAddSubPrecedence + accumulatedPrecedence,
              sSUBTRACTION);
    }

    /**
     * Constructor.
     */
    Subtract() {
        this(0);
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
    Multiply(int accumulatedPrecedence) {
        super(null, 
              null,
              sMulDivPrecedence + accumulatedPrecedence,
              sMULTIPLICATION);
    }

    /** 
     * Constructor.
     */
    Multiply() {
        this(0);
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
    Divide(int accumulatedPrecedence) {
        super(null, 
              null, 
              sMulDivPrecedence + accumulatedPrecedence,
              sDIVISION);
    }

    /**
     * Constructor.
     */
    Divide() {
        this(0);
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
              sParenPrecedence,
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
              sParenPrecedence,
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
              sDelimiterPrecedence,
              sDELIMITER);
    }
}
