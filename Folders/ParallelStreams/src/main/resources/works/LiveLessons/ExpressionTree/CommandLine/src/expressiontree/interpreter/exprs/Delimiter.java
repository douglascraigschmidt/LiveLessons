package expressiontree.interpreter.exprs;

import expressiontree.interpreter.exprs.Expr;

import static expressiontree.composites.ComponentNode.sDELIMITER;

/**
 * A parse tree node that handles the beginning-of-input and
 * end-of-input delimiters.
 */
public class Delimiter
      extends Expr {
    /**
     * Constructor.
     */
    public Delimiter() {
        super(sDELIMITER);
    }
}

