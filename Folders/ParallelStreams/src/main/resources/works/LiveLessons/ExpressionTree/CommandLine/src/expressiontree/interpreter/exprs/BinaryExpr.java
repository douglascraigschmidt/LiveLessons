package expressiontree.interpreter.exprs;

import expressiontree.interpreter.exprs.Expr;

/**
 * Abstract super class for operator non-terminal expressions.
 */
public abstract class BinaryExpr
         extends Expr {
    /**
     * The left expression associated with the operator.
     */
    public Expr mLeftExpr;

    /**
     * The right expression associated with the operator.
     */
    public Expr mRightExpr;

    /**
     * Constructor.
     */
    BinaryExpr(Expr leftExpression,
               Expr rightExpression,
               int symbolType) {
        super(symbolType);
        mLeftExpr = leftExpression;
        mRightExpr = rightExpression;
    }

    public BinaryExpr(int symbolType) {
        super(symbolType);
    }
}

