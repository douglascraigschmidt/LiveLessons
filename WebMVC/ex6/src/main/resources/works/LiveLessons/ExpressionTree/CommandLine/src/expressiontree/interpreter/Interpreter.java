package expressiontree.interpreter;

import expressiontree.interpreter.inorder.InOrderInterpreter;
import expressiontree.interpreter.postorder.PostOrderInterpreter;
import expressiontree.tree.ExpressionTree;
import expressiontree.tree.ExpressionTreeFactory;

/**
 * Parses incoming expression strings into a parse tree and builds an
 * expression tree from the parse tree.  This class plays the role of
 * the "Interpreter" in the Intepreter pattern, tweaked to use the
 * getPrecedence of operators/operands to guide the creation of the parse
 * tree.  It also uses the Builder pattern to build component nodes in
 * the Composite-based expression tree.
 */
public class Interpreter {
    /**
     * Reference to the implementor hierarchy.
     */
    private InterpreterImpl mInterpreterImpl;

    /**
     * Constructor creates a new interpreter that handles user input
     * supplied via the given {@code format} using a default {@code
     * ExpressionTreeFactory} implementation.
     */
    public Interpreter(String format) {
        this(format, new ExpressionTreeFactory());
    }

    /**
     * Constructor creates a new interpreter that handles user input
     * supplied via the given {@code format} using the given {@code
     * ExpressionTreeFactory} implementation.
     */
    public Interpreter(String format,
                       ExpressionTreeFactory expressionTreeFactory) {
        if (format.equals(""))
            // Default to in-order if user doesn't explicitly
            // request a format order.
            format = "in-order";

        switch (format) {
            case "in-order":
                mInterpreterImpl = new InOrderInterpreter(expressionTreeFactory);
                break;
            case "post-order":
                mInterpreterImpl = new PostOrderInterpreter(expressionTreeFactory);
                break;
            default:
                throw new IllegalArgumentException(format
                        + " is not a supported format");
        }
    }

    /**
     * This method converts the user-supplier {@code inputExpression}
     * string into an expression tree.  
     */
    public ExpressionTree interpret(String inputExpression) {
        return mInterpreterImpl.interpret(inputExpression);
    }

    /**
     * @return Return the symbol table.
     */
    public SymbolTable symbolTable() {
        return mInterpreterImpl.symbolTable();
    }
}

