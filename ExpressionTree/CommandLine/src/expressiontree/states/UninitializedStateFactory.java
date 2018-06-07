package expressiontree.states;

import java.util.HashMap;

/**
 * Implementation of a factory pattern that dynamically allocates the
 * appropriate {@code State} object.  This class is a variant of the
 * Abstract Factory pattern that has a set of related factory methods
 * but which doesn't use inheritance.
 */
class UninitializedStateFactory {
    /**
     * A HashMap that maps user format string requests to the
     * corresponding UninitializedState implementations.
     */
    private HashMap<String, State> mUninitializedStateMap =
        new HashMap<>();
	  	 	 
    /** 
     * Constructor.
     */
    UninitializedStateFactory() {
        mUninitializedStateMap.put
            ("in-order",
             new InOrderUninitializedState());
        mUninitializedStateMap.put
            ("pre-order",
             new PreOrderUninitializedState());
        mUninitializedStateMap.put
            ("post-order",
             new PostOrderUninitializedState());
        mUninitializedStateMap.put
            ("level-order",
             new LevelOrderUninitializedState());
    }
	  			
    /** 
     * Dynamically allocate a new {@code State} object based on the
     * designated {@code format}.
     */
    State makeUninitializedState(String formatRequest) {
        // Try to find the pre-allocated UninitializedState
        // implementation.
        State state = mUninitializedStateMap.get(formatRequest);
	  				
        if(state != null)
            // If we find it then return it. 
            return state;
        else 
            // Otherwise, the user gave an unknown request, so
            // throw an exception.
            throw new IllegalArgumentException(formatRequest 
                                               + " is not a supported format");
    }
}
