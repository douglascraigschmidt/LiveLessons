package com.example.expressiontree;

import java.util.Iterator;
import java.util.HashMap;

/**
 * @class IteratorFactory
 * 
 * @brief Implementation of the Factory Method pattern that
 *        dynamically allocates the appropriate @a Iterator strategy
 *        requested by a caller.  This variant of the pattern doesn't
 *        use inheritance, so it plays the role of the ConcreteCreator
 *        in the Factory Method pattern.
 */
public class IteratorFactory {
    /** 
     * This interface uses the Command pattern to create @a Iterator
     * implementations at runtime.
     */
    public static interface IIteratorFactoryCommand {
        public Iterator<ExpressionTree> execute(ExpressionTree tree);
    }

    /**
     * Map used to validate input requests for @a Iterator
     * implementations and dispatch the execute() method of the
     * requested iterator. .
     */
    private HashMap<String, IIteratorFactoryCommand> traversalMap = 
        new HashMap<String, IIteratorFactoryCommand>();
	
    /** Ctor */
    public IteratorFactory() {
        /**
         * The IteratorFactory maps strings to an interface capable of
         * building the appropriate @a Iterator implementation at
         * runtime.
         */

    	/** 
         * An "in-order" string maps to a command object that creates
         * an @a InOrderIterator implementation.
         */
        traversalMap.put("in-order", new IIteratorFactoryCommand() {
                public Iterator<ExpressionTree> execute(ExpressionTree tree) {
                    return new InOrderIterator(tree);
                }
            });
            
    	/** 
         * A "pre-order" string maps to a command object that creates
         * a @a PreOrderIterator implementation.
         */
        traversalMap.put("pre-order", new IIteratorFactoryCommand() {
                public Iterator<ExpressionTree> execute(ExpressionTree tree) {
                    return new PreOrderIterator(tree);
                }
            });
            
    	/** 
         * A "post-order" string maps to a command object that creates
         * a @a PostOrderIterator implementation.
         */
        traversalMap.put("post-order", new IIteratorFactoryCommand() {
                public Iterator<ExpressionTree> execute(ExpressionTree tree) {
                    return new PostOrderIterator(tree);
                }
            });
            
    	/** 
         * A "level-order" string maps to a command object that
         * creates a @a LevelOrderIterator implementation.
         */
        traversalMap.put("level-order", new IIteratorFactoryCommand() {
                public Iterator<ExpressionTree> execute(ExpressionTree tree) {
                    return new LevelOrderIterator(tree);
                }
            });  
    }
	
    /** 
     * Create a new @a Iterator implementation based on the caller's
     * designated @a traversalOrderRequest.
     */
    public Iterator<ExpressionTree> makeIterator(ExpressionTree tree,
                                                 String traversalOrderRequest) {
        if (traversalOrderRequest.equals(""))
            /** 
             * Default to in-order if user doesn't explicitly request
             * a traversal order.
             */
            traversalOrderRequest = "in-order";

        /** Try to find the pre-allocated factory command. */
        IIteratorFactoryCommand command =
            traversalMap.get(traversalOrderRequest);

        if(command != null)
            /** If we find it then execute it. */
            return command.execute(tree);
        else
            /** 
             * Otherwise, the user gave an unknown request, so throw
             * an exception.
             */
            throw new IllegalArgumentException
                (traversalOrderRequest 
                 + " is not a supported traversal order");
    }
}
	
	
	
