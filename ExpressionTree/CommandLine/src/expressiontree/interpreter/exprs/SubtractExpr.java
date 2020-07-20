package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;
import expressiontree.composites.CompositeSubtractNode;
import expressiontree.interpreter.exprs.BinaryExpr;
import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sSUBTRACTION;

/**
 * A parse tree interpreter/builder that handles the binary subtract
 * operator non-terminal expression.
 */
public class SubtractExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */
    public SubtractExpr(Expr leftExpression,
                        Expr rightExpression) {
        super(leftExpression, rightExpression, sSUBTRACTION);
    }

    public SubtractExpr() {
        super(sSUBTRACTION);
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
