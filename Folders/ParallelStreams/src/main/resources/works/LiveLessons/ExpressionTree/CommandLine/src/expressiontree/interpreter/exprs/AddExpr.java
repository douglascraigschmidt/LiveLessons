package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;
import expressiontree.composites.CompositeAddNode;
import expressiontree.interpreter.exprs.BinaryExpr;
import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sADDITION;

/**
 * A parse tree interpreter/builder that handles the binary add
 * operator non-terminal expression.
 */
public class AddExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */
    public AddExpr(Expr leftExpression,
                   Expr rightExpression) {
        super(leftExpression, rightExpression, sADDITION);
    }

    public AddExpr() {
        super(sADDITION);
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
