package expressiontree.interpreter;

import expressiontree.platspecs.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * This class stores variables and their values for use by the
 * Interpreter.  It plays the role of the "Context" in the
 * Interpreter pattern.
 */
public class SymbolTable {
    /**
     * Hash table containing variable names and values. 
     */
    private HashMap<String, Integer> mMap =
        new HashMap<>();

    /** 
     * Constructor.
     */
    SymbolTable() {
    }

    /**
     * ...
     */
    int get(String variable) {
        // If variable isn't set then assign it a 0 value.
        if(mMap.get(variable) != null)
            return mMap.get(variable);
        else {
            mMap.put(variable, 0);
            return mMap.get(variable);
        }
    }

    /**
     * Set the value of a variable. 
     */
    public void set(String variable, int value) {
        mMap.put(variable, value);
    }

    /** 
     * Print all variables and their values as an aid for
     * debugging.
     */
    public void print() {
        mMap.forEach((key, value) -> Platform.instance().outputLine((key
                + " = "
                + value)));
    }

    /** 
     * Clear all variables and their values. 
     */
    public void reset() {
        mMap.clear();
    }
}

