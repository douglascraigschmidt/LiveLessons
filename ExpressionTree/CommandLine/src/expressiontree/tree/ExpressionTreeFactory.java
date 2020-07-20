package expressiontree.tree;

import expressiontree.composites.ComponentNode;

/**
 * This class implements the Factory Method pattern to create {@code
 * ExpressionTree} objects.  If you want a different getType of {@code
 * ExpressionTree} you can subclass from this class and override the
 * {@code makeExpressionTree()} factory method.
 */
public class ExpressionTreeFactory {
    /** 
     * Create a new {@code ExpressionTree} object that encapsulates the {@code
     * componentNodeRoot} that's passed as a parameter.
     */
    public ExpressionTree makeExpressionTree(ComponentNode componentNodeRoot) {
        return new ExpressionTree(componentNodeRoot);
    }
}
	
	
	
