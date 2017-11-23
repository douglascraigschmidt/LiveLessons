package com.example.expressiontree;

import com.example.expressiontree.State.UninitializedState;
import com.example.expressiontree.Interpreter.SymbolTable;

/**
 * @class TreeOps
 *
 * @brief Plays the role of the "Context" in the State pattern that
 *        ensures user operations on an expression tree are invoked
 *        according to the correct protocol.  Most of its methods
 *        delegate to the corresponding methods in sublcasses of
 *        the @a State base class, which then perform the requested
 *        operations.
 */
public class TreeOps {
    /**
     * Keep track of the current state of the sequence of user
     * operations.
     */
    private State state;
    
    /** The current @a ExpressionTree. */
    private ExpressionTree tree;

    /**
     * Returns whether or not a successful format call has been
     * called.
     */
    private boolean formatted;

    /** 
     * The interpreter used to parse and process user expression
     * input.
     */
    private Interpreter interpreter;

    /** Accessor for the interpreter. */
    public Interpreter interpreter() {
        return interpreter;
    }

    /** Ctor */
    public TreeOps() {
        state = new UninitializedState();
        formatted = false;
        interpreter = new Interpreter();
        tree = new ExpressionTree(null);
    }

    /** Set the desired format to the designated @a newformat. */
    public void format(String newformat) {
        state.format(this, newformat);
        this.formatted = true;
    }

    /**
     * Make an expression tree based on the designated @a expression
     * using the previously designated format.
     */
    public void makeTree(String expression) {
        state.makeTree(this, expression);
    }

    /**
     * Print the most recently created expression tree using the
     * designated @a format.
     */
    public void print(String format) {
        state.print(this, format);
    }

    /**
     * Evaluate the "yield" of the most recently created expression
     * tree using the designated @a format.
     */
    public void evaluate(String format) {
        state.evaluate(this, format);
    }

    /** Sets the variable to its corresponding value. */
    public void set(String keyValuePair) throws Exception {
        /** Get rid of all spaces. */
        String inputString = keyValuePair.replaceAll(" ", "");
        
        /** Keep track of found characters. */
        int pos;

        /** Inputstring should be key=value. */
        if((pos = inputString.indexOf('=')) != -1) {
            /**
             * If the position is not the first char (e.g.,
             * '=value') and position is not the last char (e.g.,
             * 'key=') then split the string and set the symbol
             * table in the interpreter accordingly.
             */
            if(pos != 0 && pos < inputString.length() - 1) {
                String key = inputString.substring(0, pos);
                String value = inputString.substring(pos + 1);

                interpreter.symbolTable().set(key,
                                              Integer.parseInt(value));
            } else
                throw new Exception("Must be in the form key=value");
        } else
            throw new Exception("Must have = sign present");
    }

    /** Return a pointer to the current @a State. */
    State state() {
        return state;
    }

    /**
     * Set the current @a State to the designated @a
     * newstate pointer.
     */
    void state(State newState) {
        this.state = newState;
    }

    /** 
     * Return the current @a ExpressionTree that's owned by this
     * object.
     */
    ExpressionTree tree() {
        return tree;
    }

    /** Set the current @a ExpressionTree to @a newtree.*/
    void tree(ExpressionTree newTree) {
        tree = newTree;
    }

    /**
     * Returns whether or not a successful format call has been
     * called.
     */
    public final boolean formatted() {
        return formatted;
    }
}
