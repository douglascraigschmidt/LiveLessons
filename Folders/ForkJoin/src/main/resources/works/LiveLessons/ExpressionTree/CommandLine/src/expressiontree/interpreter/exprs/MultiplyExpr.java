package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;
import expressiontree.composites.CompositeMultiplyNode;
import expressiontree.interpreter.exprs.BinaryExpr;
import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sMULTIPLICATION;

/**
 * A parse tree interpreter/builder that handles the binary multiply
 * operator non-terminal expression.
 */
public class MultiplyExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */
    public MultiplyExpr(Expr leftExpression,
                        Expr rightExpression) {
        super(leftExpression, rightExpression, sMULTIPLICATION);
    }

    public MultiplyExpr() {
        super(sMULTIPLICATION);
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
