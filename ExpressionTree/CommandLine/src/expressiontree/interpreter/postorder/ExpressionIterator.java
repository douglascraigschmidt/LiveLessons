package expressiontree.interpreter.postorder;

import expressiontree.interpreter.InterpreterImpl;

import java.util.Iterator;
import java.util.Stack;

/**
 * An iterator that traverses through a user-supplied input expression
 * and returns each expression.
 */
class ExpressionIterator
      implements Iterator<Expr> {
    /**
     *
     */
    private InterpreterImpl mInterpreterImpl;

    /**
     * The user-supplied input expression.
     */
    private String mInputExpression;

    /**
     * The current location in mInputExpression.
     */
    private int mIndex;

    /**
     *
     */
    private Stack<Expr> mStack;

    /**
     * The prior expression (used to disambiguate unary and binary
     * minus).
     */
    private Expr mPriorExpression;

    /**
     * Constructor initializes the fields.
     *
     * @param inputExpression User-supplied input expression.
     */
    ExpressionIterator(InterpreterImpl iteratorImpl,
                       Stack<Expr> stack,
                       String inputExpression) {
        // Store in fields.
        mInterpreterImpl = iteratorImpl;
        mStack = stack;
        mInputExpression = inputExpression;
    }

    /**
     * @return True if there are more expressions in the input, else false.
     */
    public boolean hasNext() {
        return mIndex < mInputExpression.length();
    }

    /**
     * @return The next expression in the input expression.
     */
    public Expr next() {
        // Get the next input character.
        char c = mInputExpression.charAt(mIndex++);
 
        // Skip over whitespace.
        while (Character.isWhitespace(c))
            c = mInputExpression.charAt(mIndex++);

        Expr latestExpression = null;;

        // Handle a variable or number.
        if (Character.isLetterOrDigit(c)) {
            latestExpression = makeNumber(mInputExpression,
                                          mIndex - 1);
        } else {
            Expr rightExpression = mStack.pop();
            switch (c) {
            case '+':
                // Addition operation.
                latestExpression =
                    new AddExpr(mStack.pop(),
                                rightExpression);
                break;
            case '-':
                // Subtraction operation.
                latestExpression = new SubtractExpr(mStack.pop(),
                                                    rightExpression);
                break;
            case '~':
                // Negate operation.
                latestExpression = new NegateExpr(rightExpression);
                break;
            case '*':
                // Multiplication operation.
                latestExpression = 
                    new MultiplyExpr(mStack.pop(),
                                     rightExpression);
                break;
            case '/':
                // Division Operation.
                latestExpression = 
                    new DivideExpr(mStack.pop(),
                                   rightExpression);
                break;
            default:
                throw new RuntimeException("invalid character: " + c);
            }
        }

        mPriorExpression = latestExpression;
        return latestExpression;
    }

    /**
     * Make a new Number expression.
     */
    private Expr makeNumber(String input,
                            int startIndex) {
        // Merge all consecutive number chars into a single Number
        // expression, e.g., "123" = int(123).
        int endIndex = 1;

        // If input == 1, an out of bounds exception is thrown as a
        // result of the charAt() statement.
        if (input.length() > startIndex + endIndex)
            // Locate the end of the number.
            for(;
                startIndex + endIndex < input.length ()
                    && Character.isDigit(input.charAt(startIndex + endIndex));
                ++endIndex)
                //noinspection UnnecessaryContinue
                continue;

        NumberExpr number;

        // Handle a number.
        if (Character.isDigit(input.charAt(startIndex)))
            number = new NumberExpr(input.substring(startIndex,
                                                    startIndex + endIndex));
        else
            // Handle a variable by looking up its value in
            // mExpressionTable.
            number = new NumberExpr(mInterpreterImpl
                                    .symbolTable()
                                    .get(input.substring(startIndex,
                                                         startIndex + endIndex)));

        // Update mIndex skip past the #.
        mIndex = startIndex + endIndex;
        return number;
    }
}
