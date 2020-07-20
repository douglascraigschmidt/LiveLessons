package expressiontree.interpreter.exprs;

import expressiontree.composites.ComponentNode;

/**
 * Define an abstract expression interface that's implemented by parse
 * tree builder classes.  This interface plays the role of the
 * AbstractExpression in the Interpreter pattern and the role of the
 * Builder in the Builder pattern.
 */
public abstract class Expr {
    /**
     * Symbol getType for each Symbol.
     */
    private final int mSymbolType;

    /**
     * @return Return the type of the Symbol.
     */
    public int getType() {
        return mSymbolType;
    }

    /**
     * Constructor sets the type.
     */
    public Expr(int symbolType) {
        mSymbolType = symbolType;
    }

    /**
     * Interpret the expression and return a value.
     */
    public int interpret() { return 0; };

    /**
     * Hook method for building a {@code ComponentNode} a la the
     * Builder pattern.
     */
    public ComponentNode build() { return null; }
}
