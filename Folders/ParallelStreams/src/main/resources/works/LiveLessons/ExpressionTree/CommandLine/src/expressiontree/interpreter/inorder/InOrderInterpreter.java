package expressiontree.interpreter.inorder;

import expressiontree.interpreter.*;
import expressiontree.interpreter.exprs.BinaryExpr;
import expressiontree.interpreter.exprs.Delimiter;
import expressiontree.interpreter.exprs.Expr;
import expressiontree.interpreter.exprs.NegateExpr;
import expressiontree.tree.ExpressionTree;
import expressiontree.tree.ExpressionTreeFactory;

import java.util.Iterator;
import java.util.Stack;

import static expressiontree.composites.ComponentNode.*;

/**
 * Parses incoming expression strings into a parse tree and builds an
 * expression tree from the parse tree.  This class plays the role of
 * the "Interpreter" in the Intepreter pattern, tweaked to use the
 * getPrecedence of operators/operands to guide the creation of the
 * parse tree.  It also uses the Builder pattern to build component
 * nodes in the Composite-based expression tree.
 */
public class InOrderInterpreter
       extends InterpreterImpl {
    /**
     * Constructor initializes the super class.
     */
    public InOrderInterpreter(ExpressionTreeFactory expressionTreeFactory) {
        super(expressionTreeFactory);
    }

    /**
     * This method first converts the user-supplier {@code
     * inputExpression} string into a parse tree and then build an
     * expression tree out of the parse tree.  It plays the role of
     * the template method in the Template Method pattern.
     */
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
     * Invoke a recursive build of the ExpressionTree, starting with
     * the root symbol.  The Builder pattern is used at each node to
     * create the appropriate subclass of {@code ComponentNode}.
     */
    private ExpressionTree buildExpressionTree(Expr parseTree) {
        return mExpressionTreeFactory.makeExpressionTree(parseTree.build());
    }

    /**
     * @return Return the root of a parse tree corresponding to the
     * {@code inputExpression}.
     */
    private Expr buildParseTree(String inputExpression) {
        Expr root = null;

        // This stack handles "reductions".
        Stack<Expr> mHandleStack = new Stack<>();

        // This stack keeps track of operator precedence.
        Stack<Expr> mOperatorStack = new Stack<>();

        // The operator stack always starts with a Delimiter symbol.
        mOperatorStack.push(new Delimiter());

        // Create an iterator for the inputExpression and iterate
        // through all the symbols.
        for (Iterator<Expr> iter = makeIterator(inputExpression);
             iter.hasNext(); ) {
            // Get the next Symbol from the user's input expression.
            Expr symbol = iter.next();

            // Determine the type of symbol.
            int symbolType = symbol.getType();
            // System.out.println("Symbol type = " + symbolType);

            // Numbers are pushed directly onto the handle stack.
            if (symbolType == sNUMBER) {
                // System.out.println("Number = " + ((Number) symbol).mItem);
                mHandleStack.push(symbol);
            }

            // If the operator on top of the stack is lower precedence
            // than the current operator symbol then push the symbol
            // on the stack.
            else if (mTopOfStackPrecedence[mOperatorStack.peek().getType()]
                     < mCurrentTokenPrecedence[symbolType])
                mOperatorStack.push(symbol);
            else {
                // As long as the operator on top of the stack is higher precedence
                // than the current operator symbol perform reductions.
                while (mTopOfStackPrecedence[mOperatorStack.peek().getType()]
                       > mCurrentTokenPrecedence[symbolType]) {
                    // Pop the top operator off the stack.
                    Expr temp = mOperatorStack.pop();

                    // A Negate operator symbol triggers unary reduction.
                    if (temp.getType() == sNEGATION) {
                        // Push the reduction onto the handle stack.
                        mHandleStack.push(synthesizeNode((NegateExpr) temp,
                                                         mHandleStack.pop()));
                    }
                    // Other symbols trigger a binary reduction.
                    else {
                        // Pop the top two items off the stack.
                        Expr rightChild = mHandleStack.pop();
                        Expr leftChild = mHandleStack.pop();

                        // Push the reduction onto the handle stack.
                        mHandleStack.push(synthesizeNode((BinaryExpr) temp,
                                                         leftChild,
                                                         rightChild));
                    }
                }

                // Get the type that's on top of the operator stack.
                int topSymbolType = mOperatorStack.peek().getType();

                // If all we have are matching Delimiters we are done.
                if (topSymbolType == sDELIMITER && symbolType == sDELIMITER) {
                    root = mHandleStack.pop();
                }
                // Remove a left-paren when its matching right-paren
                // is reached.
                else if (topSymbolType == sLPAREN
                         && symbolType == sRPAREN) {
                    mOperatorStack.pop();
                } else
                    // Push the symbol onto the top of the operator stack.
                    mOperatorStack.push(symbol);
            }            
        }

        return root;
    }

    /**
     *
     * @param binaryOperator
     * @param leftChild
     * @param rightChild
     * @return Symbol
     */
    private Expr synthesizeNode(BinaryExpr binaryOperator,
                                Expr leftChild,
                                Expr rightChild) {
        binaryOperator.mLeftExpr = leftChild;
        binaryOperator.mRightExpr = rightChild;
        return binaryOperator;
    }

    /**
     *
     * @param negateOperator
     * @param rightChild
     * @return Symbol
     */
    private Expr synthesizeNode(NegateExpr negateOperator,
                                Expr rightChild) {
        negateOperator.mRightExpr = rightChild;
        return negateOperator;
    }

    /**
     * @return An iterator that traverses all the terminals in {@code
     * inputExpression}.
     */
    private Iterator<Expr> makeIterator(String inputExpression) {
        return new ExprIterator(this, inputExpression);
    }
}

