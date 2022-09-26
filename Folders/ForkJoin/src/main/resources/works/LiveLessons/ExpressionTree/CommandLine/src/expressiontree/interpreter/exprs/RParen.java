package expressiontree.interpreter.exprs;

import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sRPAREN;

/**
 * A parse tree node that handles the right-paren terminal expression.
 */
public class RParen
      extends Expr {
    /**
     * Constructor.
     */
    public RParen() {
        super(sRPAREN);
    }
}

