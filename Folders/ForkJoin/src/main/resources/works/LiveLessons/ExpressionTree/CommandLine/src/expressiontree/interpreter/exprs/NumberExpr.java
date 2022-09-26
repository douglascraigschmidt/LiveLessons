package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;
import expressiontree.composites.LeafNode;
import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sNUMBER;

/**
 * A parse tree interpreter/builder that handles number terminal
 * expressions.
 */
public class NumberExpr
      extends Expr {
    /**
     * Value of the number.
     */
    private final int mNumber;

    /**
     * Constructor.
     */
    public NumberExpr(int number) {
        super(sNUMBER);
        mNumber = number;
    }

    /**
     * Constructor.
     */
    public NumberExpr(String number) {
        super(sNUMBER);
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
