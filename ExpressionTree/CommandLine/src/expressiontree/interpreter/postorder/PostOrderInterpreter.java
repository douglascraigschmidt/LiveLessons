package expressiontree.interpreter.postorder;

import expressiontree.interpreter.*;
import expressiontree.tree.ExpressionTree;
import expressiontree.tree.ExpressionTreeFactory;

import java.util.Iterator;
import java.util.Stack;

/**
 * Parses incoming expression strings into a parse tree and builds an
 * expression tree from the parse tree.  This class plays the role of
 * the "Interpreter" in the Intepreter pattern, tweaked to use the
 * getPrecedence of operators/operands to guide the creation of the
 * parse tree.  It also uses the Builder pattern to build component
 * nodes in the Composite-based expression tree.
 */
public class PostOrderInterpreter
      extends InterpreterImpl {
    /**
     * Constructor initializes the field.
     *
     * @param expressionTreeFactory
     */
    public PostOrderInterpreter(ExpressionTreeFactory expressionTreeFactory) {
        super(expressionTreeFactory);
    }

    @Override
    public ExpressionTree interpret(String inputExpression) {
        Expr parseTreeRoot =
            buildParseTree(inputExpression);

        // If the parseTree has an element in it perform the
        // (optional) optimization pass and then build the
        // ExpressionTree for the parseTree.
        if (parseTreeRoot != null) {
            // This hook method can be overridden to optimize the
            // parseTree prior to generating the ExpressionTree.
            optimizeParseTree(parseTreeRoot);

            // This hook method builds the ExpressionTree from the
            // parseTree.
            return buildExpressionTree(parseTreeRoot);
        } else
            // If we reach this, we didn't have any symbols.
            return mExpressionTreeFactory.makeExpressionTree(null);
    }

    /**
     * This hook method can be overridden to conduct optimization on
     * the {@code parseTree} prior to generating the @a
     * ExpressionTree.  By default it's a no-op.
     */
    protected void optimizeParseTree(Expr parseTree) {
    }

    /**
     * @return Return the root of a parse tree corresponding to the
     * {@code inputExpression}.
     */
    private Expr buildParseTree(String inputExpression) {
        Stack<Expr> stack = new Stack<>();

        // Create an iterator for the inputExpression and iterate
        // through all the expressions.
        for (Iterator<Expr> iter = makeIterator(stack, inputExpression);
             iter.hasNext(); ) 
            // Get next expression from the user's input and push it
            // onto the stack.
            stack.push(iter.next());

        // One way to get the "yield" of the expression is to say
        //
        // stack.pop().interpret();
        //
        // at this point!!

        // Pop the top item off the stack, which should contain the
        // complete expression tree.
        return stack.pop();
    }

    /**
     * Invoke a recursive build of the ExpressionTree, starting with
     * the root expression of the parse tree.  The Builder pattern is
     * used at each node to create the appropriate subclass of {@code
     * ComponentNode}.
     */
    private ExpressionTree buildExpressionTree(Expr parseTree) {
        return mExpressionTreeFactory.makeExpressionTree(parseTree.build());
    }

    /**
     * @return An iterator that traverses all the terminals in {@code
     * inputExpression}.
     */
    private Iterator<Expr> makeIterator(Stack<Expr> stack,
                                        String inputExpression) {
        return new ExpressionIterator(this, stack, inputExpression);
    }
}

