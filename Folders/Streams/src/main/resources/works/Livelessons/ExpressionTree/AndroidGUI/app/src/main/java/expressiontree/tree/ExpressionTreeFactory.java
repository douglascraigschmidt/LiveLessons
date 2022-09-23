package expressiontree.tree;

import expressiontree.nodes.ComponentNode;
import expressiontree.tree.ExpressionTree;

/**
 * This class implements the Factory Method pattern to create {@code
 * ExpressionTree} objects.  If you want a different type of {@code
 * ExpressionTree} you can subclass from this class and override the
 * {@code makeExpressionTree()} factory method.
 */
public class ExpressionTreeFactory {
    /** 
     * Create a new @a ExpressionTree object that encapsulates the @a
     * componentNodeRoot that's passed as a parameter.
     */
    public ExpressionTree makeExpressionTree(ComponentNode componentNodeRoot) {
        return new ExpressionTree(componentNodeRoot);
    }
}
	
	
	
