package expressiontree.interpreter;

import expressiontree.nodes.*;
import expressiontree.platspecs.Platform;
import expressiontree.tree.ExpressionTree;
import expressiontree.tree.ExpressionTreeFactory;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

/**
 * Parses incoming expression strings into a parse tree and builds an
 * expression tree from the parse tree.  This class plays the role of
 * the "Interpreter" in the Intepreter pattern, tweaked to use the
 * mPrecedence of operators/operands to guide the creation of the parse
 * tree.  It also uses the Builder pattern to build the component
 * nodes in the Composite-based expression tree.
 */
public class Interpreter {
    /** 
     * Stores numbers with multiple digits. 
     */
    private int mMultiDigitNumbers;

    /**
     * Stores the previous symbol. 
     */
    private Symbol mLastValidInput;

    /** 
     * Tracks the mPrecedence of the expression.
     */
    private int mAccumulatedPrecedence;

    /** 
     * The mPrecedence of the '+' and '-' operators.
     */
    final static int mAddSubPrecedence = 1;

    /** 
     * The mPrecedence of the '*' and '/' operators.
     */
    final static int mMulDivPrecedence = 2;

    /** 
     * The mPrecedence of a '-' (negate) operator.
     */
    final static int mNegatePrecedence = 3;

    /** 
     * The mPrecedence of a number.
     */
    final static int mNumberPrecedence = 4;

    /**
     * The mPrecedence of a number.
     */
    final static int mParenPrecedence = 5;

    /** 
     * Factory that makes an expression tree. 
     */
    private ExpressionTreeFactory mExpressionTreeFactory;

    /**
     * Stores variables and their values for use by the Interpreter.
     */
    private SymbolTable mSymbolTable =
        new SymbolTable();

    /** 
     * Accessor method. 
     */
    public SymbolTable symbolTable() {
        return mSymbolTable;
    }

    /**
     * Provide a default implementation of the @a
     * ExpressionTreeFactory.
     */
    public Interpreter() {
        mExpressionTreeFactory =
            new ExpressionTreeFactory();
    }

    /**
     * Provide the means to override the default @a
     * ExpressionTreeFactory.
     */
    public Interpreter(ExpressionTreeFactory expressionTreeFactory) {
        mExpressionTreeFactory = expressionTreeFactory;
    }

    /**
     * This method first converts a string into a parse tree and then
     * build an expression tree out of the parse tree.  It's
     * implemented using the Template Method pattern.
     */
    public ExpressionTree interpret(String inputExpression) {
        // The parseTree is implemented as a Stack.
        Stack<Symbol> parseTree =
            buildParseTree(inputExpression);

        // If the parseTree has an element in it perform the
        // (optional) optimization pass and then build the
        // ExpressionTree fro the parseTree.
        if (!parseTree.empty()) {
            // This hook method can be overridden to optimize the
            // parseTree prior to generating the ExpressionTree.
            optimizeParseTree(parseTree);

            // Build the ExpressionTree from the parseTree.
            return buildExpressionTree(parseTree);
        } else
            // If we reach this, we didn't have any symbols.
            return mExpressionTreeFactory
                .makeExpressionTree(null);
    }

    /**
     * ...
     */
    private Stack<Symbol> buildParseTree(String inputExpression) {
        Stack<Symbol> parseTree = new Stack<>();

        // Keep track of the last valid mInput, which is useful for
        // handling unary minus (negation) operators.
        mLastValidInput = null;

        boolean handled = false;

        // Initialize some fields to their default values.
        mAccumulatedPrecedence = 0;
        mMultiDigitNumbers = 0;

        for(int index = 0;
            index < inputExpression.length();
            ++index) {
            // Locate the next symbol in the mInput and place it into
            // the parse tree according to its mPrecedence.
            parseTree = parseNextSymbol(inputExpression,
                                        index,
                                        handled,
                                        parseTree);

            if(mMultiDigitNumbers > index)
                index = mMultiDigitNumbers;
        }

        return parseTree;
    }

    /**
     * This hook method can be overridden to conduct optimization on
     * the {@code parseTree} prior to generating the @a
     * ExpressionTree.  By default it's a no-op.
     */
    private void optimizeParseTree(Stack<Symbol> parseTree) {
    }

    /** 
     * Invoke a recursive build of the ExpressionTree, starting with
     * the root symbol, which should be the one and only item in the
     * linked list.  The Builder pattern is used at each node to
     * create the appropriate subclass of {@code ComponentNode}.
     */
    private ExpressionTree buildExpressionTree(Stack<Symbol> parseTree) {
        // There had better only be one element mLeft in the stack!
        assert (parseTree.size() == 1);
        return mExpressionTreeFactory.makeExpressionTree(parseTree.peek().build());
    }

    /** 
     * Parse next terminal expression. 
     */
    private Stack<Symbol> parseNextSymbol(String inputExpression,
                                          int index,
                                          boolean handled,
                                          Stack<Symbol> parseTree) {
        handled = false;
        if (Character.isDigit(inputExpression.charAt(index))) {
            handled = true;
            parseTree = insertNumberOrVariable(inputExpression,
                                               index,
                                               parseTree,
                                               false);
        } else if (Character.isLetterOrDigit(inputExpression.charAt(index))) {
            handled = true;
            parseTree = insertNumberOrVariable(inputExpression, 
                                               index,
                                               parseTree,
                                               true);
        } else if (inputExpression.charAt(index) == '+') {
            handled = true;
            // Addition operation. 
            Add op = new Add();
            op.addPrecedence(mAccumulatedPrecedence);

            mLastValidInput = null;

            // Insert the op according to mLeft-to-mRight relationships.
            parseTree = insertSymbolByPrecedence(op, parseTree);
        } else if(inputExpression.charAt(index) == '-') {
            handled = true;

            Symbol op = null;
            // Subtraction operation.
            Number number = null;

            if (mLastValidInput == null) {
                // Negate. 
                op = new Negate();
                op.addPrecedence(mAccumulatedPrecedence);
            } else {
                // Subtract.
                op = new Subtract();
                op.addPrecedence(mAccumulatedPrecedence);
            }

            mLastValidInput = null;

            // Insert the op according to mLeft-to-mRight
            // relationships.
            parseTree = insertSymbolByPrecedence(op, parseTree);
        } else if(inputExpression.charAt(index) == '*') {
            handled = true;
            // Multiplication operation. 
            Multiply op = new Multiply();
            op.addPrecedence(mAccumulatedPrecedence);

            mLastValidInput = null;

            // Insert the op according to mPrecedence
            // relationships. 
            parseTree = insertSymbolByPrecedence(op, parseTree);
        }
        else if(inputExpression.charAt(index) == '/') {
            handled = true;
            // Division Operation. 
            Divide op = new Divide();
            op.addPrecedence(mAccumulatedPrecedence);

            mLastValidInput = null;

            // Insert the op according to mPrecedence relationships.
            parseTree = insertSymbolByPrecedence(op, parseTree);
        }
        else if(inputExpression.charAt(index) == '(') {
            handled = true;
            parseTree = handleParentheses(inputExpression,
                                          index,
                                          handled,
                                          parseTree);
        }
        else if(inputExpression.charAt(index)== ' ' 
                || inputExpression.charAt(index) == '\n') 
            // Skip whitespace. 
            handled = true;

        return parseTree;
    }

    /** 
     * Inserts a @a Number into the parse tree. 
     */
    private Stack<Symbol> insertNumberOrVariable(String input,
                                                 int startIndex,
                                                 Stack<Symbol> parseTree,
                                                 boolean isVariable)  {
        // Merge all consecutive number chars into a single Number
        // symbol, eg '123' = int (123). Scope of endIndex must be
        // outside of the for loop.
        int endIndex = 1;

        // Explanation for this additional if statement: if mInput = 1,
        // an out of bounds exception is thrown as a result of the
        // charAt() statement.
        if (input.length() > startIndex + endIndex)
            // Locate the end of the number.
            for(;
                startIndex + endIndex < input.length () 
                    && Character.isDigit(input.charAt(startIndex + endIndex)); 
                ++endIndex)
                continue;

        Number number;

        if (isVariable)
            // Lookup the value in the mSymbolTable.
            number =
                new Number(mSymbolTable.get(input.substring(startIndex,
                                                           startIndex 
                                                           + endIndex)));
        else
            number =
                new Number(input.substring(startIndex,
                                           startIndex + endIndex));

        number.addPrecedence(mAccumulatedPrecedence);

        mLastValidInput = number;

        // Update startIndex to the last character that was a number.
        // The ++startIndex will update the startIndex at the end of
        // the loop to the next check.
        startIndex += endIndex - 1;
        mMultiDigitNumbers = startIndex;

        return insertSymbolByPrecedence(number, parseTree);
    }

    /** 
     * Inserts a {@code Symbol} into the parse tree.
     */
    private Stack<Symbol> insertSymbolByPrecedence(Symbol symbol,
                                                   Stack<Symbol> parseTree) {
        if (!parseTree.empty()) {
            // If last element was a number, then make that our mLeft.
            Symbol parent = parseTree.peek();
            Symbol child = parent.mRight;

            if (child != null)
                // While there is a child of parent, keep going
                // down the mRight side.
                for (;
                    child != null 
                        && child.precedence () < symbol.precedence ();
                    child = child.mRight)
                    parent = child;

            if (parent.precedence () < symbol.precedence()) {
                // symbol mLeft will be the old child. new parent child
                // will be the symbol.  To allow infinite negations,
                // we have to check for unaryoperator.
                if (symbol.mLeft == null)
                    symbol.mLeft = child;

                parent.mRight = symbol;
            } else {
                // This can be one of two things, either we are the
                // same mPrecedence or we are less mPrecedence than the
                // parent.  This also means different things for unary
                // ops.  The most recent unary op (negate) has a
                // higher mPrecedence.
                UnaryOperator up = new Negate();

                if (symbol.getClass() == up.getClass()) {
                    for(;
                        child != null 
                        && child.precedence() == symbol.precedence();
                        child = child.mRight)
                        parent = child;

                    parent.mRight = symbol;
                } else {
                    // Everything else is evaluated the same. For
                    // instance, if this is 5 * 4 / 2, and we
                    // currently have Mult(5,4) in the parseTree, we
                    // need to make parent our mLeft child.
                    symbol.mLeft = parent;
                    parseTree.pop();
                    parseTree.push(symbol);
                    parent = child;
                }
            }
        } else
            parseTree.push(symbol);

        return parseTree;
    }

    /**
     *
     */
    private Stack<Symbol> handleParentheses(String inputExpression,
                                            int index,
                                            boolean handled,
                                            Stack<Symbol> masterParseTree) {
        // Handling parentheses is a lot like handling the original
        // interpret() call.  The difference is that we have to worry
        // about how the calling function has its masterParseTree
        // setup.
        mAccumulatedPrecedence += mParenPrecedence;
        Stack<Symbol> localParseTree = new Stack<>();

        handled = false;
        for(++index; 
            index < inputExpression.length(); 
            ++index) {
            localParseTree = parseNextSymbol(inputExpression,
                                             index,
                                             handled,
                                             localParseTree);

            if (mMultiDigitNumbers > index)
                index = mMultiDigitNumbers;

            if (inputExpression.charAt(index) == ')') {
                handled = true;
                mAccumulatedPrecedence -= mParenPrecedence;
                break;
            }
        }

        if (masterParseTree.size() > 0 
            && localParseTree.size() > 0) {
                Symbol op = masterParseTree.peek();

                // Is it a node with 2 children?
                if (op != null)
                    masterParseTree = 
                        insertSymbolByPrecedence(localParseTree.peek(),
                                                 masterParseTree);
                else if (op == null)
                    // Is it a unary node (like negate)? 
                    masterParseTree = 
                        insertSymbolByPrecedence(localParseTree.peek(),
                                                 masterParseTree);
                else {
                    // Is it a terminal node (Number)?  If so, there's
                    // an error.
                }
	    }
        else if (localParseTree.size () > 0)
            masterParseTree = localParseTree;

        return masterParseTree;
    }
}

