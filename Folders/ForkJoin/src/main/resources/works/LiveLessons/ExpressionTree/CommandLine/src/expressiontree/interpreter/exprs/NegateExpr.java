package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;
import expressiontree.composites.CompositeNegateNode;
import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sNEGATION;

/**
 * A parse tree interpreter/builder that handles the unary minus
 * operator non-terminal expression.
 */
public class NegateExpr
      extends Expr {
    /**
     * The expression associated with this negation operator.
     */
    public Expr mRightExpr;

    /**
     * Constructor.
     */
    public NegateExpr(Expr rightExpression) {
        super(sNEGATION);
        mRightExpr = rightExpression;
    }

    public NegateExpr() {
        super(sNEGATION);
        mRightExpr = null;
    }

    /**
     * Interpret the non-terminal by negating the stored expression.
     */
    @Override
    public int interpret() {
        assert mRightExpr != null;
        return -mRightExpr.interpret();
    }

    /**
     * Hook method for building a {@code CompositeNegateNode} a la the
     * Builder pattern.
     */
    @Override
    public ComponentNode build() {
        assert mRightExpr != null;
        return new CompositeNegateNode(mRightExpr.build());
    }
}
