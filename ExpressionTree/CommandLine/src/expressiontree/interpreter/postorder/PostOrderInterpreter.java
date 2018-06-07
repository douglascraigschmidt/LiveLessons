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
        Stack<Expression> stack = new Stack<>();

        // Create an iterator for the inputExpression and iterate
        // through all the expressions.
        for (Iterator<Expression> iter = makeIterator(stack, inputExpression);
             iter.hasNext(); ) {
            // Get next Expression from the user's input expression.
            Expression expression = iter.next();
        
            stack.push(expression);
        }
        System.out.println("Result: "+ stack.peek().interpret());
        return mExpressionTreeFactory.makeExpressionTree(stack.pop().build());
    }

    /**
     * @return An iterator that traverses all the terminals in {@code
     * inputExpression}.
     */
    private Iterator<Expression> makeIterator(Stack<Expression> stack,
                                              String inputExpression) {
        return new ExpressionIterator(this, stack, inputExpression);
    }
}

