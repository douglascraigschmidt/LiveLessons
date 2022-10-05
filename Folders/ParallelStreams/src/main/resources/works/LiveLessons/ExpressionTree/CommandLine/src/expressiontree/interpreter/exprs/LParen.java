package expressiontree.interpreter.exprs;

import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sLPAREN;

/**
 * A parse tree node that handles the left-paren terminal expression.
 */
public class LParen
      extends Expr {
    /**
     * Constructor.
     */
    public LParen() {
        super(sLPAREN);
    }
}

