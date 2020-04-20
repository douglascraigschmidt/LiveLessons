package expressiontree.interpreter.postorder;


import expressiontree.nodes.*;

/**
 * Define an abstract expression interface that's implemented by parse
 * tree builder classes.  This interface plays the role of the
 * AbstractExpression in the Interpreter pattern and the role of the
 * Builder in the Builder pattern.
 */
interface Expr {
    /**
     * Interpret the expression and return a value.
     */
    int interpret();

    /**
     * Hook method for building a {@code ComponentNode} a la the
     * Builder pattern.
     */
    ComponentNode build();
}

/**
 * A parse tree interpreter/builder that handles number terminal
 * expressions.
 */
class NumberExpr
      implements Expr {
    /**
     * Value of the number.
     */
    private int mNumber;

    /** 
     * Constructor.
     */
    NumberExpr(int number) {
        mNumber = number;
    }

    /** 
     * Constructor.
     */
    NumberExpr(String number) {
        mNumber = Integer.parseInt(number);
    }

    /**
     * Interpret this terminal expression by simply returning the
     * number.
     */
    @Override
    public int interpret() {
        return mNumber;
    }

    /**
     * Hook method for building a {@code LeafNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() { return new LeafNode(mNumber); }
}

/**
 * A parse tree interpreter/builder that handles the unary minus
 * operator non-terminal expression.
 */
class NegateExpr
      implements Expr {
    /**
     * The expression associated with this negation operator.
     */
    private Expr mRightExpr;

    /**
     * Constructor.
     */ 
    NegateExpr(Expr rightExpression) {
        mRightExpr = rightExpression;
    }

    /**
     * Interpret the non-terminal by negating the stored expression.
     */
    @Override
    public int interpret() {
        return -mRightExpr.interpret();
    }

    /**
     * Hook method for building a {@code CompositeNegateNode} a la the
     * Builder pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeNegateNode(mRightExpr.build());
    }
}

/**
 * Abstract super class for operator non-terminal expressions.
 */
abstract class BinaryExpr
      implements Expr {
    /**
     * The left expression associated with the operator.
     */
    Expr mLeftExpr;

    /**
     * The right expression associated with the operator.
     */
    Expr mRightExpr;

    /**
     * Constructor.
     */
    BinaryExpr(Expr leftExpression,
               Expr rightExpresion) {
        mLeftExpr = leftExpression;
        mRightExpr = rightExpresion;
    }
}

/**
 * A parse tree interpreter/builder that handles the binary multiply
 * operator non-terminal expression.
 */
class MultiplyExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */ 
    MultiplyExpr(Expr leftExpression,
                 Expr rightExpression) {
        super(leftExpression, rightExpression);
    }

    /**
     * Interpret the non-terminal by multiplying the stored left and
     * right expressions.
     */
    @Override
    public int interpret() {
        return mLeftExpr.interpret() 
            * mRightExpr.interpret();
    }

    /**
     * Hook method for building a {@code CompositeMultipleNode} a la
     * the Builder pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeMultiplyNode(mLeftExpr.build(),
                                         mRightExpr.build());
    }
}

/**
 * A parse tree interpreter/builder that handles the binary divide
 * operator non-terminal expression.
 */
class DivideExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */ 
    DivideExpr(Expr leftExpression,
               Expr rightExpression) {
        super(leftExpression, rightExpression);
    }

    /**
     * Interpret the non-terminal by dividing the stored left and
     * right expressions.
     */
    @Override
    public int interpret() {
        return mLeftExpr.interpret() 
            / mRightExpr.interpret();
    }

    /**
     * Hook method for building a {@code CompositeDivideNode} a la the
     * Builder pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeDivideNode(mLeftExpr.build(),
                                       mRightExpr.build());
    }
}

/**
 * A parse tree interpreter/builder that handles the binary add
 * operator non-terminal expression.
 */
class AddExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */ 
    AddExpr(Expr leftExpression,
            Expr rightExpression) {
        super(leftExpression, rightExpression);
    }

    /**
     * Interpret the non-terminal by adding the stored left and right
     * expressions.
     */
    @Override
    public int interpret() {
        return mLeftExpr.interpret()
            + mRightExpr.interpret();
    }

    /**
     * Hook method for building a {@code CompositeAddNode} a la the
     * Builder pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeAddNode(mLeftExpr.build(),
                                    mRightExpr.build());
    }
}

/**
 * A parse tree interpreter/builder that handles the binary subtract
 * operator non-terminal expression.
 */
class SubtractExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */ 
    SubtractExpr(Expr leftExpression,
                 Expr rightExpression) {
        super(leftExpression, rightExpression);
    }

    /**
     * Interpret the non-terminal by subtracting the stored left from
     * the stored right expression.
     */
    @Override
    public int interpret() {
        return mLeftExpr.interpret() 
            - mRightExpr.interpret();
    }

    /**
     * Hook method for building a {@code CompositeSubtractNode} a la
     * the Builder pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeSubtractNode(mLeftExpr.build(),
                                         mRightExpr.build());
    }
}

