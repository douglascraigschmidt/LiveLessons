package expressiontree.interpreter;

import expressiontree.platspecs.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores variables and their values used by the Interpreter.  It
 * plays the role of the "Context" in the Interpreter pattern.
 */
public class SymbolTable {
    /**
     * Hash table containing variable names and values. 
     */
    private HashMap<String, Integer> mMap =
        new HashMap<>();

    /**
     * @return The int value of {@code variable} stored in the table
     * or 0 if {@code variable} isn't present yet.
     */
    public int get(String variable) {
        Integer value = mMap.get(variable);
        // If variable isn't set then assign it a 0 value.
        if(value != null)
            return value;
        else {
            mMap.put(variable, 0);
            return 0;
        }
    }

    /**
     * Set the value of a variable. 
     */
    public void set(String variable, int value) {
        mMap.put(variable, value);
    }

    /** 
     * Print all variables and their values as an aid for debugging.
     */
    public void print() {
        mMap.forEach((key, value)
                     -> Platform.instance().outputLine((key
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

