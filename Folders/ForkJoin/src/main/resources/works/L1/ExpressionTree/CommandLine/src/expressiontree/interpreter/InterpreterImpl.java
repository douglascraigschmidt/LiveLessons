package expressiontree.interpreter;

import expressiontree.tree.ExpressionTree;
import expressiontree.tree.ExpressionTreeFactory;

public abstract class InterpreterImpl {
    /** 
     * Factory that makes an expression tree. 
     */
    protected ExpressionTreeFactory mExpressionTreeFactory;

    /**
     * Stores variables and their values for use by the Interpreter.
     */
    private SymbolTable mSymbolTable = new SymbolTable();

    /** 
     * Accessor method. 
     */
    public SymbolTable symbolTable() {
        return mSymbolTable;
    }

    /**
     * Constructor initializes the field.
     */
    public InterpreterImpl(ExpressionTreeFactory expressionTreeFactory) {
        mExpressionTreeFactory = expressionTreeFactory;
    }

    /**
     *
     * @param inputExpression
     * @return
     */
    public abstract ExpressionTree interpret(String inputExpression);
}
