package expressiontree.interpreter.inorder;

import expressiontree.interpreter.InterpreterImpl;

import java.util.Iterator;

import static expressiontree.nodes.ComponentNode.sNUMBER;
import static expressiontree.nodes.ComponentNode.sRPAREN;

/**
 * An iterator that traverses through a user-supplied input expression
 * and returns each symbol.
 */
class SymbolIterator
      implements Iterator<Symbol> {
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
     * The prior symbol (used to disambiguate unary and binary
     * minus).
     */
    private Symbol mPriorSymbol;

    /**
     * Constructor initializes the fields.
     *
     * @param inputExpression User-supplied input expression.
     */
    SymbolIterator(InterpreterImpl iteratorImpl,
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
    public Symbol next() {
        // Get the next input character.
        char c = mInputExpression.charAt(mIndex++);
 
        // Skip over whitespace.
        while (Character.isWhitespace(c))
            c = mInputExpression.charAt(mIndex++);

        Symbol latestSymbol = null;;

        // Handle a variable or number.
        if (Character.isLetterOrDigit(c)) {
            latestSymbol = makeNumber(mInputExpression,
                                      mIndex - 1);
        } else {
            switch (c) {
            case '+':
                // Addition operation.
                latestSymbol = new Add();
                break;
            case '-':
                if (mPriorSymbol != null
                    && (mPriorSymbol.getType() == sNUMBER
                        || mPriorSymbol.getType() == sRPAREN))
                    // Subtraction operation.
                    latestSymbol = new Subtract();
                else
                    // Negate operation.
                    latestSymbol = new Negate();
                break;
            case '*':
                // Multiplication operation.
                latestSymbol = new Multiply();
                break;
            case '/':
                // Division Operation.
                latestSymbol = new Divide();
                break;
            case '(':
                // LParen.
                latestSymbol = new LParen();
                break;
            case ')':
                // RParen
                latestSymbol = new RParen();
                break;
            case '$':
                latestSymbol = new Delimiter();
                break;
            default:
                throw new RuntimeException("invalid character: " + c);
            }
        }
        mPriorSymbol = latestSymbol;
        return latestSymbol;
    }

    /**
     * Make a new Number symbol.
     */
    private Symbol makeNumber(String input,
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

        Number number;

        // Handle a number.
        if (Character.isDigit(input.charAt(startIndex)))
            number = new Number(input.substring(startIndex,
                                                startIndex + endIndex));
        else
            // Handle a variable by looking up its value in
            // mSymbolTable.
            number = new Number(mInterpreterImpl.symbolTable()
                                .get(input.substring(startIndex,
                                                     startIndex + endIndex)));

        // Update mIndex skip past the #.
        mIndex = startIndex + endIndex;
        return number;
    }
}
