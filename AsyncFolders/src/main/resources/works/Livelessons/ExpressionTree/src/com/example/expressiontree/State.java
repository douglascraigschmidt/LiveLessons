package com.example.expressiontree;

import java.util.Iterator;
import java.util.HashMap;

/**
 * @class State
 * 
 * @brief Implementation of the State pattern that is used to define
 *        the various states that affect how users operations are
 *        processed.  Plays the role of the "State" base class in the
 *        State pattern that is used as the basis for the subclasses
 *        that actually define the various states.
 */
public class State {
    /**
     * A factory that creates the appropriate visitors.
     */
    private static VisitorFactory visitorFactory = 
        new VisitorFactory();

    /**
     * Throws an exception if called in the wrong state.
     */
    void format(TreeOps context, String newFormat) {
        throw new IllegalStateException("State.format() called in invalid state");
    }
	  
    /** 
     * Throws an exception if called in the wrong state.
     */
    void makeTree(TreeOps context,
                  String expression) {
        throw new IllegalStateException("State.makeTree() called in invalid state");
    }
	  
    /** 
     * Throws an exception if called in the wrong state.
     */
    void print(TreeOps context,
               String format) {
        throw new IllegalStateException("State.print() called in invalid state");
    }
	  
    /** 
     * Throws an exception if called in the wrong state.
     */
    void evaluate(TreeOps context,
                  String format) {
        throw new IllegalStateException("State.evaluate() called in invalid state");
    }
    
    /** 
     * Print the operators and operands of the @a tree using the
     * designated @a traversalOrder.
     */
    static void printTree(ExpressionTree tree,
                          String traversalOrder) {		  
        if (traversalOrder.equals(""))
            /** 
             * Default to in-order if user doesn't explicitly request
             * a print order.
             */
            traversalOrder = "in-order";

        /**
         * Note the high pattern density in the code below, which uses
         * of the Factory Method, Iterator, Bridge, Strategy, and
         * Visitor patterns.
         */

        /** Create the PrintVisitor using a factory. */
        Visitor printVisitor = visitorFactory.makeVisitor("print"); 
        
        /** 
         * Iterate through all nodes in the expression tree and accept
         * the printVisitor to print each type of node.
         */
        for(Iterator<ExpressionTree> it = tree.makeIterator(traversalOrder);
            it.hasNext();
            )
            it.next().accept(printVisitor);
    }
	  	
    /** 
     * Evaluate and print the yield of the @a tree using the
     * designated @a traversalOrder.
     */
    static void evaluateTree(ExpressionTree tree,
                             String traversalOrder) {
        if (traversalOrder.equals(""))
            /** 
             * Default to post-order if user doesn't explicitly
             * request an eval order.
             */
            traversalOrder = "post-order";
        else if (!traversalOrder.equals("post-order"))
            throw new IllegalArgumentException(traversalOrder + " evaluation is not supported yet");

        /**
         * Note the high pattern density in the code below, which uses
         * of the Factory Method, Iterator, Bridge, Strategy, and
         * Visitor patterns.
         */

        /** Create the EvaluationVisitor using a factory. */
        Visitor evalVisitor = visitorFactory.makeVisitor("eval");
  
        /** 
         * Iterate through all nodes in the expression tree and accept
         * the evalVisitor to evaluate each type of node.
         */
        for(Iterator<ExpressionTree> it = tree.makeIterator(traversalOrder);
            it.hasNext();
            )
            it.next().accept(evalVisitor);

        Integer total = ((EvaluationVisitor) evalVisitor).total();

        // Use the platform strategy to printout the result.
        Platform.instance().outputLine(total.toString());
    }

    /** 
     * A state without an initialized context or format.
     */
    static class UninitializedState extends State {
        /** Formats the traversal order of the state. */
        public void format(TreeOps context, String newFormat) {
            if (newFormat.equals(""))
                /** 
                 * Default to in-order if user doesn't explicitly
                 * request a format order.
                 */
                newFormat = "in-order";
            else if (!newFormat.equals("in-order"))
                throw new IllegalArgumentException(newFormat 
                                                   + " evaluation is not supported yet");

            /** Transition to the designated UninitializedState. */
            context.state(uninitializedStateFactory.makeUninitializedState(newFormat));
        }

        /**
         * @class UninitializedStateFactory
         * 
         * @brief Implementation of a factory pattern that dynamically
         *        allocates the appropriate @a State object. 
         * 
         *        This class is a variant of the Abstract Factory
         *        pattern that has a set of related factory methods
         *        but which doesn't use inheritance.
         */
        static class UninitializedStateFactory {
            /**
             * A HashMap that maps user format string requests to the
             * corresponding UninitializedState implementations.
             */
            private HashMap<String, State> uninitializedStateMap =
                new HashMap<String, State>();
	  	 	 
            /** Ctor */
            UninitializedStateFactory() {
                uninitializedStateMap.put
                    ("in-order",
                     new InOrderUninitializedState());
                uninitializedStateMap.put
                    ("pre-order",
                     new PreOrderUninitializedState());
                uninitializedStateMap.put
                    ("post-order",
                     new PostOrderUninitializedState());
                uninitializedStateMap.put
                    ("level-order",
                     new LevelOrderUninitializedState());
            }
	  			
            /** 
             * Dynamically allocate a new @a State object based on the
             * designated @a format.
             */
            public State makeUninitializedState(String formatRequest) {
                /** 
                 * Try to find the pre-allocated UninitializedState
                 * implementation. 
                 */
                State state = uninitializedStateMap.get(formatRequest);
	  				
                if(state != null)
                    /** If we find it then return it. */
                    return state;
                else 
                    /** 
                     * Otherwise, the user gave an unknown request, so throw
                     * an exception.
                     */
                    throw new IllegalArgumentException(formatRequest 
                                                       + "is not a supported format");
            }
        }

        /** 
         * A state factory responsible for building uninitilized
         * states.
         */        
        private static UninitializedStateFactory uninitializedStateFactory = 
            new UninitializedStateFactory();

        static class InOrderUninitializedState extends UninitializedState {
            /** 
             * Process the @a expression using an in-order interpreter
             * and update the state of the @a context to the @a
             * InOrderInitializedState.
             */
            public InOrderUninitializedState() {
                super();
            }

            /** 
             * Process the @a expression using a in-order interpreter
             * and update the state of @a treeOps to the @a
             * InOrderInitializedState.
             */
            void makeTree(TreeOps treeOps, String inputExpression) {
                /**
                 * Use the Interpreter and Builder patterns to create
                 * the expression tree designated by user input.
                 */
                treeOps.tree(treeOps.interpreter().interpret(inputExpression));

                /** Transition to the InOrderInitializedState. */
                treeOps.state(new InOrderInitializedState());
            }
        }

        /** 
         * A state formatted in-order and containing an expression
         * tree.
         */
        static class InOrderInitializedState extends InOrderUninitializedState {
            public InOrderInitializedState() {
            }

            /** 
             * Print the current expression tree in the @a context
             * using the designed @a format.
             */
            void print(TreeOps context, String format) {
                State.printTree(context.tree(), format);
            }

            /** 
             * Evaluate the yield of the current expression tree in the @a
             * context using the designed @a format.
             */
            void evaluate(TreeOps context, String format) {
                State.evaluateTree(context.tree(), format);
            }
        }

        /**
         * A state formated level order without an expression tree. 
         */
        static class LevelOrderUninitializedState extends UninitializedState {
            public LevelOrderUninitializedState() {
            }

            /**
             * Process the @a expression using a level-order
             * interpreter and update the state of the @a context to
             * the @a LevelOrderInitializedState.
             */
            void makeTree(TreeOps context, String expression) {
                throw new IllegalStateException("LevelOrderUninitializedState.makeTree() not yet implemented");
            }
        }
		  
        /**
         * A state formated level order and containing an expression
         * tree.
         */
        static class LevelOrderInitializedState extends LevelOrderUninitializedState {
            public LevelOrderInitializedState() {
            }

            /**
             * Print the current expression tree in the @a context
             * using the designed @a format.
             */
            void print(TreeOps context, String format) {
                State.printTree(context.tree(), format);
            }
			  	
            /** 
             * Evaluate the yield of the current expression tree
             * in the @a context using the designed @a format.
             */
            void evaluate(TreeOps context, String format) {
                throw new IllegalArgumentException("LevelOrderInitializedState.evaluate() not yet implemented");
            }
        }

        /**
         * A state formated post order without an expression tree. 
         */
        static class PostOrderUninitializedState extends UninitializedState {
            /**Ctor*/
            public PostOrderUninitializedState() {
            }

            /** 
             * Process the @a expression using a post-order
             * interpreter and update the state of the @a context to
             * the @a PostOrderInitializedState.
             */
            void makeTree(TreeOps context, String expression) {
                throw new IllegalStateException("PostOrderUninitializedState.makeTree() not yet implemented");
            }
        }

        /**
         * A state formated post order and containing an expression
         * tree.
         */
        static class PostOrderInitializedState extends PostOrderUninitializedState {
            /** Ctor */
            public PostOrderInitializedState() {
            }

            /**
             * Print the current expression tree in the @a context
             * using the designed @a format.
             */
            void print(TreeOps context, String format) {
                State.printTree(context.tree(), format);
            }

            /** 
             * Evaluate the yield of the current expression tree
             * in the @a context using the designed @a format.
             */
            void evaluate(TreeOps context, String param) {
                throw new IllegalArgumentException("PostOrderInitializedState.evaluate() not yet implemented");
            }
        }
		  
        /**
         * A state formated pre-order without an expression tree.
         */
        static class PreOrderUninitializedState extends UninitializedState {
            /** Ctor */
            public PreOrderUninitializedState() {
            }
            
            /**
             * Process the @a expression using a pre-order interpreter
             * and update the state of the @a context to the @a
             * PreOrderUninitializedState.
             */
            void makeTree(TreeOps context, String format) {
                throw new IllegalStateException("PreOrderUninitializedState.makeTree() not yet implemented");
            }
        }
	  	  
        /** 
         * A state formated pre-order and containing an expression
         * tree.
         */
        static class PreOrderInitializedState extends PreOrderUninitializedState {
            /** Ctor */
            public PreOrderInitializedState() {
            }

            /** 
             * Print the current expression tree in the @a context
             * using the designed @a format.
             */
            void print(TreeOps context, String format) {
                State.printTree(context.tree(), format);
            }
		  	
            /** 
             * Evaluate the yield of the current expression tree in
             * the @a context using the designed @a format.
             */
            void evaluate(TreeOps context, String format) {
                throw new IllegalArgumentException("PreOrderInitializedState.evaluate() not yet implemented");
            }
        }
    }
}
