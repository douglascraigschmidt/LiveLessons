package com.example.expressiontree;

import java.util.HashMap;

/**
 * @class VisitorFactory
 * 
 * @brief Implementation of the Factory Method pattern that
 *        dynamically allocates the appropriate @a Visitor object
 *        requested by a caller.  This variant of the pattern doesn't
 *        use inheritance, so it plays the role of the ConcreteCreator
 *        in the Factory Method pattern.
 */
public class VisitorFactory {
    /** 
     * This interface uses the Command pattern to create @a Visitor
     * implementations at runtime.
     */
    private static interface IVisitorFactoryCommand {
        public Visitor execute();
    }

    /**
     * Map used to validate input requests for @a Visitor
     * implementations and dispatch the execute() method of the
     * requested visitor.
     */
    private HashMap<String, IVisitorFactoryCommand> visitorMap = 
        new HashMap<String, IVisitorFactoryCommand>();
	
    /** Ctor */
    public VisitorFactory() {
        /**
         * The VisitorFactory maps strings to an interface capable of
         * building the appropriate @a Visitor implementation at
         * runtime.
         */

    	/** 
         * An "eval" string maps to a command object that creates
         * an @a EvaluationVisitor implementation.
         */
        visitorMap.put("eval", new IVisitorFactoryCommand() {
                public Visitor execute() {
                    return new EvaluationVisitor();
                }
            });
            
        /**
         * A "print" string maps to a command object that creates
         * an @a PrintVisitor implementation.
         */
        visitorMap.put("print", new IVisitorFactoryCommand() {
                public Visitor execute() {
                    return new PrintVisitor();
                }
            });
    }
	
    /** 
     * Create a new @a Visitor object based on the caller's
     * designated @a visitorRequest.
     */
    public Visitor makeVisitor(String visitorRequest) {
        /** Try to find the pre-allocated factory command. */
        IVisitorFactoryCommand command =
            visitorMap.get(visitorRequest);

        if(command != null)
            /** If we find it then execute it. */
            return command.execute();
        else
            /** 
             * Otherwise, the user gave an unknown request, so throw
             * an exception.
             */
            throw new IllegalArgumentException
                (visitorRequest + " is not a supported visitor");
    }
}
	
	
	
