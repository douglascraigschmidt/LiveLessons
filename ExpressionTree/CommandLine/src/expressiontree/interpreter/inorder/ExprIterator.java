package expressiontree.interpreter.inorder;

import expressiontree.interpreter.InterpreterImpl;
import expressiontree.interpreter.exprs.*;

import java.util.Iterator;

import static expressiontree.composites.ComponentNode.sNUMBER;
import static expressiontree.composites.ComponentNode.sRPAREN;

/**
 * An iterator that traverses through a user-supplied input expression
 * and returns each symbol.
 */
class ExprIterator
      implements Iterator<Expr> {
    /**
     * Reference to the interpreter implementation that's currently
     * configured.
     */
    private final InterpreterImpl mInterpreterImpl;

    /**
     * The user-supplied input expression.
     */
    private final String mInputExpression;

    /**
     * The current location in mInputExpression.
     */
    private int mIndex;

    /**
     * The prior symbol (used to disambiguate unary and binary minus).
     */
    private Expr mPriorExpr;

    /**
     * Constructor initializes the fields.
     *
     * @param inputExpression User-supplied input expression.
     */
    ExprIterator(InterpreterImpl iteratorImpl,
                 String inputExpression) {
        // Store in the field.
        mInterpreterImpl = iteratorImpl;

        // Add the '$' delimiter to the end of the input.
        mInputExpression = inputExpression + '$';
    }

    /**
     * @return True if there are more symbols in the input, else false.
     */
    public boolean hasNext() {
        return mIndex < mInputExpression.length();
    }

    /**
     * @return The next symbol in the input expression.
     */
    public Expr next() {
        // Get the next input character.
        char c = mInputExpression.charAt(mIndex++);
 
        // Skip over whitespace.
        while (Character.isWhitespace(c))
            c = mInputExpression.charAt(mIndex++);

        Expr latestExpr = null;;

        // Handle a variable or number.
        if (Character.isLetterOrDigit(c)) {
            latestExpr = makeNumber(mInputExpression,
                                      mIndex - 1);
        } else {
            switch (c) {
            case '+':
                // Addition operation.
                latestExpr = new AddExpr();
                break;
            case '-':
                if (mPriorExpr != null
                    && (mPriorExpr.getType() == sNUMBER
                        || mPriorExpr.getType() == sRPAREN))
                    // Subtraction operation.
                    latestExpr = new SubtractExpr();
                else
                    // Negate operation.
                    latestExpr = new NegateExpr();
                break;
            case '*':
                // Multiplication operation.
                latestExpr = new MultiplyExpr();
                break;
            case '/':
                // Division Operation.
                latestExpr = new DivideExpr();
                break;
            case '(':
                // LParen.
                latestExpr = new LParen();
                break;
            case ')':
                // RParen
                latestExpr = new RParen();
                break;
            case '$':
                latestExpr = new Delimiter();
                break;
            default:
                throw new RuntimeException("invalid character: " + c);
            }
        }
        mPriorExpr = latestExpr;
        return latestExpr;
    }

    /**
     * Make a new Number symbol.
     */
    private Expr makeNumber(String input,
                              int startIndex) {
        // Merge all consecutive number chars into a single Number
        // symbol, e.g., "123" = int(123).
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
            // mSymbolTable.
            number = new NumberExpr(mInterpreterImpl.symbolTable()
                                .get(input.substring(startIndex,
                                                     startIndex + endIndex)));

        // Update mIndex skip past the #.
        mIndex = startIndex + endIndex;
        return number;
    }
}
