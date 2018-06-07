package expressiontree.tree;

import expressiontree.interpreter.Interpreter;
import expressiontree.states.State;
import expressiontree.states.UninitializedState;

/**
 * Plays the role of the "Context" in the State pattern that ensures
 * user operations on an expression tree are invoked according to the
 * correct protocol.  Most of its methods delegate to the
 * corresponding methods in subclasses of the {@code State} base
 * class, which then perform the requested operations.
 */
public class TreeContext {
    /**
     * Keep track of the current mState of the sequence of user
     * operations.
     */
    private State mState;
    
    /**
     * The current {@code ExpressionTree}.
     */
    private ExpressionTree mTree;

    /**
     * Returns whether or not a successful format call has been
     * called.
     */
    private boolean mFormatted;

    /** 
     * The mInterpreter used to parse and process user expression
     * mInput.
     */
    private Interpreter mInterpreter;

    /** 
     * Accessor for the mInterpreter.
     */
    public Interpreter interpreter() {
        return mInterpreter;
    }

    /**
     * Constructor.
     */
    public TreeContext() {
        // Start out in the uninitialized state.
        mState = new UninitializedState();

        // Formatting has not been performed (yet).
        mFormatted = false;

        // Create a no-op tree.
        mTree = new ExpressionTree(null);
    }

    /**
     * Set the format to the given {@code format}.
     */
    public void format(String format) {
        // Create appropriate type of interpreter for input format.
        mInterpreter = new Interpreter(format);

        // Delegate to the appropriate state handler.
        mState.format(this, format);

        // Formatting has been performed.
        mFormatted = true;
    }

    /**
     * Make an expression tree based on the designated {@code
     * expression} using the previously designated format.
     */
    public void makeTree(String expression) {
        mState.makeTree(this, expression);
    }

    /**
     * Print the most recently created expression tree using the
     * designated {@code format}.
     */
    public void print(String format) {
        mState.print(this, format);
    }

    /**
     * Evaluate the "yield" of the most recently created expression
     * tree using the designated {@code format}.
     */
    public void evaluate(String format) {
        mState.evaluate(this, format);
    }

    /** 
     * Sets the variable to its corresponding value. 
     */
    public void set(String keyValuePair) {
        // Get rid of all spaces. 
        String inputString = keyValuePair.replaceAll(" ", "");
        
        // Keep track of found characters.
        int pos;

        // Inputstring should be key=value. 
        if((pos = inputString.indexOf('=')) != -1) {
            // If the position is not the first char (e.g., '=value')
            // and position is not the last char (e.g., 'key=') then
            // split the string and set symbol table in mInterpreter.
            if(pos != 0 && pos < inputString.length() - 1) {
                String key = inputString.substring(0, pos);
                String value = inputString.substring(pos + 1);

                mInterpreter.symbolTable().set(key,
                                               Integer.parseInt(value));
            } else
                throw new RuntimeException("Must be in the form key=value");
        } else
            throw new RuntimeException("Must have = sign present");
    }

    /** 
     * Return a pointer to the current {@code State}.
     */
    State state() {
        return mState;
    }

    /**
     * Set the current {@code State} to the designated {@code
     * newstate}.
     */
    public void state(State newState) {
        this.mState = newState;
    }

    /** 
     * Return the current {@code ExpressionTree} owned by this object.
     */
    public ExpressionTree tree() {
        return mTree;
    }

    /** 
     * Set the current {@code ExpressionTree} to {@code newTree}.
     */
    public void tree(ExpressionTree newTree) {
        mTree = newTree;
    }

    /**
     * Returns whether or not a successful format call has been
     * called.
     */
    public final boolean formatted() {
        return mFormatted;
    }
}
