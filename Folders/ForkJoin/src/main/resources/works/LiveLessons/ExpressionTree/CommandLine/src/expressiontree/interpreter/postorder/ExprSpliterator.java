package expressiontree.interpreter.postorder;

import expressiontree.interpreter.*;
import expressiontree.interpreter.exprs.*;

import java.util.Spliterators;
import java.util.Stack;
import java.util.function.Consumer;

/**
 * A simple spiterator that traverses through a user-supplied input
 * expression and returns each expression.
 */
class ExprSpliterator
      extends Spliterators.AbstractSpliterator<Expr> {
    /**
     * The interpreter implementation that's being traversed.
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
     * The stack that stores intermediate and final expression results.
     */
    private final Stack<Expr> mStack;

    /**
     * Constructor initializes the fields.
     *
     * @param inputExpression User-supplied input expression.
     */
    ExprSpliterator(InterpreterImpl iteratorImpl,
                    Stack<Expr> stack,
                    String inputExpression) {
        super(0, 0);
        // Store in fields.
        mInterpreterImpl = iteratorImpl;
        mStack = stack;
        mInputExpression = inputExpression;
    }

    /**
     * Processes the next expression in the input expression.
     *
     * @return false if no remaining elements existed upon entry to this method, else true.
     */
    @Override
    public boolean tryAdvance(Consumer<? super Expr> notUsed) {
        // Check to see if we're at the end of the input.
        if (mIndex >= mInputExpression.length())
            // Tell caller we're done.
            return false;
        else {
            // Get the next input character.
            char c = mInputExpression.charAt(mIndex++);

            // Skip over whitespace.
            while (Character.isWhitespace(c))
                c = mInputExpression.charAt(mIndex++);

            // Handle a variable or number.
            if (Character.isLetterOrDigit(c)) 
                mStack.push(makeNumber(mInputExpression,
                            mIndex - 1));
            else {
                Expr rightExpr = mStack.pop();
                switch (c) {
                    case '+':
                        // Addition operation.
                        mStack.push(new AddExpr(mStack.pop(),
                                                rightExpr));
                        break;
                    case '-':
                        // Subtraction operation.
                        mStack.push(new SubtractExpr(mStack.pop(),
                                                     rightExpr));
                        break;
                    case '~':
                        // Negate operation.
                        mStack.push(new NegateExpr(rightExpr));
                        break;
                    case '*':
                        // Multiplication operation.
                        mStack.push(new MultiplyExpr(mStack.pop(),
                                                     rightExpr));
                        break;
                    case '/':
                        // Division Operation.
                        mStack.push(new DivideExpr(mStack.pop(),
                                                   rightExpr));
                        break;
                    default:
                        throw new RuntimeException("invalid character: " + c);
                }
            }

            // Tell caller to keep trying.
            return true;
        }
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
