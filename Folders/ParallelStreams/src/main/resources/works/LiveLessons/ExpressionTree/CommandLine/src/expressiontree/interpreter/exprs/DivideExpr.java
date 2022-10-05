package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;
import expressiontree.composites.CompositeDivideNode;
import expressiontree.interpreter.exprs.BinaryExpr;
import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sDIVISION;

/**
 * A parse tree interpreter/builder that handles the binary divide
 * operator non-terminal expression.
 */
public class DivideExpr
      extends BinaryExpr {
    /**
     * Constructor.
     */
    public DivideExpr(Expr leftExpression,
                      Expr rightExpression) {
        super(leftExpression, rightExpression, sDIVISION);
    }

    public DivideExpr() {
        super(sDIVISION);
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
